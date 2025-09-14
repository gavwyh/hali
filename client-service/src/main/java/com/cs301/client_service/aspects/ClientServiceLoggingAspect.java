package com.cs301.client_service.aspects;
import com.cs301.client_service.aspects.base.DatabaseLoggingAspect;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.ClientRepository;
import com.cs301.client_service.repositories.LogRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
/**
 * Aspect for logging client service operations to the database.
 */
@Aspect
@Component
public class ClientServiceLoggingAspect extends DatabaseLoggingAspect {
    private final ClientRepository clientRepository;
    private final LogRepository logRepository;
    private final Logger logger = LoggerFactory.getLogger(ClientServiceLoggingAspect.class);
    
    public ClientServiceLoggingAspect(ClientRepository clientRepository, LogRepository logRepository) {
        this.clientRepository = clientRepository;
        this.logRepository = logRepository;
    }
    
    @Override
    protected CrudRepository<Client, String> getRepository() {
        return clientRepository;
    }
    
    @Override
    protected String getEntityId(Object entity) {
        if (entity instanceof Client client) {
            return client.getClientId();
        }
        return null;
    }
    
    @Override
    protected String getClientId(Object entity) {
        // For audit and logging purposes, we extract client ID with additional context
        // which may include extra validation or formatting in the future
        if (entity instanceof Client client) {
            // Get the client ID from the client entity
            String id = client.getClientId();
            // Add potential custom logic for client ID processing that differs from entity ID handling
            return id != null ? id.trim() : null;
        }
        return null;
    }
    
    @Override
    protected String getEntityType() {
        return "Client";
    }
    
    @Override
    protected Map<String, Map.Entry<String, String>> compareEntities(Object oldEntity, Object newEntity) {
        // Use the LoggingUtils.compareEntities method to compare all properties of the entities
        Map<String, Map.Entry<String, String>> changes = com.cs301.client_service.utils.LoggingUtils.compareEntities(oldEntity, newEntity);
        // Remove clientId from changes since it's not actually changing
        changes.remove("clientId");
        return changes;
    }
    
    /**
     * Pointcut for client creation
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.createClient(..))")
    public void clientCreation() {}
    
    /**
     * Pointcut for client retrieval
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.getClient(..))")
    public void clientRetrieval() {}
    
    /**
     * Pointcut for client update
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.updateClient(..))")
    public void clientUpdate() {}
    
    /**
     * Pointcut for client deletion
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.deleteClient(..))")
    public void clientDeletion() {}
    
    /**
     * Pointcut for client verification
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.verifyClient(..))")
    public void clientVerification() {}
    
    /**
     * Log after client creation
     */
    @AfterReturning(pointcut = "clientCreation()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientCreation(JoinPoint joinPoint, Client result) {
        // Use the parent class method to log the creation with empty before/after values
        super.logAfterCreation(joinPoint, result);
    }
    
    /**
     * Log after client retrieval
     */
    @AfterReturning(pointcut = "clientRetrieval()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientRetrieval(JoinPoint joinPoint, Client result) {
        // Use the parent class method to log the retrieval with empty before/after values
        super.logAfterRetrieval(joinPoint, result);
    }
    
    /**
     * Log after client deletion
     */
    @AfterReturning("clientDeletion()")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientDeletion(JoinPoint joinPoint) {
        try {
            Object[] args = super.getArgs(joinPoint);
            String clientId = (String) args[0];
            // Get the client before deletion
            Client client = clientRepository.findById(clientId).orElse(null);
            if (client == null) {
                logger.warn("Client not found for deletion with ID: {}", clientId);
            }
            // Create a log entry with client ID as the attribute name
            Log log = createLogEntry(
                clientId,
                client, // Pass the client entity to ensure client name is set
                Log.CrudType.DELETE,
                clientId, // Store clientId in attributeName
                "",
                ""
            );
            logRepository.save(log);
            logger.info("Logged deletion for client: {}", clientId);
        } catch (Exception e) {
            logger.error("Error logging client deletion", e);
        }
    }
    
    /**
     * Log after client verification
     */
    @AfterReturning(pointcut = "clientVerification()", returning = "result")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterClientVerification(JoinPoint joinPoint, Client result) {
        try {
            Object[] args = super.getArgs(joinPoint);
            String clientId = (String) args[0];
            if (result == null) {
                logger.warn("Client not found for verification with ID: {}", clientId);
                return;
            }
            // Create a log entry with verificationStatus as the changed attribute
            Log log = createLogEntry(
                clientId,
                result, // Pass the client entity to ensure client name is set
                Log.CrudType.UPDATE,
                "verificationStatus", // Store the attribute name
                "PENDING",
                "VERIFIED"
            );
            logRepository.save(log);
            // Logged verification for client
        } catch (Exception e) {
            logger.error("Error logging client verification", e);
        }
    }
}