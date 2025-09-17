package com.cs301.client_service.exceptions;

public class ActiveAccountsExistException extends RuntimeException {
    public ActiveAccountsExistException(String message) {
        super(message);
    }
}