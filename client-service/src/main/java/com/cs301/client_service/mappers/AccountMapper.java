package com.cs301.client_service.mappers;

import com.cs301.client_service.dtos.AccountDTO;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
public class AccountMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public AccountDTO toDto(Account model) {
        if (model == null) {
            return null;
        }

        String clientName = "";
        if (model.getClient() != null) {
            clientName = model.getClient().getFirstName() + " " + model.getClient().getLastName();
        }

        return AccountDTO.builder()
                .accountId(model.getAccountId())
                .clientId(model.getClient() != null ? model.getClient().getClientId() : null)
                .clientName(clientName)
                .accountType(model.getAccountType())
                .accountStatus(model.getAccountStatus())
                .openingDate(model.getOpeningDate().format(DATE_FORMATTER))
                .initialDeposit(model.getInitialDeposit())
                .currency(model.getCurrency())
                .branchId(model.getBranchId())
                .build();
    }

    public Account toModel(AccountDTO dto) {
        if (dto == null) {
            return null;
        }

        Account model = new Account();
        // Only set accountId if it's provided in the DTO, otherwise let JPA generate it
        if (dto.getAccountId() != null && !dto.getAccountId().isEmpty()) {
            model.setAccountId(dto.getAccountId());
        }

        // Note: Client needs to be set separately as we only have clientId in DTO
        if (dto.getClientId() != null) {
            Client client = new Client();
            client.setClientId(dto.getClientId());
            model.setClient(client);
        }

        model.setAccountType(dto.getAccountType());
        model.setAccountStatus(dto.getAccountStatus());
        model.setOpeningDate(LocalDate.parse(dto.getOpeningDate(), DATE_FORMATTER));
        model.setInitialDeposit(dto.getInitialDeposit());
        model.setCurrency(dto.getCurrency());
        model.setBranchId(dto.getBranchId());

        return model;
    }

    public List<AccountDTO> toDtoList(List<Account> models) {
        if (models == null) {
            return Collections.emptyList();
        }
        return models.stream()
                .map(this::toDto)
                .toList();
    }

    public List<Account> toModelList(List<AccountDTO> dtos) {
        if (dtos == null) {
            return Collections.emptyList();
        }
        return dtos.stream()
                .map(this::toModel)
                .toList();
    }
}
