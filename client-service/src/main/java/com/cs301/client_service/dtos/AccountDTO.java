package com.cs301.client_service.dtos;

import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.AccountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private String accountId;

    @NotBlank
    private String clientId;

    private String clientName;

    @NotNull
    private AccountType accountType;

    @NotNull
    private AccountStatus accountStatus;

    @NotBlank
    private String openingDate;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal initialDeposit;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @NotBlank
    @Size(min = 1, max = 50)
    private String branchId;
}
