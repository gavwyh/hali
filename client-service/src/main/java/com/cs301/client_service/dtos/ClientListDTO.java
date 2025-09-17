package com.cs301.client_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified client DTO for list views
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientListDTO {
    private String clientId;
    private String firstName;
    private String lastName;
}
