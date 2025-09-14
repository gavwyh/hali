package com.cs301.client_service.exceptions;

public class AccountNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Account not found with ID: ";
    
    public AccountNotFoundException(String accountId) {
        super(DEFAULT_MESSAGE + accountId);
    }
    
    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
