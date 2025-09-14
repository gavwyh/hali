package com.cs301.client_service.services;

import com.cs301.client_service.constants.TransactionStatus;
import com.cs301.client_service.dtos.TransactionDTO;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    
    List<TransactionDTO> getAllTransactions(String searchQuery, int page, int limit);
    
    List<TransactionDTO> getTransactionsByClientId(String clientId, String searchQuery, int page, int limit);
    
    List<TransactionDTO> getTransactionsByAgentId(String agentId, String searchQuery, int page, int limit);
    
    TransactionDTO getTransactionById(UUID transactionId);
    
    List<TransactionDTO> getTransactionsByAccountId(String accountId);
    
    List<TransactionDTO> getTransactionsByStatus(TransactionStatus status);
}
