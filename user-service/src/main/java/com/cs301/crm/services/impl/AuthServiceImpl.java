package com.cs301.crm.services.impl;

import com.cs301.crm.dtos.requests.LoginRequestDTO;
import com.cs301.crm.dtos.requests.OtpVerificationDTO;
import com.cs301.crm.dtos.requests.ResendOtpRequestDTO;
import com.cs301.crm.dtos.responses.GenericResponseDTO;
import com.cs301.crm.dtos.responses.RefreshLoginResponseDTO;
import com.cs301.crm.exceptions.InvalidUserCredentials;
import com.cs301.crm.models.User;
import com.cs301.crm.models.UserEntity;
import com.cs301.crm.repositories.UserRepository;
import com.cs301.crm.services.AuthService;
import com.cs301.crm.utils.JwtUtil;
import com.cs301.crm.utils.RedisUtil;
import com.nimbusds.jose.jwk.JWKSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public class AuthServiceImpl implements AuthService {
    private Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final UserRepository userRepository;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           RedisUtil redisUtil,
                           UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
        this.userRepository = userRepository;
    }

    @Override
    public GenericResponseDTO login(LoginRequestDTO loginRequestDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.email(), loginRequestDTO.password())
        );

        redisUtil.generateOtp(loginRequestDTO.email());

        return new GenericResponseDTO(
                true, "Please verify using the OTP sent to your email address", ZonedDateTime.now()
        );
    }

    @Override
    public GenericResponseDTO verifyOtp(OtpVerificationDTO otpVerificationDTO) {
        if (redisUtil.verifyOtp(otpVerificationDTO.email(), otpVerificationDTO.oneTimePassword())) {
            throw new InvalidUserCredentials("Wrong OTP, please try again");
        }

        redisUtil.cleanupAfterSuccessfulVerification(otpVerificationDTO.email());

        UserEntity userEntity = userRepository.findByEmail(otpVerificationDTO.email())
                .orElseThrow(() -> new InvalidUserCredentials("User does not exist"));

        logger.info("{} login successful", otpVerificationDTO.email());

        return new GenericResponseDTO(
                true,
                this.buildUserInformationResponse(userEntity),
                ZonedDateTime.now()
        );
    }

    @Override
    public GenericResponseDTO resendOtp(ResendOtpRequestDTO otpRequestDTO) {
        redisUtil.invalidateExistingOtps(otpRequestDTO.email());
        redisUtil.generateOtp(otpRequestDTO.email());

        return new GenericResponseDTO(
                true, "A new OTP has been sent to your email address.", ZonedDateTime.now()
        );
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        return jwtUtil.generateToken(userDetails);
    }

    @Override
    public String getJwkSet() {
        return new JWKSet(jwtUtil.getRSAKey()).toJSONObject().toString();
    }


    public RefreshLoginResponseDTO buildUserInformationResponse(UserEntity userEntity) {
        return new RefreshLoginResponseDTO(
                userEntity.getId().toString(),
                userEntity.getFirstName() + " " + userEntity.getLastName(),
                userEntity.getUserRole().getAuthority(),
                this.generateAccessToken(new User(userEntity))
        );
    }

}