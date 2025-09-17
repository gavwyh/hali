package com.cs301.client_service.producers;

import com.cs301.shared.protobuf.A2C;
import com.cs301.shared.protobuf.C2C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Service for producing messages to Kafka topics
 */
@Component
public class KafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    @Value("${spring.kafka.topic.c2c}")
    private String c2cTopic;
    
    @Value("${spring.kafka.topic.a2c}")
    private String a2cTopic;
    
    @Value("${spring.kafka.topic.log}")
    private String logTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Produces a message to the appropriate Kafka topic based on message type
     * @param message the message to produce
     */
    public void produceMessage(Object message) {
        String topic = getTopic(message);
        
        // Produce message to topic

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent message=[{}] with offset=[{}]",
                    message,
                    result.getRecordMetadata().offset());
            } else {
                logger.error("Unable to send message=[{}] due to : {}",
                    message,
                    ex.getMessage());
            }
        });
    }
    
    /**
     * Produces a message to Kafka if the operation was successful
     * @param entityId the entity ID (used for logging only)
     * @param message the message to produce
     * @param isSuccessful whether the API call was successful
     */
    public void produceMessage(String entityId, Object message, boolean isSuccessful) {
        // Only publish when API calls did not have errors
        if (!isSuccessful) {
            // Skip message production for unsuccessful API calls
            return;
        }

        produceMessage(message);
    }
    
    /**
     * Alias for produceMessage to maintain backward compatibility with A2C messages
     * @param accountId the account ID (used for logging only)
     * @param message the message to produce (should be of type A2C)
     * @param isSuccessful whether the API call was successful
     */
    public void produceA2CMessage(String accountId, Object message, boolean isSuccessful) {
        if (!(message instanceof A2C)) {
            logger.warn("Message is not of type A2C: {}", message.getClass());
        }
        produceMessage(accountId, message, isSuccessful);
    }
    
    /**
     * Produces a log message to Kafka
     * @param logId the log ID (used for logging only)
     * @param message the message to produce (should be of type Log)
     * @param isSuccessful whether the API call was successful
     */
    public void produceLogMessage(String logId, Object message, boolean isSuccessful) {
        if (!(message instanceof com.cs301.shared.protobuf.Log)) {
            logger.warn("Message is not of type Log: {}", message.getClass());
        }
        produceMessage(logId, message, isSuccessful);
    }
    
    /**
     * Determines the appropriate Kafka topic based on the message type
     * @param message the message
     * @return the topic name
     * @throws IllegalArgumentException if the message is not of type C2C or A2C
     */
    private String getTopic(Object message) {
        if (message instanceof C2C) {
            return c2cTopic;
        } else if (message instanceof A2C) {
            return a2cTopic;
        } else if (message instanceof com.cs301.shared.protobuf.Log) {
            return logTopic;
        }
        
        throw new IllegalArgumentException("Message is not of supported type: " + message.getClass());
    }
}
