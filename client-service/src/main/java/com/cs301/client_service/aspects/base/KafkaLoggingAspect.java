package com.cs301.client_service.aspects.base;

import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.producers.KafkaProducer;
import com.cs301.shared.protobuf.A2C;
import com.cs301.shared.protobuf.C2C;
import com.cs301.shared.protobuf.CRUDInfo;
import com.cs301.client_service.utils.LoggingUtils;
import org.aspectj.lang.JoinPoint;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * Base class for Kafka logging aspects.
 * Provides common functionality for logging operations to Kafka.
 */
@Order(2) // Kafka logging should happen after database logging
public abstract class KafkaLoggingAspect extends BaseLoggingAspect {
    
    protected KafkaProducer kafkaProducer;
    
    /**
     * Constructor for dependency injection
     * 
     * @param kafkaProducer the kafka producer to be injected
     */
    protected KafkaLoggingAspect(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }
    
    /**
     * Default constructor for backward compatibility
     */
    protected KafkaLoggingAspect() {
        // Default constructor for backward compatibility
    }
    
    /**
     * Get the entity ID from the entity
     */
    protected abstract String getEntityId(Object entity);

    /**
     * Get the client ID from the entity
     */
    protected abstract String getClientId(Object entity);

    /**
     * Get the client email from the entity
     */
    protected abstract String getClientEmail(Object entity);

    /**
     * Get the entity type name (e.g., "Client", "Account")
     */
    protected abstract String getEntityType();
    
    /**
     * Get attribute names for an entity
     */
    protected abstract String getAttributeNames(Object entity);
    
    /**
     * Get entity values as a string
     */
    protected abstract String getEntityValues(Object entity);
    
    /**
     * Log after entity creation to Kafka
     * 
     * @param joinPoint the join point (not used, kept for backward compatibility)
     * @param result the entity created
     */
    protected void logAfterCreationToKafka(JoinPoint joinPoint, Object result) {
        logAfterCreationToKafkaImpl(result);
    }
    
    /**
     * Implementation of log after entity creation to Kafka
     */
    private void logAfterCreationToKafkaImpl(Object result) {
        try {
            if (result == null) {
                logger.warn("{} is null for {} creation", getEntityType(), getEntityType().toLowerCase());
                return;
            }
            
            String clientId = getClientId(result);
            String clientEmail = getClientEmail(result);
            
            if (result instanceof Account account) {
                // Use A2C format for accounts
                logAccountOperationToKafka(account, clientId, clientEmail, "CREATE");
            } else {
                // Use C2C format for other entities
                logCreateOperationToKafka(result, clientId, clientEmail);
            }
            
            // Creation event published to Kafka
        } catch (Exception e) {
            logger.error("Error publishing {} creation to Kafka", getEntityType().toLowerCase(), e);
        }
    }

    /**
     * Log after entity update to Kafka
     */
    protected void logAfterUpdateToKafka(Object result, Map<String, Map.Entry<String, String>> changes) {
        try {
            if (result == null) {
                logger.warn("{} is null for {} update", getEntityType(), getEntityType().toLowerCase());
                return;
            }
            
            String clientId = getClientId(result);
            String clientEmail = getClientEmail(result);
            
            if (result instanceof Account account) {
                // Use A2C format for accounts
                logAccountOperationToKafka(account, clientId, clientEmail, "UPDATE");
            } else {
                // For other entities, use C2C format with changes
                logUpdateOperationToKafka(result, clientId, clientEmail, changes);
            }
            
            // Update event published to Kafka
        } catch (Exception e) {
            logger.error("Error publishing {} update to Kafka", getEntityType().toLowerCase(), e);
        }
    }

    /**
     * Log after entity deletion to Kafka
     */
    protected void logAfterDeletionToKafka(String clientId, String clientEmail) {
        try {
            // For accounts, we would need the account object to use A2C format
            // For now, we'll use C2C format for all entities
            
            // Create an empty entity for deletion
            Object entity = null;
            if (getEntityType().equals("Client")) {
                Client client = new Client();
                client.setClientId(clientId);
                entity = client;
            } else if (getEntityType().equals("Account")) {
                // For accounts, we would need more information to create a proper Account object
                // This is typically handled in the concrete aspect classes
            }
            
            if (entity != null) {
                logDeleteOperationToKafka(entity, clientId, clientEmail);
                // Deletion event published to Kafka
            }
        } catch (Exception e) {
            logger.error("Error publishing {} deletion to Kafka", getEntityType().toLowerCase(), e);
        }
    }
    
    /**
     * Log a create operation to Kafka
     */
    protected void logCreateOperationToKafka(Object entity, String clientId, String email) {
        try {
            // Publishing creation event to Kafka
            
            String entityId = getMessageKey(entity);
            
            // Create an empty CRUDInfo
            CRUDInfo crudInfo = CRUDInfo.newBuilder().build();
            
            C2C c2c = C2C.newBuilder()
                    .setAgentId(LoggingUtils.getCurrentAgentId())
                    .setClientId(clientId)
                    .setClientEmail(email)
                    .setCrudType("CREATE")
                    .setCrudInfo(crudInfo)
                    .build();
            
            // Pass true to indicate successful operation
            if (kafkaProducer != null) {
                kafkaProducer.produceMessage(entityId, c2c, true);
            }
        } catch (Exception e) {
            logException("Kafka create", e);
        }
    }
    
    /**
     * Log an update operation to Kafka
     */
    protected void logUpdateOperationToKafka(Object newEntity, String clientId, String email, 
                                           Map<String, Map.Entry<String, String>> changes) {
        try {
            // Publishing update event to Kafka
            
            if (changes != null && !changes.isEmpty()) {
                // Create a consolidated string of all attribute names
                StringBuilder attributeNames = new StringBuilder();
                // Create consolidated strings for before and after values
                StringBuilder beforeValues = new StringBuilder();
                StringBuilder afterValues = new StringBuilder();
                
                boolean first = true;
                for (Map.Entry<String, Map.Entry<String, String>> change : changes.entrySet()) {
                    if (!first) {
                        attributeNames.append(",");
                        beforeValues.append(",");
                        afterValues.append(",");
                    }
                    
                    String attrName = change.getKey();
                    String beforeValue = change.getValue().getKey();
                    String afterValue = change.getValue().getValue();
                    
                    attributeNames.append(attrName);
                    beforeValues.append(beforeValue);
                    afterValues.append(afterValue);
                    
                    first = false;
                }
                
                CRUDInfo crudInfo = CRUDInfo.newBuilder()
                        .setAttribute(attributeNames.toString())
                        .setBeforeValue(beforeValues.toString())
                        .setAfterValue(afterValues.toString())
                        .build();
                
                C2C c2c = C2C.newBuilder()
                        .setAgentId(LoggingUtils.getCurrentAgentId())
                        .setClientId(clientId)
                        .setClientEmail(email)
                        .setCrudType("UPDATE")
                        .setCrudInfo(crudInfo)
                        .build();
                
                // Pass true to indicate successful operation
                if (kafkaProducer != null) {
                    kafkaProducer.produceMessage(getMessageKey(newEntity), c2c, true);
                }
            }
        } catch (Exception e) {
            logException("Kafka update", e);
        }
    }
    
    /**
     * Log a delete operation to Kafka
     */
    protected void logDeleteOperationToKafka(Object entity, String clientId, String email) {
        try {
            // Publishing deletion event to Kafka
            
            String entityId = getMessageKey(entity);
            
            // Create an empty CRUDInfo
            CRUDInfo crudInfo = CRUDInfo.newBuilder().build();
            
            C2C c2c = C2C.newBuilder()
                    .setAgentId(LoggingUtils.getCurrentAgentId())
                    .setClientId(clientId)
                    .setClientEmail(email)
                    .setCrudType("DELETE")
                    .setCrudInfo(crudInfo)
                    .build();
            
            // Pass true to indicate successful operation
            if (kafkaProducer != null) {
                kafkaProducer.produceMessage(entityId, c2c, true);
            }
        } catch (Exception e) {
            logException("Kafka delete", e);
        }
    }
    
    /**
     * Log an account operation to Kafka using A2C format
     */
    protected void logAccountOperationToKafka(Account account, String clientId, String email, String crudType) {
        try {
            // Publishing account event to Kafka
            
            String accountId = account.getAccountId();
            String accountType = account.getAccountType() != null ? account.getAccountType().toString() : "";
            
            A2C a2c = A2C.newBuilder()
                    .setAgentId(LoggingUtils.getCurrentAgentId())
                    .setClientId(clientId)
                    .setClientEmail(email)
                    .setCrudType(crudType)
                    .setAccountId(accountId)
                    .setAccountType(accountType)
                    .build();
            
            // Pass true to indicate successful operation
            if (kafkaProducer != null) {
                kafkaProducer.produceA2CMessage(accountId, a2c, true);
            }
        } catch (Exception e) {
            logger.error("Error logging account {} to Kafka: {}", crudType.toLowerCase(), e.getMessage(), e);
        }
    }
    
    /**
     * Get message key for Kafka
     */
    protected String getMessageKey(Object entity) {
        if (entity instanceof Client client) {
            return client.getClientId();
        } else if (entity instanceof Account account) {
            return account.getAccountId();
        } else {
            return "UNKNOWN";
        }
    }
    
    /**
     * Extract client ID from an entity
     */
    @Override
    protected String extractClientId(Object entity) {
        if (entity instanceof Client client) {
            return client.getClientId();
        } else if (entity instanceof Account account) {
            return account.getClient().getClientId();
        } else {
            return "UNKNOWN";
        }
    }
}