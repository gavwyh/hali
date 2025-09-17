package com.cs301.client_service.services.impl;

import com.cs301.client_service.constants.TransactionStatus;
import com.cs301.client_service.dtos.TransactionDTO;
import com.cs301.client_service.exceptions.ApiException;
import com.cs301.client_service.mappers.TransactionMapper;
import com.cs301.client_service.models.Transaction;
import com.cs301.client_service.repositories.TransactionRepository;
import com.cs301.client_service.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public List<TransactionDTO> getAllTransactions(String searchQuery, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Transaction> transactions;
        
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            transactions = transactionRepository.searchAllTransactions(searchQuery, pageable);
        } else {
            transactions = transactionRepository.findAll(pageable);
        }
        
        return transactionMapper.toDTOList(transactions.getContent());
    }
    
    @Override
    public List<TransactionDTO> getTransactionsByClientId(String clientId, String searchQuery, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Transaction> transactions;
        
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            transactions = transactionRepository.searchByClientId(clientId, searchQuery, pageable);
        } else {
            transactions = transactionRepository.findByClientClientId(clientId, pageable);
        }
        
        return transactionMapper.toDTOList(transactions.getContent());
    }

    @Override
    public List<TransactionDTO> getTransactionsByAgentId(String agentId, String searchQuery, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Transaction> transactions;
        
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            transactions = transactionRepository.searchByAgentId(agentId, searchQuery, pageable);
        } else {
            transactions = transactionRepository.findByClientAgentId(agentId, pageable);
        }
        
        return transactionMapper.toDTOList(transactions.getContent());
    }

    @Override
    public TransactionDTO getTransactionById(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ApiException("Transaction not found with ID: " + transactionId));
        return transactionMapper.toDTO(transaction);
    }

    @Override
    public List<TransactionDTO> getTransactionsByAccountId(String accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountAccountId(accountId);
        return transactionMapper.toDTOList(transactions);
    }

    @Override
    public List<TransactionDTO> getTransactionsByStatus(TransactionStatus status) {
        List<Transaction> transactions = transactionRepository.findByStatus(status);
        return transactionMapper.toDTOList(transactions);
    }
}
