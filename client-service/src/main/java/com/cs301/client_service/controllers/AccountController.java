package com.cs301.client_service.controllers;

import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.AccountType;
import com.cs301.client_service.dtos.AccountDTO;
import com.cs301.client_service.exceptions.UnauthorizedAccessException;
import com.cs301.client_service.mappers.AccountMapper;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.services.AccountService;
import com.cs301.client_service.services.ClientService;
import com.cs301.client_service.utils.JwtAuthorizationUtil;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final ClientService clientService;

    public AccountController(AccountService accountService, AccountMapper accountMapper, ClientService clientService) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
        this.clientService = clientService;
        logger.info("AccountController initialized");
    }

    /**
     * Create a new account
     * Requires: authenticated user
     * - ROLE_AGENT: agentId from JWT subj == account's clientId (FK to client)'s agentId == JWT AgentID
     * - ROLE_ADMIN: no requirements
     */
    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(
            Authentication authentication,
            @Valid @RequestBody AccountDTO accountDTO) {
        
        var accountModel = accountMapper.toModel(accountDTO);
        
        // If user is an agent, validate they can only create accounts for their clients
        if (JwtAuthorizationUtil.isAgent(authentication) && accountModel.getClient() != null) {
            String agentId = JwtAuthorizationUtil.getAgentId(authentication);
            Client client = clientService.getClient(accountModel.getClient().getClientId());
            
            if (!agentId.equals(client.getAgentId())) {
                throw new UnauthorizedAccessException("Agent can only create accounts for their own clients");
            }
        }
        // Admin can create accounts for any client, no validation needed
        
        var savedAccount = accountService.createAccount(accountModel);
        var response = accountMapper.toDto(savedAccount);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all accounts with pagination and optional filtering
     * Requires: authenticated user
     * - ROLE_AGENT: only retrieve accounts where agentId from JWT subj == account's client's agentID
     * - ROLE_ADMIN: retrieve everything
     */
    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) AccountType type,
            @RequestParam(required = false) AccountStatus status) {
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Account> accountsPage;
        
        // Handle null or empty searchQuery
        String normalizedSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim() : null;
        
        // For agent users
        if (JwtAuthorizationUtil.isAgent(authentication)) {
            String agentIdFromJwt = JwtAuthorizationUtil.getAgentId(authentication);
            
            // If agentId request param is provided, validate it matches the JWT agentId
            if (agentId != null && !agentId.isEmpty() && !agentIdFromJwt.equals(agentId)) {
                throw new UnauthorizedAccessException("Agent can only access accounts for their own agentId");
            }
            
            // Use the JWT agentId for filtering
            accountsPage = accountService.getAccountsWithSearchAndFilters(agentIdFromJwt, normalizedSearchQuery, type, status, pageable);
        }
        // For admin users who specify an agentId
        else if (JwtAuthorizationUtil.isAdmin(authentication) && agentId != null && !agentId.isEmpty()) {
            accountsPage = accountService.getAccountsWithSearchAndFilters(agentId, normalizedSearchQuery, type, status, pageable);
        }
        // For admin users with no agentId filter
        else {
            // Use getAccountsWithSearchAndFilters with null agentId to allow searching across all accounts
            accountsPage = accountService.getAccountsWithSearchAndFilters(null, normalizedSearchQuery, type, status, pageable);
        }
        
        List<AccountDTO> accountDTOs = accountMapper.toDtoList(accountsPage.getContent());
        
        return ResponseEntity.ok(accountDTOs);
    }

    /**
     * Get an account by ID
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if agentId from JWT subj == account's client's agentID
     * - ROLE_ADMIN: no requirements
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDTO> getAccount(
            Authentication authentication,
            @PathVariable String accountId) {
        
        Account account = accountService.getAccount(accountId);
        
        // Validate if the authenticated user has access to this account
        JwtAuthorizationUtil.validateAccountAccess(authentication, account);
        
        var response = accountMapper.toDto(account);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an account
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if agentId from JWT subj == account's client's agentID
     * - ROLE_ADMIN: no requirements
     */
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            Authentication authentication,
            @PathVariable String accountId) {
        
        // Validate access before deletion
        Account account = accountService.getAccount(accountId);
        JwtAuthorizationUtil.validateAccountAccess(authentication, account);
        
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get accounts by client ID
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if agentId from JWT subj == client's agentID
     * - ROLE_ADMIN: no requirements
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AccountDTO>> getAccountsByClientId(
            Authentication authentication,
            @PathVariable String clientId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        // Validate client exists and user has access to it
        Client client = clientService.getClient(clientId);
        JwtAuthorizationUtil.validateAgentAccess(authentication, client);
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Account> accountsPage = accountService.getAccountsByClientIdPaginated(clientId, pageable);
        
        List<AccountDTO> accountDTOs = accountMapper.toDtoList(accountsPage.getContent());
        
        return ResponseEntity.ok(accountDTOs);
    }
}
