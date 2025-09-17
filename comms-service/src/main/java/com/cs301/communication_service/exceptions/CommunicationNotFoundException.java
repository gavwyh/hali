package com.cs301.communication_service.exceptions;

import java.util.*;

public class CommunicationNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Communication not found with ID: ";

    public CommunicationNotFoundException(UUID communicationId) {
        super(DEFAULT_MESSAGE + communicationId.toString());
    }
    
    public CommunicationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
