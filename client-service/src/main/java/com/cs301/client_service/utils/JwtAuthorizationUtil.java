package com.cs301.client_service.utils;

import com.cs301.client_service.exceptions.UnauthorizedAccessException;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthorizationUtil {

    private static final String ROLE_ADMIN = "SCOPE_ROLE_ADMIN";
    private static final String ROLE_AGENT = "SCOPE_ROLE_AGENT";
    public static final String JWT_SUBJECT_CLAIM = "sub";

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthorizationUtil.class);

    private JwtAuthorizationUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Check if the authenticated user has the ADMIN role
     * @param authentication The authentication object
     * @return true if the user has the ADMIN role
     */
    public static boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        
        return hasRole(authentication, ROLE_ADMIN);
    }

    /**
     * Check if the authenticated user has the AGENT role
     * @param authentication The authentication object
     * @return true if the user has the AGENT role
     */
    public static boolean isAgent(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        
        return hasRole(authentication, ROLE_AGENT);
    }

    /**
     * Check if the agent from JWT can access the specified client
     * @param authentication The authentication object
     * @param client The client to check access for
     * @throws UnauthorizedAccessException if the agent does not have access to the client
     */
    public static void validateAgentAccess(Authentication authentication, Client client) {
        if (authentication == null) {
            logger.warn("Authentication is null during access validation");
            throw new UnauthorizedAccessException("Authentication required");
        }

        // If admin, always allow access
        if (isAdmin(authentication)) {
            return;
        }

        // If agent, only allow access to clients assigned to them
        if (isAgent(authentication)) {
            String agentId = JWTUtil.getClaim(authentication, JWT_SUBJECT_CLAIM);
            // Keep this log as it's important for JWT subject checks
            logger.info("Agent access check - JWT subject: {}, Client agentId: {}", agentId, client.getAgentId());
            
            if (agentId == null || agentId.isEmpty()) {
                logger.warn("Agent ID from JWT is null or empty");
                throw new UnauthorizedAccessException("Invalid agent identifier");
            }
            
            if (!agentId.equals(client.getAgentId())) {
                logger.warn("Access denied: Agent {} attempted to access client of agent {}", 
                            agentId, client.getAgentId());
                throw new UnauthorizedAccessException("Agent does not have access to this client");
            }
            
            return;
        }

        // If not admin or agent, deny access
        logger.warn("Access denied: User has neither ADMIN nor AGENT role");
        throw new UnauthorizedAccessException("Insufficient permissions");
    }

    /**
     * Check if the agent from JWT can access the specified account
     * @param authentication The authentication object
     * @param account The account to check access for
     * @throws UnauthorizedAccessException if the agent does not have access to the account
     */
    public static void validateAccountAccess(Authentication authentication, Account account) {
        if (authentication == null || account == null || account.getClient() == null) {
            throw new UnauthorizedAccessException("Invalid authentication or account data");
        }

        // Admin can access any account
        if (isAdmin(authentication)) {
            return;
        }

        // Agent can only access accounts of their clients
        if (isAgent(authentication)) {
            String agentId = JWTUtil.getClaim(authentication, JWT_SUBJECT_CLAIM);
            if (!agentId.equals(account.getClient().getAgentId())) {
                throw new UnauthorizedAccessException("Agent does not have access to this account");
            }
            return;
        }

        // If not admin or agent, deny access
        throw new UnauthorizedAccessException("Insufficient permissions to access account data");
    }

    /**
     * Get the agent ID from the JWT's subject claim
     * @param authentication The authentication object
     * @return The agent ID from the JWT
     */
    public static String getAgentId(Authentication authentication) {
        if (authentication == null) {
            logger.warn("Authentication is null in getAgentId");
            return null;
        }
        
        String agentId = JWTUtil.getClaim(authentication, JWT_SUBJECT_CLAIM);
        // Keep this log as it's important for JWT subject checks
        logger.info("Retrieved agent ID from JWT: {}", agentId);
        return agentId;
    }

    // Private helper to check for a specific role
    private static boolean hasRole(Authentication authentication, String role) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        // Keep this log as it's important for JWT scope checks
        logger.info("User has the following roles: {}", authorities);
        
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}
