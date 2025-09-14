package com.cs301.client_service.aspects.base;

import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.LogRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Base class for database logging aspects.
 * Provides common functionality for logging operations to the database.
 */
@Order(1) // Database logging should happen first
public abstract class DatabaseLoggingAspect extends BaseLoggingAspect {
    @Autowired
    protected LogRepository logRepository;
    
    /**
     * Get the repository for the entity
     */
    protected abstract <T> CrudRepository<T, String> getRepository();

    /**
     * Get the entity ID from the entity
     */
    protected abstract String getEntityId(Object entity);

    /**
     * Get the client ID from the entity
     */
    protected abstract String getClientId(Object entity);

    /**
     * Get the entity type name (e.g., "Client", "Account")
     */
    protected abstract String getEntityType();

    /**
     * Compare old and new entities to determine changes
     * Returns a map of attribute names to pairs of before/after values
     */
    protected abstract Map<String, Map.Entry<String, String>> compareEntities(Object oldEntity, Object newEntity);

    /**
     * Get entity by ID
     */
    protected Object getEntityById(String id) {
        try {
            Optional<?> entity = getRepository().findById(id);
            return entity.orElse(null);
        } catch (Exception e) {
            logger.error("Error retrieving entity by ID: {}", id, e);
            return null;
        }
    }

    /**
     * Log after entity creation
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterCreation(JoinPoint joinPoint, Object result) {
        try {
            if (result == null) {
                logger.warn("{} is null for {} creation", getEntityType(), getEntityType().toLowerCase());
                return;
            }
            
            String clientId = getClientId(result);
            
            // Create a log entry
            Log log = createLogEntry(
                clientId,
                result,
                Log.CrudType.CREATE,
                clientId, // Store clientId in attributeName
                "",
                ""
            );
            
            logRepository.save(log);
            // Logged successful creation
        } catch (Exception e) {
            logger.error("Error logging {} creation", getEntityType().toLowerCase(), e);
        }
    }

    /**
     * Log after entity retrieval
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterRetrieval(JoinPoint joinPoint, Object result) {
        try {
            if (result == null) {
                Object[] args = getArgs(joinPoint);
                String entityId = (String) args[0];
                logger.warn("{} is null for {} ID: {}", getEntityType(), getEntityType().toLowerCase(), entityId);
                return;
            }
            
            String clientId = getClientId(result);
            
            // Create a log entry
            Log log = createLogEntry(
                clientId,
                result,
                Log.CrudType.READ,
                clientId, // Store clientId in attributeName
                "",
                ""
            );
            
            logRepository.save(log);
            // Logged successful read
        } catch (Exception e) {
            logger.error("Error logging {} retrieval", getEntityType().toLowerCase(), e);
        }
    }

    /**
     * Log entity update
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object logUpdate(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // Extract method arguments
            Object[] args = joinPoint.getArgs();
            String entityId = (String) args[0];
            
            // Get the existing entity BEFORE the update
            Object oldEntity = getEntityById(entityId);
            
            if (oldEntity == null) {
                logger.warn("{} not found for update with ID: {}", getEntityType(), entityId);
                return joinPoint.proceed(); // Just proceed with the method execution
            }
            
            String clientId = getClientId(oldEntity);
            
            // Proceed with the method execution (this performs the actual update)
            Object result = joinPoint.proceed();
            
            // The result is the updated entity
            Object updatedEntity = result;
            
            if (updatedEntity != null) {
                // Compare old and new entities to determine what changed
                Map<String, Map.Entry<String, String>> changes = compareEntities(oldEntity, updatedEntity);
                
                // Only create a log if there were changes
                if (!changes.isEmpty()) {
                    // Create consolidated strings for attribute names, before values, and after values
                    StringBuilder attributeNames = new StringBuilder();
                    StringBuilder beforeValues = new StringBuilder();
                    StringBuilder afterValues = new StringBuilder();
                    
                    boolean first = true;
                    for (Map.Entry<String, Map.Entry<String, String>> change : changes.entrySet()) {
                        if (!first) {
                            attributeNames.append("|");
                            beforeValues.append("|");
                            afterValues.append("|");
                        }
                        
                        String attrName = change.getKey();
                        String beforeValue = change.getValue().getKey();
                        String afterValue = change.getValue().getValue();
                        
                        attributeNames.append(attrName);
                        beforeValues.append(beforeValue);
                        afterValues.append(afterValue);
                        
                        first = false;
                    }
                    
                    // Create and save the log entry
                    Log log = createLogEntry(
                        clientId,
                        updatedEntity,
                        Log.CrudType.UPDATE,
                        attributeNames.toString(),
                        beforeValues.toString(),
                        afterValues.toString()
                    );
                    
                    logRepository.save(log);
                    // Logged update
                }
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Error logging {} update", getEntityType().toLowerCase(), e);
            return joinPoint.proceed(); // Still proceed with the method execution even if logging fails
        }
    }

    /**
     * Log before entity deletion
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBeforeDeletion(String entityId) {
        try {
            // Get the entity before deletion
            Object entity = getEntityById(entityId);
            
            if (entity != null) {
                String clientId = getClientId(entity);
                
                // Create a log entry
                Log log = createLogEntry(
                    clientId,
                    null,
                    Log.CrudType.DELETE,
                    clientId, // Store clientId in attributeName
                    "",
                    ""
                );
                
                logRepository.save(log);
                // Logged successful deletion
            } else {
                // If we can't find the entity, use a generic log
                logger.warn("Entity not found for deletion with ID: {}", entityId);
            }
        } catch (Exception e) {
            logger.error("Error logging {} deletion", getEntityType().toLowerCase(), e);
        }
    }
    
    /**
     * Extract client ID from a Client entity
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