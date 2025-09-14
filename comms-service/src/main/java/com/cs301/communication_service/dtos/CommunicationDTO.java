package com.cs301.communication_service.dtos;

//import java.time.LocalDateTime;

import com.cs301.communication_service.constants.*;
import com.cs301.communication_service.models.CRUDInfo;

//import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationDTO {

    @NotBlank
    private String agentId;

    @NotBlank
    private String clientId;  // The client receiving the communication

    @NotBlank
    @Email
    private String clientEmail;  // The client's email address

    @NotNull
    private CRUDType crudType;

    // @NotBlank
    // private String subject;  // Subject of the email

    // @NotBlank
    // @Lob
    // private String messageBody;  // Content of the email

    @NotNull
    private CommunicationStatus status;

    private CRUDInfo crudInfo;

    // @NotNull
    // private String timestamp;
}
