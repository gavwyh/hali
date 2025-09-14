package com.cs301.crm.services.impl;

import com.cs301.crm.dtos.responses.GenericResponseDTO;
import com.cs301.crm.exceptions.EmailNotFoundException;
import com.cs301.crm.models.RefreshToken;
import com.cs301.crm.models.UserEntity;
import com.cs301.crm.repositories.TokenRepository;
import com.cs301.crm.repositories.UserRepository;
import com.cs301.crm.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {
    @Value("${jwt.refresh.duration}")
    private long refreshTokenValidity;

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Autowired
    public TokenServiceImpl(TokenRepository tokenRepository,
                            UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public String createRefreshToken(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException(email));

        final UUID tokenId = UUID.randomUUID();
        final RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(tokenId);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(OffsetDateTime.now().plus(Duration.ofMinutes(refreshTokenValidity)));

        tokenRepository.save(refreshToken);
        return refreshToken.getToken().toString();
    }

    @Override
    @Transactional
    public RefreshToken validateRefreshToken(UUID refreshToken) {
        return tokenRepository
                .findByTokenAndExpiresAtAfter(refreshToken, OffsetDateTime.now())
                .orElse(null);
    }

    @Override
    @Transactional
    public GenericResponseDTO logout(UUID refreshToken) {
        tokenRepository.findByTokenAndExpiresAtAfter(refreshToken, OffsetDateTime.now())
                .ifPresent( invalidToken -> {
                    // Set expiration to now in order to invalidate the token
                    invalidToken.setExpiresAt(OffsetDateTime.now());
                    tokenRepository.save(invalidToken);
                });
        return new GenericResponseDTO(
                true, "User logged out successfully", ZonedDateTime.now()
        );
    }
}
