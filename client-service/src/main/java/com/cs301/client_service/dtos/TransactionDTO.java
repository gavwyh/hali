package com.cs301.client_service.dtos;

import com.cs301.client_service.constants.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private String id;
    private String clientId;
    private String accountId;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime date;
    private String description;
    private String clientFirstName;
    private String clientLastName;
}
