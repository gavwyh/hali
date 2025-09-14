package com.cs301.client_service.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.topic.c2c}")
    private String c2cTopic;
    
    @Value("${spring.kafka.topic.a2c}")
    private String a2cTopic;
    
    @Value("${spring.kafka.topic.log}")
    private String logTopic;

    /**
     * Creates a producer factory for Kafka
     * @return the configured producer factory
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class);
        configProps.put("schema.registry.url", schemaRegistryUrl);
        configProps.put("auto.register.schemas", true);
        configProps.put("use.latest.version", true);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Creates a KafkaTemplate for sending messages
     * @return the configured KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Creates a Kafka topic for client-to-client communication logs
     * @return the configured topic
     */
    @Bean
    public NewTopic c2cTopic() {
        return TopicBuilder.name(c2cTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    /**
     * Creates a Kafka topic for account-to-client communication logs
     * @return the configured topic
     */
    @Bean
    public NewTopic a2cTopic() {
        return TopicBuilder.name(a2cTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    /**
     * Creates a Kafka topic for log messages
     * @return the configured topic
     */
    @Bean
    public NewTopic logTopic() {
        return TopicBuilder.name(logTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
