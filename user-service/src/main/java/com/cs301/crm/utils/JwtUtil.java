package com.cs301.crm.utils;

import com.cs301.crm.exceptions.JwtCreationException;
import com.nimbusds.jose.jwk.RSAKey;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/**
 * @author: gav
 * @version: 1.1
 * @since: 24-09-18
 * @description: Utility class for JWT operations
 */
@Getter
@Component
public class JwtUtil {

    @Value("${jwt.access.duration}")
    private long expirationTime;

    @Value("${jwt.issuer}")
    private String issuer;

    private final RSAKey rsaKey;

    private final JwtEncoder jwtEncoder;

    @Autowired
    public JwtUtil(JwtEncoder jwtEncoder, RSAKey rsaKey) {
        this.jwtEncoder = jwtEncoder;
        this.rsaKey = rsaKey;
    }

    /**
     * Generates a JWT token post-authentication.
     *
     * @param userDetails the UserDetails object containing user details (authorities, etc)
     * @return a String representation of the signed JWT token (for Bearer)
     */
    public String generateToken(UserDetails userDetails) {
        try {
            JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(issuer)
                .id(UUID.randomUUID().toString())
                .audience(
                    List.of(
                        "crm-client-service",
                        "crm-user-service",
                        "crm-comms-service"
                    )
                )
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(expirationTime))
                .subject(userDetails.getUsername())
                .claim(
                    "scope",
                    userDetails
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(" "))
                )
                .build();

            return jwtEncoder
                .encode(JwtEncoderParameters.from(claimsSet))
                .getTokenValue();
        } catch (Exception e) {
            throw new JwtCreationException("Something went wrong", e);
        }
    }

    public RSAKey getRSAKey() {
        return this.rsaKey;
    }
}
