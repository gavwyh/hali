package com.cs301.client_service.aspects.base;

import com.cs301.client_service.models.Client;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.services.ClientService;
import com.cs301.client_service.utils.LoggingUtils;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

/**
 * Base abstract class for all logging aspects.
 * Provides common functionality for logging operations.
 */
public abstract class BaseLoggingAspect {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    // Inject ClientService to fetch client information
    @Autowired
    protected ClientService clientService;
    
    /**
     * Create a log entry for a CRUD operation
     * 
     * For CREATE, READ, DELETE operations:
     * - attributeName: clientId
     * - beforeValue/afterValue: as provided
     * 
     * For UPDATE operations:
     * - attributeName: pipe-separated attribute names (e.g., "firstName|address")
     * - beforeValue: pipe-separated values (e.g., "LEE|ABC")
     * - afterValue: pipe-separated values (e.g., "TAN|XX")
     */
    protected Log createLogEntry(String clientId, Object entity, Log.CrudType crudType, 
                               String attributeName, String beforeValue, String afterValue) {
        
        // For CREATE, READ, DELETE operations, store clientId in attributeName if not provided
        if ((crudType == Log.CrudType.CREATE || crudType == Log.CrudType.READ || crudType == Log.CrudType.DELETE) 
                && (attributeName == null || attributeName.isEmpty())) {
                    return Log.builder()
                        .crudType(crudType)
                        .attributeName(clientId) // Just use the client ID without the name
                        .beforeValue("")
                        .afterValue("")
                        .agentId(LoggingUtils.getCurrentAgentId())
                        .clientId(clientId)
                        .dateTime(LocalDateTime.now())
                        .build();
        }
        
        return Log.builder()
                .crudType(crudType)
                .attributeName(attributeName) // Just use the attribute name without adding client name
                .beforeValue(beforeValue)
                .afterValue(afterValue)
                .agentId(LoggingUtils.getCurrentAgentId())
                .clientId(clientId)
                .dateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * Extract client ID from an entity
     */
    protected abstract String extractClientId(Object entity);
    
    /**
     * Check if a response is successful (for controller aspects)
     */
    protected boolean isSuccessfulResponse(ResponseEntity<?> response) {
        return response != null && response.getStatusCode().is2xxSuccessful() && response.getBody() != null;
    }
    
    /**
     * Extract method arguments from a join point
     */
    protected Object[] getArgs(JoinPoint joinPoint) {
        return joinPoint.getArgs();
    }
    
    /**
     * Log an exception that occurred during aspect execution
     */
    protected void logException(String operation, Exception e) {
        logger.error("Error during {} logging operation", operation, e);
    }
}
