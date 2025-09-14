package com.cs301.crm.dtos.requests;

import jakarta.validation.constraints.*;

public record DangerousActionOtpVerificationDTO(
        @NotNull(message = "Email cannot be null")
        @NotEmpty(message = "Email cannot be empty")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "OTP cannot be null")
        @Size(min=6, max=6, message = "OTP must be 6 digits")
        @Pattern(message = "OTP must only contain numbers",
                regexp="\\d+$")
        String oneTimePassword,

        @NotNull(message = "OTP context cannot be null")
        @NotEmpty(message = "OTP context cannot be empty")
        String otpContext
) {
}
