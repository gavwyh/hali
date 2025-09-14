package com.cs301.client_service.mappers;

import com.cs301.client_service.dtos.TransactionDTO;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.models.Transaction;
import com.cs301.client_service.repositories.AccountRepository;
import com.cs301.client_service.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;

    public TransactionMapper(ClientRepository clientRepository, AccountRepository accountRepository) {
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
    }

    public TransactionDTO toDTO(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        Client client = transaction.getClient();
        
        return TransactionDTO.builder()
                .id(transaction.getTransactionId() != null ? transaction.getTransactionId().toString() : null)
                .clientId(client != null ? client.getClientId() : null)
                .accountId(transaction.getAccount() != null ? transaction.getAccount().getAccountId() : null)
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .date(transaction.getTimestamp())
                .description(transaction.getDescription())
                .clientFirstName(client != null ? client.getFirstName() : null)
                .clientLastName(client != null ? client.getLastName() : null)
                .build();
    }

    public List<TransactionDTO> toDTOList(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::toDTO)
                .toList();
    }

    public Transaction toEntity(TransactionDTO dto) {
        if (dto == null) {
            return null;
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionId(dto.getId() != null ? UUID.fromString(dto.getId()) : UUID.randomUUID());
        transaction.setAmount(dto.getAmount());
        transaction.setStatus(dto.getStatus());
        transaction.setTimestamp(dto.getDate());
        transaction.setDescription(dto.getDescription());

        // Set client and account references
        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId()).orElse(null);
            transaction.setClient(client);
        }

        if (dto.getAccountId() != null) {
            Account account = accountRepository.findById(dto.getAccountId()).orElse(null);
            transaction.setAccount(account);
        }

        return transaction;
    }

    public Transaction updateEntityFromDTO(Transaction transaction, TransactionDTO dto) {
        if (dto == null) {
            return transaction;
        }

        if (dto.getAmount() != null) {
            transaction.setAmount(dto.getAmount());
        }
        
        if (dto.getStatus() != null) {
            transaction.setStatus(dto.getStatus());
        }
        
        if (dto.getDate() != null) {
            transaction.setTimestamp(dto.getDate());
        }
        
        if (dto.getDescription() != null) {
            transaction.setDescription(dto.getDescription());
        }

        // Update client reference if clientId is provided
        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId()).orElse(null);
            transaction.setClient(client);
        }

        // Update account reference if accountId is provided
        if (dto.getAccountId() != null) {
            Account account = accountRepository.findById(dto.getAccountId()).orElse(null);
            transaction.setAccount(account);
        }

        return transaction;
    }
}
