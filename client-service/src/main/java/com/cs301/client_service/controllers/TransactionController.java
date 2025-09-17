package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.TransactionDTO;
import com.cs301.client_service.exceptions.UnauthorizedAccessException;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.services.ClientService;
import com.cs301.client_service.services.TransactionService;
import com.cs301.client_service.utils.JwtAuthorizationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;
    private final ClientService clientService;

    public TransactionController(TransactionService transactionService, ClientService clientService) {
        this.transactionService = transactionService;
        this.clientService = clientService;
    }
    
    /**
     * Get all transactions with pagination and search
     * Requires: authenticated user
     * - ROLE_AGENT: Only returns transactions for clients assigned to the authenticated agent
     * - ROLE_ADMIN: Returns all transactions
     */
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(
            Authentication authentication,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        List<TransactionDTO> transactions;
        
        // Handle null or empty searchQuery
        String normalizedSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim() : null;
        
        // For agents, filter by their agentId
        if (JwtAuthorizationUtil.isAgent(authentication)) {
            String agentId = JwtAuthorizationUtil.getAgentId(authentication);
            transactions = transactionService.getTransactionsByAgentId(agentId, normalizedSearchQuery, page, limit);
        } 
        // For admins, return all transactions
        else if (JwtAuthorizationUtil.isAdmin(authentication)) {
            transactions = transactionService.getAllTransactions(normalizedSearchQuery, page, limit);
        }
        // In case of invalid jwt
        else {
            throw new UnauthorizedAccessException("Insufficient permissions to access transaction data");
        }
        
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions by client ID with pagination and search
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if agentId from JWT subj == client's agentID
     * - ROLE_ADMIN: no requirements
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByClientId(
            Authentication authentication,
            @PathVariable String clientId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        // Validate access to this client
        Client client = clientService.getClient(clientId);
        JwtAuthorizationUtil.validateAgentAccess(authentication, client);
        
        // Handle null or empty searchQuery
        String normalizedSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim() : null;
        
        List<TransactionDTO> transactions = transactionService.getTransactionsByClientId(
                clientId, normalizedSearchQuery, page, limit);
        
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions by agent ID with pagination and search
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if pathvariable agentId == JWT sub agentID
     * - ROLE_ADMIN: no requirements
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByAgentId(
            Authentication authentication,
            @PathVariable String agentId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        // For agents, only allow accessing their own transactions
        if (JwtAuthorizationUtil.isAgent(authentication)) {
            String agentIdFromJwt = JwtAuthorizationUtil.getAgentId(authentication);
            if (!agentIdFromJwt.equals(agentId)) {
                throw new UnauthorizedAccessException("Agent can only view transactions for clients assigned to them");
            }
        }
        // Admin can access any agent's transactions, no check needed
        
        // Handle null or empty searchQuery
        String normalizedSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim() : null;
        
        List<TransactionDTO> transactions = transactionService.getTransactionsByAgentId(
                agentId, normalizedSearchQuery, page, limit);
        
        return ResponseEntity.ok(transactions);
    }
}
