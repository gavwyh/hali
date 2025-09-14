package com.cs301.crm.exceptions;

public class InvalidUserCredentials extends RuntimeException {
    public InvalidUserCredentials(String message) {
        super(message);
    }
}
