package com.cs301.crm.dtos.responses;

import com.cs301.crm.models.UserRole;

import java.util.UUID;

public record ReadResponseDTO(
        UUID id,

        String firstName,

        String lastName,

        String email,

        boolean enabled,

        UserRole role
) {
}
