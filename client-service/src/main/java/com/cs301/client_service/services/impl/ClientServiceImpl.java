package com.cs301.client_service.services.impl;

import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.VerificationStatus;
import com.cs301.client_service.dtos.ClientDTO;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.exceptions.VerificationException;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.mappers.ClientMapper;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.producers.KafkaProducer;
import com.cs301.shared.protobuf.C2C;
import com.cs301.shared.protobuf.CRUDInfo;
import com.cs301.client_service.repositories.ClientRepository;
import com.cs301.client_service.repositories.LogRepository;
import com.cs301.client_service.services.AccountService;
import com.cs301.client_service.services.ClientService;
import com.cs301.client_service.utils.ClientContextHolder;
import com.cs301.client_service.utils.LoggingUtils;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {
    private static final Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);
    private static final String CRUD_TYPE_UPDATE = "UPDATE";
    private static final String CRUD_TYPE_DELETE = "DELETE";
    private static final String OPERATION_UPDATE = "update";
    private static final String OPERATION_DELETE = "delete";
    private static final String OPERATION_VERIFY = "verify";
    
    private final ClientRepository clientRepository;
    private final AccountService accountService;
    private final ClientMapper clientMapper;
    private final KafkaProducer kafkaProducer;
    private final LogRepository logRepository;
    
    public ClientServiceImpl(ClientRepository clientRepository, AccountService accountService, KafkaProducer kafkaProducer, LogRepository logRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.accountService = accountService;
        this.kafkaProducer = kafkaProducer;
        this.logRepository = logRepository;
        this.clientMapper = clientMapper;
    }

    @Override
    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Client getClient(String clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Client> getAllClientsPaginated(Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return clientRepository.findAllWithSearch(search.trim(), pageable);
        }
        return clientRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> getClientsByAgentId(String agentId) {
        return clientRepository.findByAgentId(agentId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Client> getClientsByAgentIdPaginated(String agentId, Pageable pageable) {
        return clientRepository.findByAgentId(agentId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Client> getClientsWithSearchAndAgentId(String agentId, String searchQuery, Pageable pageable) {
        return clientRepository.findWithSearchAndAgentId(agentId, searchQuery, pageable);
    }

    @Override
    public Client updateClient(String clientId, ClientDTO clientDTO) {
        logger.info("Updating client");
        
        // Get a fresh copy of the existing client directly from the repository
        // This ensures we have the real current state, not an already modified copy
        Client existingClient = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
        
        // Create a copy of the existing client for comparison (before state)
        Client beforeClient = existingClient.toBuilder().build();
        
        // Create a new ClientDTO with only the fields that have changed
        ClientDTO filteredDTO = new ClientDTO();
        
        // Always set the clientId
        filteredDTO.setClientId(clientId);
        
        // Only set fields that are different from the existing client
        ClientDTO existingDTO = clientMapper.toDto(existingClient);
        
        if (clientDTO.getFirstName() != null && !clientDTO.getFirstName().equals(existingDTO.getFirstName())) {
            filteredDTO.setFirstName(clientDTO.getFirstName());
        }
        
        if (clientDTO.getLastName() != null && !clientDTO.getLastName().equals(existingDTO.getLastName())) {
            filteredDTO.setLastName(clientDTO.getLastName());
        }
        
        if (clientDTO.getDateOfBirth() != null && !clientDTO.getDateOfBirth().equals(existingDTO.getDateOfBirth())) {
            filteredDTO.setDateOfBirth(clientDTO.getDateOfBirth());
        }
        
        if (clientDTO.getGender() != null && !clientDTO.getGender().equals(existingDTO.getGender())) {
            filteredDTO.setGender(clientDTO.getGender());
        }
        
        if (clientDTO.getEmailAddress() != null && !clientDTO.getEmailAddress().equals(existingDTO.getEmailAddress())) {
            filteredDTO.setEmailAddress(clientDTO.getEmailAddress());
        }
        
        if (clientDTO.getPhoneNumber() != null && !clientDTO.getPhoneNumber().equals(existingDTO.getPhoneNumber())) {
            filteredDTO.setPhoneNumber(clientDTO.getPhoneNumber());
        }
        
        if (clientDTO.getAddress() != null && !clientDTO.getAddress().equals(existingDTO.getAddress())) {
            filteredDTO.setAddress(clientDTO.getAddress());
        }
        
        if (clientDTO.getCity() != null && !clientDTO.getCity().equals(existingDTO.getCity())) {
            filteredDTO.setCity(clientDTO.getCity());
        }
        
        if (clientDTO.getState() != null && !clientDTO.getState().equals(existingDTO.getState())) {
            filteredDTO.setState(clientDTO.getState());
        }
        
        if (clientDTO.getCountry() != null && !clientDTO.getCountry().equals(existingDTO.getCountry())) {
            filteredDTO.setCountry(clientDTO.getCountry());
        }
        
        if (clientDTO.getPostalCode() != null && !clientDTO.getPostalCode().equals(existingDTO.getPostalCode())) {
            filteredDTO.setPostalCode(clientDTO.getPostalCode());
        }
        
        if (clientDTO.getNric() != null && !clientDTO.getNric().equals(existingDTO.getNric())) {
            filteredDTO.setNric(clientDTO.getNric());
        }
        
        if (clientDTO.getAgentId() != null && !clientDTO.getAgentId().equals(existingDTO.getAgentId())) {
            filteredDTO.setAgentId(clientDTO.getAgentId());
        }
        
        if (clientDTO.getVerificationStatus() != null && !clientDTO.getVerificationStatus().equals(existingDTO.getVerificationStatus())) {
            filteredDTO.setVerificationStatus(clientDTO.getVerificationStatus());
        }
        
        // Apply partial updates to the existing client
        Client updatedClient = clientMapper.applyPartialUpdates(existingClient, filteredDTO);
        
        // Ensure clientId is preserved
        updatedClient.setClientId(clientId);
        
        // Check for any actual changes in key fields
        boolean hasChanges = 
            !equals(beforeClient.getFirstName(), updatedClient.getFirstName()) ||
            !equals(beforeClient.getLastName(), updatedClient.getLastName()) ||
            !equals(beforeClient.getEmailAddress(), updatedClient.getEmailAddress()) ||
            !equals(beforeClient.getPhoneNumber(), updatedClient.getPhoneNumber()) ||
            !equals(beforeClient.getAddress(), updatedClient.getAddress()) ||
            !equals(beforeClient.getCity(), updatedClient.getCity()) ||
            !equals(beforeClient.getState(), updatedClient.getState()) ||
            !equals(beforeClient.getCountry(), updatedClient.getCountry()) ||
            !equals(beforeClient.getPostalCode(), updatedClient.getPostalCode()) ||
            !equals(beforeClient.getNric(), updatedClient.getNric()) ||
            !equals(beforeClient.getGender(), updatedClient.getGender()) ||
            !equals(beforeClient.getDateOfBirth(), updatedClient.getDateOfBirth()) ||
            !equals(beforeClient.getAgentId(), updatedClient.getAgentId()) ||
            !equals(beforeClient.getVerificationStatus(), updatedClient.getVerificationStatus());
        
        String clientEmail = updatedClient.getEmailAddress();
        
        try {
            setClientContext(clientId, clientEmail);
            
            // Send Kafka message if there are changes
            if (hasChanges) {
                sendKafkaMessageSafely(() -> 
                    sendClientUpdateKafkaMessage(clientId, clientEmail, beforeClient, updatedClient),
                    "client update"
                );
            } else {
                logger.info("No changes detected, skipping Kafka message");
            }
            
            // Save the updated client
            Client savedClient = clientRepository.save(updatedClient);
            
            // Create a log entry for this update with pipe-separated values for changed fields
            StringBuilder logAttributeNames = new StringBuilder();
            StringBuilder logBeforeValues = new StringBuilder();
            StringBuilder logAfterValues = new StringBuilder();
            
            // Check each field for changes
            if (!equals(beforeClient.getFirstName(), savedClient.getFirstName())) {
                logAttributeNames.append("First Name|");
                logBeforeValues.append(toString(beforeClient.getFirstName())).append("|");
                logAfterValues.append(toString(savedClient.getFirstName())).append("|");
            }
            
            if (!equals(beforeClient.getLastName(), savedClient.getLastName())) {
                logAttributeNames.append("Last Name|");
                logBeforeValues.append(toString(beforeClient.getLastName())).append("|");
                logAfterValues.append(toString(savedClient.getLastName())).append("|");
            }
            
            if (!equals(beforeClient.getEmailAddress(), savedClient.getEmailAddress())) {
                logAttributeNames.append("Email|");
                logBeforeValues.append(toString(beforeClient.getEmailAddress())).append("|");
                logAfterValues.append(toString(savedClient.getEmailAddress())).append("|");
            }
            
            if (!equals(beforeClient.getPhoneNumber(), savedClient.getPhoneNumber())) {
                logAttributeNames.append("Phone|");
                logBeforeValues.append(toString(beforeClient.getPhoneNumber())).append("|");
                logAfterValues.append(toString(savedClient.getPhoneNumber())).append("|");
            }
            
            if (!equals(beforeClient.getAddress(), savedClient.getAddress())) {
                logAttributeNames.append("Address|");
                logBeforeValues.append(toString(beforeClient.getAddress())).append("|");
                logAfterValues.append(toString(savedClient.getAddress())).append("|");
            }
            
            if (!equals(beforeClient.getCity(), savedClient.getCity())) {
                logAttributeNames.append("City|");
                logBeforeValues.append(toString(beforeClient.getCity())).append("|");
                logAfterValues.append(toString(savedClient.getCity())).append("|");
            }
            
            if (!equals(beforeClient.getState(), savedClient.getState())) {
                logAttributeNames.append("State|");
                logBeforeValues.append(toString(beforeClient.getState())).append("|");
                logAfterValues.append(toString(savedClient.getState())).append("|");
            }
            
            if (!equals(beforeClient.getCountry(), savedClient.getCountry())) {
                logAttributeNames.append("Country|");
                logBeforeValues.append(toString(beforeClient.getCountry())).append("|");
                logAfterValues.append(toString(savedClient.getCountry())).append("|");
            }
            
            if (!equals(beforeClient.getPostalCode(), savedClient.getPostalCode())) {
                logAttributeNames.append("Postal Code|");
                logBeforeValues.append(toString(beforeClient.getPostalCode())).append("|");
                logAfterValues.append(toString(savedClient.getPostalCode())).append("|");
            }
            
            if (!equals(beforeClient.getNric(), savedClient.getNric())) {
                logAttributeNames.append("NRIC|");
                logBeforeValues.append(toString(beforeClient.getNric())).append("|");
                logAfterValues.append(toString(savedClient.getNric())).append("|");
            }
            
            if (!equals(beforeClient.getDateOfBirth(), savedClient.getDateOfBirth())) {
                logAttributeNames.append("Date of Birth|");
                logBeforeValues.append(toString(beforeClient.getDateOfBirth())).append("|");
                logAfterValues.append(toString(savedClient.getDateOfBirth())).append("|");
            }
            
            if (!equals(beforeClient.getGender(), savedClient.getGender())) {
                logAttributeNames.append("Gender|");
                logBeforeValues.append(toString(beforeClient.getGender())).append("|");
                logAfterValues.append(toString(savedClient.getGender())).append("|");
            }
            
            if (!equals(beforeClient.getAgentId(), savedClient.getAgentId())) {
                logAttributeNames.append("Agent ID|");
                logBeforeValues.append(toString(beforeClient.getAgentId())).append("|");
                logAfterValues.append(toString(savedClient.getAgentId())).append("|");
            }
            
            if (!equals(beforeClient.getVerificationStatus(), savedClient.getVerificationStatus())) {
                logAttributeNames.append("Verification Status|");
                logBeforeValues.append(toString(beforeClient.getVerificationStatus())).append("|");
                logAfterValues.append(toString(savedClient.getVerificationStatus())).append("|");
            }
            
            // Remove trailing pipes
            String logAttributes = logAttributeNames.toString();
            String logBefore = logBeforeValues.toString();
            String logAfter = logAfterValues.toString();
            
            if (logAttributes.endsWith("|")) {
                logAttributes = logAttributes.substring(0, logAttributes.length() - 1);
            }
            
            if (logBefore.endsWith("|")) {
                logBefore = logBefore.substring(0, logBefore.length() - 1);
            }
            
            if (logAfter.endsWith("|")) {
                logAfter = logAfter.substring(0, logAfter.length() - 1);
            }
            
            Log log = Log.builder()
                .clientId(clientId)
                .crudType(Log.CrudType.UPDATE)
                .attributeName(logAttributes)  // For UPDATE logs, use the pipe-separated list of attributes
                .beforeValue(logBefore)
                .afterValue(logAfter)
                .agentId(LoggingUtils.getCurrentAgentId())
                .dateTime(java.time.LocalDateTime.now())
                .build();
            
            // Save the log entry
            Log savedLog = logRepository.save(log);
            logger.info("Created log entry");
            
            return savedClient;
        } finally {
            ClientContextHolder.clear();
        }
    }

    @Override
    public void deleteClient(String clientId) {
        Client client = validateClientOperation(clientId, OPERATION_DELETE);
        String clientEmail = client.getEmailAddress();
        
        try {
            setClientContext(clientId, clientEmail);
            logger.info("Deleting client");
            
            sendKafkaMessageSafely(() -> 
                sendClientDeleteKafkaMessage(clientId, clientEmail),
                "client deletion"
            );
            
            deleteClientData(clientId);
        } finally {
            ClientContextHolder.clear();
        }
    }

    @Override
    public void verifyClient(String clientId) {
        Client client = validateClientOperation(clientId, OPERATION_VERIFY);
        client.setVerificationStatus(VerificationStatus.VERIFIED);
        clientRepository.save(client);
    }
    
    private void setClientContext(String clientId, String clientEmail) {
        ClientContextHolder.setClientId(clientId);
        ClientContextHolder.setClientEmail(clientEmail);
    }
    
    private void sendKafkaMessageSafely(Runnable messageSender, String operationType) {
        try {
            messageSender.run();
        } catch (Exception e) {
            logger.error("Error sending C2C message for {}: {}", operationType, e.getMessage(), e);
            // Continue with operation even if message sending fails
        }
    }
    
    private void deleteClientData(String clientId) {
        logger.info("Deleting associated accounts");
        accountService.deleteAccountsByClientId(clientId);
        
        logger.info("Deleting the client");
        clientRepository.deleteById(clientId);
        
        logger.info("Client deleted");
    }
    
    private void sendClientUpdateKafkaMessage(String clientId, String clientEmail, Client existingClient, Client updatedClient) {
        logger.info("Sending Kafka message for client update");
        
        // Create lists to track changed fields
        StringBuilder attributeNames = new StringBuilder();
        StringBuilder beforeValues = new StringBuilder();
        StringBuilder afterValues = new StringBuilder();
        
        // Check each field for changes
        if (!equals(existingClient.getFirstName(), updatedClient.getFirstName())) {
            attributeNames.append("firstName,");
            beforeValues.append(toString(existingClient.getFirstName())).append(",");
            afterValues.append(toString(updatedClient.getFirstName())).append(",");
        }
        
        if (!equals(existingClient.getLastName(), updatedClient.getLastName())) {
            attributeNames.append("lastName,");
            beforeValues.append(toString(existingClient.getLastName())).append(",");
            afterValues.append(toString(updatedClient.getLastName())).append(",");
        }
        
        if (!equals(existingClient.getEmailAddress(), updatedClient.getEmailAddress())) {
            attributeNames.append("emailAddress,");
            beforeValues.append(toString(existingClient.getEmailAddress())).append(",");
            afterValues.append(toString(updatedClient.getEmailAddress())).append(",");
        }
        
        if (!equals(existingClient.getPhoneNumber(), updatedClient.getPhoneNumber())) {
            attributeNames.append("phoneNumber,");
            beforeValues.append(toString(existingClient.getPhoneNumber())).append(",");
            afterValues.append(toString(updatedClient.getPhoneNumber())).append(",");
        }
        
        if (!equals(existingClient.getAddress(), updatedClient.getAddress())) {
            attributeNames.append("address,");
            beforeValues.append(toString(existingClient.getAddress())).append(",");
            afterValues.append(toString(updatedClient.getAddress())).append(",");
        }
        
        if (!equals(existingClient.getCity(), updatedClient.getCity())) {
            attributeNames.append("city,");
            beforeValues.append(toString(existingClient.getCity())).append(",");
            afterValues.append(toString(updatedClient.getCity())).append(",");
        }
        
        if (!equals(existingClient.getState(), updatedClient.getState())) {
            attributeNames.append("state,");
            beforeValues.append(toString(existingClient.getState())).append(",");
            afterValues.append(toString(updatedClient.getState())).append(",");
        }
        
        if (!equals(existingClient.getCountry(), updatedClient.getCountry())) {
            attributeNames.append("country,");
            beforeValues.append(toString(existingClient.getCountry())).append(",");
            afterValues.append(toString(updatedClient.getCountry())).append(",");
        }
        
        if (!equals(existingClient.getPostalCode(), updatedClient.getPostalCode())) {
            attributeNames.append("postalCode,");
            beforeValues.append(toString(existingClient.getPostalCode())).append(",");
            afterValues.append(toString(updatedClient.getPostalCode())).append(",");
        }
        
        if (!equals(existingClient.getNric(), updatedClient.getNric())) {
            attributeNames.append("nric,");
            beforeValues.append(toString(existingClient.getNric())).append(",");
            afterValues.append(toString(updatedClient.getNric())).append(",");
        }
        
        if (!equals(existingClient.getDateOfBirth(), updatedClient.getDateOfBirth())) {
            attributeNames.append("dateOfBirth,");
            beforeValues.append(toString(existingClient.getDateOfBirth())).append(",");
            afterValues.append(toString(updatedClient.getDateOfBirth())).append(",");
        }
        
        if (!equals(existingClient.getGender(), updatedClient.getGender())) {
            attributeNames.append("gender,");
            beforeValues.append(toString(existingClient.getGender())).append(",");
            afterValues.append(toString(updatedClient.getGender())).append(",");
        }
        
        if (!equals(existingClient.getAgentId(), updatedClient.getAgentId())) {
            attributeNames.append("agentId,");
            beforeValues.append(toString(existingClient.getAgentId())).append(",");
            afterValues.append(toString(updatedClient.getAgentId())).append(",");
        }
        
        if (!equals(existingClient.getVerificationStatus(), updatedClient.getVerificationStatus())) {
            attributeNames.append("verificationStatus,");
            beforeValues.append(toString(existingClient.getVerificationStatus())).append(",");
            afterValues.append(toString(updatedClient.getVerificationStatus())).append(",");
        }
        
        // Remove trailing commas
        String attributes = attributeNames.toString();
        String before = beforeValues.toString();
        String after = afterValues.toString();
        
        if (attributes.endsWith(",")) {
            attributes = attributes.substring(0, attributes.length() - 1);
        }
        
        if (before.endsWith(",")) {
            before = before.substring(0, before.length() - 1);
        }
        
        if (after.endsWith(",")) {
            after = after.substring(0, after.length() - 1);
        }
        
        // Build CRUD info with formatted strings
        CRUDInfo crudInfo = CRUDInfo.newBuilder()
                .setAttribute(attributes)
                .setBeforeValue(before)
                .setAfterValue(after)
                .build();
        
        // Build the C2C message
        C2C c2c = C2C.newBuilder()
                .setAgentId(LoggingUtils.getCurrentAgentId())
                .setClientId(clientId)
                .setClientEmail(clientEmail)
                .setCrudType(CRUD_TYPE_UPDATE)
                .setCrudInfo(crudInfo)
                .build();
        
        // Send the message
        kafkaProducer.produceMessage(clientId, c2c, true);
        logger.info("Successfully sent Kafka message");
    }
    
    private void sendClientDeleteKafkaMessage(String clientId, String clientEmail) {
        logger.info("Sending C2C message for client deletion");
        
        // Create an empty CRUDInfo with empty attribute, beforeValue, and afterValue
        CRUDInfo crudInfo = CRUDInfo.newBuilder()
                .setAttribute("")
                .setBeforeValue("")
                .setAfterValue("")
                .build();
        
        C2C c2c = C2C.newBuilder()
                .setAgentId(LoggingUtils.getCurrentAgentId())
                .setClientId(clientId)
                .setClientEmail(clientEmail)
                .setCrudType(CRUD_TYPE_DELETE)
                .setCrudInfo(crudInfo)
                .build();
        
        kafkaProducer.produceMessage(clientId, c2c, true);
        logger.info("Successfully sent C2C message for client deletion");
    }
    
    /**
     * Validates a client operation and checks if the client exists
     * For client updates, we no longer use this since we fetch directly from the repository
     */
    private Client validateClientOperation(String clientId, String operation) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (OPERATION_DELETE.equals(operation)) {
            checkForActiveAccounts(clientId);
        }
        
        return client;
    }
    
    private void checkForActiveAccounts(String clientId) {
        List<Account> accounts = accountService.getAccountsByClientId(clientId);
        boolean hasActiveAccounts = accounts.stream()
                .anyMatch(account -> account.getAccountStatus() == AccountStatus.ACTIVE);

        if (hasActiveAccounts) {
            throw new VerificationException("Cannot delete client with active accounts");
        }
    }
    
    // Helper method to safely compare possibly null values
    private boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    // Helper method to safely convert objects to string
    private String toString(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}
