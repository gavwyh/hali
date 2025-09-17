package com.cs301.client_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogDTO {
    private String id;
    private String agentId;
    private String clientId;
    private String clientName;
    private String crudType;
    private String dateTime;
    // Optional fields
    private String attributeName;
    private String beforeValue;
    private String afterValue;
}
