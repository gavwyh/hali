package com.cs301.client_service.aspects;
import com.cs301.client_service.aspects.base.KafkaLoggingAspect;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.producers.KafkaProducer;
import com.cs301.client_service.repositories.ClientRepository;
import com.cs301.client_service.utils.LoggingUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
/**
 * Aspect for logging client service operations to Kafka.
 * Only handles client creation as update and delete operations are handled directly in the service.
 */
@Aspect
@Component
public class ClientKafkaLoggingAspect extends KafkaLoggingAspect {
    private final ClientRepository clientRepository;
    
    public ClientKafkaLoggingAspect(ClientRepository clientRepository, KafkaProducer kafkaProducer) {
        super(kafkaProducer);
        this.clientRepository = clientRepository;
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
        if (entity instanceof Client client) {
            String clientId = client.getClientId();
            return clientId != null && !clientId.isEmpty() ? clientId : null;
        }
        return null;
    }
    
    @Override
    protected String getClientEmail(Object entity) {
        if (entity instanceof Client client) {
            return client.getEmailAddress();
        }
        return null;
    }
    
    @Override
    protected String getEntityType() {
        return "Client";
    }
    
    @Override
    protected String getAttributeNames(Object entity) {
        if (entity instanceof Client) {
            return LoggingUtils.getClientAttributeNames();
        }
        return "";
    }
    
    @Override
    protected String getEntityValues(Object entity) {
        if (entity instanceof Client client) {
            return LoggingUtils.convertClientToCommaSeparatedValues(client);
        }
        return "";
    }
    
    /**
     * Pointcut for client creation
     */
    @Pointcut("execution(* com.cs301.client_service.services.impl.ClientServiceImpl.createClient(..))")
    public void clientCreation() {}
    
    /**
     * Log after client creation to Kafka
     */
    @AfterReturning(pointcut = "clientCreation()", returning = "result")
    public void logAfterClientCreation(JoinPoint joinPoint, Client result) {
        logAfterCreationToKafka(joinPoint, result);
    }
}
