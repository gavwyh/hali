package com.cs301.client_service.services.impl;

import com.cs301.client_service.constants.VerificationStatus;
import com.cs301.client_service.exceptions.AccountNotFoundException;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.exceptions.VerificationException;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.producers.KafkaProducer;
import com.cs301.shared.protobuf.A2C;
import com.cs301.client_service.repositories.AccountRepository;
import com.cs301.client_service.repositories.ClientRepository;
import com.cs301.client_service.services.AccountService;
import com.cs301.client_service.utils.ClientContextHolder;
import com.cs301.client_service.utils.LoggingUtils;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.AccountType;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
    private static final String CRUD_TYPE_DELETE = "DELETE";
    private static final String DEFAULT_CLIENT_ID = "UNKNOWN";
    private static final String DEFAULT_CLIENT_EMAIL = "unknown@example.com";
    private static final String DEFAULT_ACCOUNT_TYPE = "";
    
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final KafkaProducer kafkaProducer;
    
    public AccountServiceImpl(AccountRepository accountRepository, ClientRepository clientRepository, KafkaProducer kafkaProducer) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public Account createAccount(Account account) {
        Client client = validateClient(account.getClient().getClientId());
        
        if (client.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new VerificationException("Cannot create account for unverified client. Client must be verified first.");
        }

        account.setClient(client);
        return accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsByClientId(String clientId) {
        validateClientExists(clientId);
        return accountRepository.findByClientClientId(clientId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Account> getAccountsByClientIdPaginated(String clientId, Pageable pageable) {
        validateClientExists(clientId);
        return accountRepository.findByClientClientId(clientId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Account> getAllAccountsPaginated(Pageable pageable, AccountType type, AccountStatus status) {
        return accountRepository.findAllWithFilters(type, status, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Account> getAccountsWithSearchAndFilters(String agentId, String searchQuery, AccountType type, AccountStatus status, Pageable pageable) {
        return accountRepository.findWithSearchAndFilters(agentId, type, status, searchQuery, pageable);
    }

    @Override
    public void deleteAccount(String accountId) {
        try {
            AccountDeletionContext context = prepareAccountDeletion(accountId);
            
            sendKafkaMessageSafely(() -> 
                sendAccountDeleteKafkaMessage(
                    accountId, 
                    context.clientId, 
                    context.clientEmail, 
                    context.accountType
                ),
                "account deletion"
            );
            
            accountRepository.deleteById(accountId);
        } finally {
            ClientContextHolder.clear();
        }
    }

    @Override
    public void deleteAccountsByClientId(String clientId) {
        validateClientExists(clientId);
        
        List<Account> accounts = accountRepository.findByClientClientId(clientId);
        
        if (accounts.isEmpty()) {
            logger.info("No accounts found for client ID");
            return;
        }
        
        accounts.forEach(account -> deleteAccount(account.getAccountId()));
    }
    
    private Client validateClient(String clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }
    
    private void validateClientExists(String clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ClientNotFoundException(clientId);
        }
    }
    
    private AccountDeletionContext prepareAccountDeletion(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        AccountDeletionContext context = new AccountDeletionContext();
        
        if (account.getClient() != null) {
            Client client = clientRepository.findById(account.getClient().getClientId()).orElse(null);
            
            if (client != null) {
                context.clientId = client.getClientId();
                context.clientEmail = client.getEmailAddress();
                context.accountType = account.getAccountType() != null ? 
                        account.getAccountType().toString() : DEFAULT_ACCOUNT_TYPE;
                
                setClientContext(context.clientId, context.clientEmail, context.accountType);
            }
        }
        
        return context;
    }
    
    private void setClientContext(String clientId, String clientEmail, String accountType) {
        ClientContextHolder.setClientId(clientId);
        ClientContextHolder.setClientEmail(clientEmail);
        ClientContextHolder.setAccountType(accountType);
    }
    
    private void sendKafkaMessageSafely(Runnable messageSender, String operationType) {
        try {
            messageSender.run();
        } catch (Exception e) {
            logger.error("Error sending A2C message for {}: {}", operationType, e.getMessage(), e);
            // Continue with operation even if message sending fails
        }
    }
    
    private void sendAccountDeleteKafkaMessage(String accountId, String clientId, String clientEmail, String accountType) {
        logger.info("Sending A2C message for account deletion");
        
        A2C a2c = A2C.newBuilder()
                .setAgentId(LoggingUtils.getCurrentAgentId())
                .setClientId(clientId)
                .setClientEmail(clientEmail)
                .setCrudType(CRUD_TYPE_DELETE)
                .setAccountId(accountId)
                .setAccountType(accountType)
                .build();
        
        kafkaProducer.produceA2CMessage(accountId, a2c, true);
        logger.info("Successfully sent A2C message for account deletion");
    }
    
    private static class AccountDeletionContext {
        String clientId = DEFAULT_CLIENT_ID;
        String clientEmail = DEFAULT_CLIENT_EMAIL;
        String accountType = DEFAULT_ACCOUNT_TYPE;
    }
}
