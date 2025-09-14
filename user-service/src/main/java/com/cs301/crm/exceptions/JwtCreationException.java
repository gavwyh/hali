package com.cs301.crm.exceptions;

public class JwtCreationException extends RuntimeException {
    public JwtCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
