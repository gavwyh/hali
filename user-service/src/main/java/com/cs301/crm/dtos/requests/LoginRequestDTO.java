package com.cs301.crm.dtos.requests;

import jakarta.validation.constraints.*;

public record LoginRequestDTO(
        @NotNull(message = "Email cannot be null")
        @NotEmpty(message = "Email cannot be empty")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "Password cannot be null")
        @NotEmpty(message = "Password cannot be empty")
        String password
) {}