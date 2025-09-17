package com.cs301.client_service.models;

import com.cs301.client_service.constants.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
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
@Table(name = "Transactions")
public class Transaction {

    @Id
    @Column(name = "transaction_id")
    private UUID transactionId;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    private Client client;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @ToString.Exclude
    private Account account;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private TransactionStatus status;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "description", length = 255)
    private String description;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
