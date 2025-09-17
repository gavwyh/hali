package com.cs301.client_service.exceptions;

/**
 * Exception thrown when there is an error converting to or from JSON
 */
public class JsonConversionException extends RuntimeException {
    
    public JsonConversionException(String message) {
        super(message);
    }
    
    public JsonConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
