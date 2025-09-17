package com.cs301.communication_service.models;

import com.cs301.communication_service.constants.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.*;
import java.util.*;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "client_communications")
public class Communication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @NotBlank
    private String agentId;  // The agent performing the action

    @NotBlank
    private String clientId;  // The client receiving the communication

    @NotBlank
    @Email
    private String clientEmail;  // The client's email address

    @NotNull
    @Enumerated(EnumType.STRING)
    private CRUDType crudType;

    @NotBlank
    private String subject;  // Subject of the email

    @NotNull
    @Enumerated(EnumType.STRING)
    private CommunicationStatus status = CommunicationStatus.SENT;  // SENT, FAILED, PENDING

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();  // When the email was sent
}
