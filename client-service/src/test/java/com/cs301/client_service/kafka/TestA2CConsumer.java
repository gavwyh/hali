package com.cs301.client_service.kafka;

import com.cs301.shared.protobuf.A2C;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

/**
 * Test consumer for the A2C Kafka topic.
 * Used for monitoring and debugging A2C messages during testing.
 */
public class TestA2CConsumer {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "a2c";
    private static final String GROUP_ID = "client-service-test-a2c-group";
    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8000";

    public static void main(String[] args) {
        System.out.println("Starting A2C Test Consumer...");
        
        // Configure consumer properties
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID + "-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class.getName());
        props.put("schema.registry.url", SCHEMA_REGISTRY_URL);
        props.put("specific.protobuf.value.type", A2C.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, A2C> consumer = new KafkaConsumer<>(props)) {
            // Subscribe to topic
            consumer.subscribe(Collections.singletonList(TOPIC));
            System.out.println("Subscribed to topic: " + TOPIC);
            System.out.println("Starting to poll for messages...");
            
            while (true) {
                ConsumerRecords<String, A2C> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, A2C> record : records) {
                    printMessage(record);
                }
            }
        } catch (Exception e) {
            System.err.println("Error consuming messages: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Print the details of an A2C message
     * 
     * @param record The Kafka consumer record containing the A2C message
     */
    private static void printMessage(ConsumerRecord<String, A2C> record) {
        System.out.println("\n=== A2C Message Received ===");
        System.out.println("Key: " + record.key());
        System.out.println("Partition: " + record.partition());
        System.out.println("Offset: " + record.offset());
        
        A2C a2c = record.value();
        System.out.println("\nMessage Content:");
        System.out.println("  Agent ID: " + a2c.getAgentId());
        System.out.println("  Client ID: " + a2c.getClientId());
        System.out.println("  Client Email: " + a2c.getClientEmail());
        System.out.println("  CRUD Type: " + a2c.getCrudType());
        System.out.println("  Account ID: " + a2c.getAccountId());
        System.out.println("  Account Type: " + a2c.getAccountType());
        
        System.out.println("=============================\n");
    }
}
