package com.cs301.client_service.exceptions;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response object for API errors
 */
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, Object> additionalDetails;

    // Default constructor
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructor with status and message
    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor with all fields
    public ErrorResponse(int status, String message, Map<String, Object> additionalDetails) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.additionalDetails = additionalDetails;
    }

    // Getters and setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(Map<String, Object> additionalDetails) {
        this.additionalDetails = additionalDetails;
    }
}
