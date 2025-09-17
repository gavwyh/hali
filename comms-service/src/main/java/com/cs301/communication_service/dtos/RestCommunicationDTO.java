package com.cs301.communication_service.dtos;

//import java.time.LocalDateTime;
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
public class RestCommunicationDTO {

    @NotBlank
    private String subject;  // Subject of the email

    @NotNull
    private String status;

    @NotNull
    private String timestamp;

    @NotNull
    private String clientId;

    @NotNull
    private String clientEmail;
}
