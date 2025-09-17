package com.cs301.client_service.exceptions;

public class ClientNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Client not found with ID: ";
    
    public ClientNotFoundException(String clientId) {
        super(DEFAULT_MESSAGE + clientId);
    }
    
    public ClientNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
