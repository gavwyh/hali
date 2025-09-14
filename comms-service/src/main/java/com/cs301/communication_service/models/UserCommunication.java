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
@Table(name = "user_communications")
public class UserCommunication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID DEFAULT gen_random_uuid()")
    private UUID id;

    @NotBlank
    private String username;  // The agent performing the action

    @NotBlank
    private String userRole;  // The client receiving the communication

    @NotBlank
    @Email
    private String userEmail;  // The client's email address

    @NotBlank
    private String subject;  // Subject of the email

    @NotBlank
    private String tempPassword;

    // @NotBlank
    // @Lob
    // private String messageBody;  // Content of the email

    @NotNull
    @Enumerated(EnumType.STRING)
    private CommunicationStatus status = CommunicationStatus.SENT;  // SENT, FAILED, PENDING

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();  // When the email was sent
}
