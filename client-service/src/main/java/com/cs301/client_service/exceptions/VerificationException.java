package com.cs301.client_service.exceptions;

public class VerificationException extends RuntimeException {
    public VerificationException(String message) {
        super(message);
    }
}