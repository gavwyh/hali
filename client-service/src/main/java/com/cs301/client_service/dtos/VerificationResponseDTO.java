package com.cs301.client_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for client verification response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponseDTO {
    private boolean verified;
} 