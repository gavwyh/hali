package com.cs301.client_service.kafka;

import com.cs301.shared.protobuf.C2C;
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
 * Test consumer for the C2C Kafka topic.
 * Used for monitoring and debugging C2C messages during testing.
 */
public class TestC2CConsumer {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "c2c";
    private static final String GROUP_ID = "client-service-test-c2c-group";
    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8000";

    public static void main(String[] args) {
        System.out.println("Starting C2C Test Consumer...");
        
        // Configure consumer properties
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID + "-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class.getName());
        props.put("schema.registry.url", SCHEMA_REGISTRY_URL);
        props.put("specific.protobuf.value.type", C2C.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, C2C> consumer = new KafkaConsumer<>(props)) {
            // Subscribe to topic
            consumer.subscribe(Collections.singletonList(TOPIC));
            System.out.println("Subscribed to topic: " + TOPIC);
            System.out.println("Starting to poll for messages...");
            
            while (true) {
                ConsumerRecords<String, C2C> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, C2C> record : records) {
                    printMessage(record);
                }
            }
        } catch (Exception e) {
            System.err.println("Error consuming messages: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Print the details of a C2C message
     * 
     * @param record The Kafka consumer record containing the C2C message
     */
    private static void printMessage(ConsumerRecord<String, C2C> record) {
        System.out.println("\n=== C2C Message Received ===");
        System.out.println("Key: " + record.key());
        System.out.println("Partition: " + record.partition());
        System.out.println("Offset: " + record.offset());
        
        C2C c2c = record.value();
        System.out.println("\nMessage Content:");
        System.out.println("  Agent ID: " + c2c.getAgentId());
        System.out.println("  Client ID: " + c2c.getClientId());
        System.out.println("  Client Email: " + c2c.getClientEmail());
        System.out.println("  CRUD Type: " + c2c.getCrudType());
        
        if (c2c.hasCrudInfo()) {
            System.out.println("\nCRUD Info:");
            System.out.println("  Attribute: " + c2c.getCrudInfo().getAttribute());
            System.out.println("  Before Value: " + c2c.getCrudInfo().getBeforeValue());
            System.out.println("  After Value: " + c2c.getCrudInfo().getAfterValue());
        }
        
        System.out.println("=============================\n");
    }
}
