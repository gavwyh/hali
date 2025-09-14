package com.cs301.crm.dtos.responses;

public record RefreshLoginResponseDTO(
        String userId,

        String fullName,

        String role,

        String jwt
) {
}
