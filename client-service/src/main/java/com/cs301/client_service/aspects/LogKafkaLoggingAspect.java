package com.cs301.client_service.aspects;
import com.cs301.client_service.aspects.base.KafkaLoggingAspect;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.producers.KafkaProducer;
import com.cs301.client_service.repositories.LogRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import java.time.Instant;
/**
 * Aspect for logging database log entries to Kafka.
 * This publishes logs to Kafka whenever a database log is created.
 */
@Aspect
@Component
public class LogKafkaLoggingAspect extends KafkaLoggingAspect {
    private final LogRepository logRepository;
    
    public LogKafkaLoggingAspect(LogRepository logRepository, KafkaProducer kafkaProducer) {
        super(kafkaProducer);
        this.logRepository = logRepository;
    }
    
    @Override
    protected String getEntityId(Object entity) {
        if (entity instanceof Log log) {
            return log.getId();
        }
        return null;
    }
    
    @Override
    protected String getClientId(Object entity) {
        if (entity instanceof Log log) {
            return log.getClientId();
        }
        return null;
    }
    
    @Override
    protected String getClientEmail(Object entity) {
        // Logs don't have direct access to client email, use placeholder or null
        return "unknown@email.com";
    }
    
    @Override
    protected String getEntityType() {
        return "Log";
    }
    
    @Override
    protected String getAttributeNames(Object entity) {
        if (entity instanceof Log) {
            return "id,clientId,agentId,crudType,attributeName,beforeValue,afterValue,dateTime";
        }
        return "";
    }
    
    @Override
    protected String getEntityValues(Object entity) {
        if (entity instanceof Log log) {
            return log.getId() + "," +
                   log.getClientId() + "," +
                   log.getAgentId() + "," +
                   log.getCrudType() + "," +
                   log.getAttributeName() + "," +
                   log.getBeforeValue() + "," +
                   log.getAfterValue() + "," +
                   log.getDateTime();
        }
        return "";
    }
    
    /**
     * Pointcut for log creation in the database
     */
    @Pointcut("execution(* com.cs301.client_service.repositories.LogRepository.save(..))")
    public void logCreation() {}
    
    /**
     * Log to Kafka after a database log is created
     */
    @AfterReturning(pointcut = "logCreation()", returning = "result")
    public void logAfterDatabaseLogCreation(JoinPoint joinPoint, Log result) {
        try {
            // Publishing log event to Kafka
            com.cs301.shared.protobuf.Log protoLog = com.cs301.shared.protobuf.Log.newBuilder()
                .setLogId(result.getId())
                .setActor(result.getAgentId())
                .setTransactionType(result.getCrudType().toString())
                .setAction(result.getAttributeName())
                .setTimestamp(Instant.now().toString())
                .build();
            
            super.kafkaProducer.produceLogMessage(result.getId(), protoLog, true);
        } catch (Exception e) {
            logger.error("Error publishing log to Kafka: {}", e.getMessage(), e);
        }
    }
}
