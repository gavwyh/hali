package com.cs301.crm.services.impl;

import com.cs301.crm.dtos.requests.CreateUserRequestDTO;
import com.cs301.crm.dtos.responses.GenericResponseDTO;
import com.cs301.crm.models.UserEntity;
import com.cs301.crm.producers.KafkaProducer;
import com.cs301.crm.repositories.UserRepository;
import com.cs301.crm.utils.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private KafkaProducer kafkaProducer;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private Jwt jwt;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, kafkaProducer, redisUtil);

        // Setup Security Context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createUser_ValidRequest_ReturnsSuccessResponse() throws Exception {
        // Arrange
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "John",
                "Doe",
                "john@example.com",
                "AGENT"
        );

        // Mock JWT claims
        when(jwt.getClaimAsString("sub")).thenReturn(UUID.randomUUID().toString());
        when(userRepository.findById(any())).thenReturn(Optional.of(new UserEntity()));

        // Act
        GenericResponseDTO response = userService.createUser(request);

        // Assert
        assertTrue(response.success());
        verify(redisUtil).generateOtp(any(), any());
    }
}