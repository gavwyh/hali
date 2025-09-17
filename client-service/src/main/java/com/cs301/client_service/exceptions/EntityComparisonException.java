package com.cs301.client_service.exceptions;

/**
 * Exception thrown when there is an error comparing entities
 */
public class EntityComparisonException extends RuntimeException {
    
    public EntityComparisonException(String message) {
        super(message);
    }
    
    public EntityComparisonException(String message, Throwable cause) {
        super(message, cause);
    }
}
