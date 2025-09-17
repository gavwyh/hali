package com.cs301.client_service.dtos;

import com.cs301.client_service.constants.Gender;
import com.cs301.client_service.constants.VerificationStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    private String clientId;

    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;

    @NotBlank
    private String dateOfBirth;

    @NotNull
    private Gender gender;

    @NotBlank
    @Email
    private String emailAddress;

    @NotBlank
    @Size(min = 10, max = 15)
    private String phoneNumber;

    @NotBlank
    @Size(min = 5, max = 100)
    private String address;

    @NotBlank
    @Size(min = 2, max = 50)
    private String city;

    @NotBlank
    @Size(min = 2, max = 50)
    private String state;

    @NotBlank
    @Size(min = 2, max = 50)
    private String country;

    @NotBlank
    @Size(min = 4, max = 10)
    private String postalCode;

    @NotBlank
    @Size(min = 9, max = 9)
    private String nric;
    
    private String agentId;
    
    private VerificationStatus verificationStatus;
}
