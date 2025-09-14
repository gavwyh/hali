package com.cs301.crm.controllers;

import com.cs301.crm.dtos.requests.LoginRequestDTO;
import com.cs301.crm.dtos.requests.OtpVerificationDTO;
import com.cs301.crm.dtos.requests.ResendOtpRequestDTO;
import com.cs301.crm.dtos.responses.GenericResponseDTO;
import com.cs301.crm.dtos.responses.RefreshLoginResponseDTO;
import com.cs301.crm.exceptions.InvalidTokenException;
import com.cs301.crm.models.RefreshToken;
import com.cs301.crm.models.User;
import com.cs301.crm.models.UserEntity;
import com.cs301.crm.services.AuthService;
import com.cs301.crm.services.TokenService;
import com.cs301.crm.utils.CookieUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final CookieUtil cookieUtil;
    private final TokenService tokenService;

    @Autowired
    public AuthController(AuthService authService,
                          CookieUtil cookieUtil,
                          TokenService tokenService) {
        this.authService = authService;
        this.cookieUtil = cookieUtil;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<GenericResponseDTO> login(
            @RequestBody @Valid LoginRequestDTO loginRequestDTO
    ) {
        return ResponseEntity.ok(authService.login(loginRequestDTO));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<GenericResponseDTO> verifyOtp(
            @RequestBody @Valid OtpVerificationDTO otpVerificationDTO
    ) {
        GenericResponseDTO response = authService.verifyOtp(otpVerificationDTO);
        RefreshLoginResponseDTO userInfo = (RefreshLoginResponseDTO) response.message();
        String refreshToken = tokenService.createRefreshToken(otpVerificationDTO.email());

        List<ResponseCookie> refreshTokenCookies = cookieUtil.buildRefreshToken(refreshToken);
        ResponseCookie accessCookie = cookieUtil.buildAccessToken(userInfo.jwt());

        return ResponseEntity.ok()
                .headers(headers -> {
                    for (ResponseCookie cookie : refreshTokenCookies) {
                        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
                    }
                    headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
                })
                .body(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<GenericResponseDTO> resendOtp(
            @Valid @RequestBody ResendOtpRequestDTO otpRequestDTO
    ) {
        return ResponseEntity.ok(authService.resendOtp(otpRequestDTO));
    }

    @PostMapping("/logout")
    public ResponseEntity<GenericResponseDTO> logout(
            @CookieValue(value="refreshToken", required = false) String refreshTokenId
    ) {
        List<ResponseCookie> refreshTokenCookies = cookieUtil.buildRefreshToken("destroyedRefresh");
        ResponseCookie accessCookie = cookieUtil.buildAccessToken("destroyedAccess");

        return ResponseEntity.ok()
                .headers(headers -> {
                    for (ResponseCookie cookie : refreshTokenCookies) {
                        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
                    }
                    headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
                })
                .body(tokenService.logout(UUID.fromString(refreshTokenId)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<GenericResponseDTO> refresh(
            @CookieValue(value="refreshToken", required = false) String tokenId
    ) {
        RefreshToken refreshToken = tokenService.validateRefreshToken(
                UUID.fromString(tokenId)
        );

        if (refreshToken == null) {
            throw new InvalidTokenException("Invalid refresh token, please log in again.");
        }

        UserEntity userEntity = refreshToken.getUser();

        String accessToken = authService.generateAccessToken(new User(userEntity));

        ResponseCookie accessCookie = cookieUtil.buildAccessToken(accessToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(
                        new GenericResponseDTO(true,
                                authService.buildUserInformationResponse(userEntity),
                        ZonedDateTime.now()
                )
        );
    }

    @GetMapping("/.well-known/jwks.json")
    public String getJwkSet() {
        return authService.getJwkSet();
    }
}
