package com.cs301.communication_service.models;

import java.time.*;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CRUDInfo {
    private String attribute;
    private String beforeValue;
    private String afterValue;
    private LocalDateTime timestamp;

    public CRUDInfo() {
        this.timestamp = LocalDateTime.now();
    }

    @JsonCreator 
    public CRUDInfo(
        @JsonProperty("attribute") String attribute, @JsonProperty("beforeValue") String beforeValue, @JsonProperty("afterValue") String afterValue
    ) {
        this.attribute = attribute;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.timestamp = LocalDateTime.now();
    }

    public String getAttribute() {
        return this.attribute;
    }

    public String getBeforeValue() {
        return this.beforeValue;
    }

    public String getAfterValue() {
        return this.afterValue;
    }

    public String getTimeStamp() {     
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm '(SGT)'");
        return timestamp.atZone(ZoneId.of("Asia/Singapore")).format(formatter);
    }
    
}