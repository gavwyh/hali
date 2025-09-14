package com.cs301.crm.services.impl;

import com.cs301.crm.dtos.requests.LoginRequestDTO;
import com.cs301.crm.dtos.requests.OtpVerificationDTO;
import com.cs301.crm.dtos.responses.GenericResponseDTO;
import com.cs301.crm.exceptions.InvalidUserCredentials;
import com.cs301.crm.models.User;
import com.cs301.crm.models.UserEntity;
import com.cs301.crm.repositories.UserRepository;
import com.cs301.crm.utils.JwtUtil;
import com.cs301.crm.utils.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private UserRepository userRepository;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(authenticationManager, jwtUtil, redisUtil, userRepository);
    }

    @Test
    void login_ValidCredentials_ReturnsSuccessResponse() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password");

        // Act
        GenericResponseDTO response = authService.login(loginRequest);

        // Assert
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(redisUtil).generateOtp(loginRequest.email());
        assertTrue(response.success());
        assertNotNull(response.timestamp());
    }

    @Test
    void verifyOtp_ValidOtp_ReturnsToken() {
        // Arrange
        OtpVerificationDTO otpVerification = new OtpVerificationDTO("test@example.com", "123456");
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("test@example.com");

        when(redisUtil.verifyOtp(otpVerification.email(), otpVerification.oneTimePassword()))
                .thenReturn(false);
        when(userRepository.findByEmail(otpVerification.email()))
                .thenReturn(Optional.of(userEntity));
        when(jwtUtil.generateToken(any(User.class)))
                .thenReturn("token");

        // Act
        GenericResponseDTO response = authService.verifyOtp(otpVerification);

        // Assert
        assertTrue(response.success());
        assertEquals("token", response.message());
        verify(redisUtil).cleanupAfterSuccessfulVerification(otpVerification.email());
    }

    @Test
    void verifyOtp_InvalidOtp_ThrowsException() {
        // Arrange
        OtpVerificationDTO otpVerification = new OtpVerificationDTO("test@example.com", "123456");

        when(redisUtil.verifyOtp(otpVerification.email(), otpVerification.oneTimePassword()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(InvalidUserCredentials.class, () -> authService.verifyOtp(otpVerification));
    }
}