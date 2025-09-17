package com.cs301.client_service.models;

import com.cs301.client_service.constants.Gender;
import com.cs301.client_service.constants.VerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

@Entity
@Data
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)")
    private String clientId;

    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;

    @Past
    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotBlank
    @Email
    @Column(unique = true)
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
    @Column(unique = true)
    private String nric;

    @NotBlank
    private String agentId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Account> accounts;
}
