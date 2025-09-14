package com.cs301.communication_service.models;

import java.time.*;

import com.cs301.communication_service.constants.CommunicationStatus;

public class RestCommunication {
    private String subject;
    private String clientEmail;
    private String clientId;
    private LocalDateTime timeStamp;
    private CommunicationStatus commStatus;

    public RestCommunication(String subject, LocalDateTime timeStamp, CommunicationStatus commStatus, String clientEmail, String clientId) {
        this.subject = subject;
        this.commStatus = commStatus;
        this.timeStamp = timeStamp;
        this.clientEmail = clientEmail;
        this.clientId = clientId;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public String getSubject() {
        return subject;
    }

    public CommunicationStatus geCommunicationStatus() {
        return commStatus;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientEmail() {
        return clientEmail;
    }

}
