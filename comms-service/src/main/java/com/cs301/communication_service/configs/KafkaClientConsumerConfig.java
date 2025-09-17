package com.cs301.communication_service.configs;

import org.apache.kafka.clients.consumer.ConsumerConfig;
// import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.beans.factory.annotation.Value;
// import com.amazonaws.services.schemaregistry.serializers.protobuf.ProtobufSerializer;
import com.cs301.shared.protobuf.C2C;

// import software.amazon.glue.schema.registry.serializers.GlueSchemaRegistryKafkaDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaClientConsumerConfig {
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value("${kafka.schema.registry}")
    private String schemaRegistryUrl;
    
    @Bean
    public ConsumerFactory<String, String> clientConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);

        // Consumer Group ID (Ensure all consumers in a group process messages evenly)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "communication-group");

        // Deserializers
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class.getName());

        // Schema Registry URL configuration
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("specific.protobuf.value.type", C2C.class.getName());
        props.put("auto.register.schemas", false);

        
        // Set missing topics to non-fatal
        props.put("spring.kafka.listener.missing-topics-fatal", false);

        // Set consumer to read from earliest message to maintain FIFO order
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> 
      kafkaListenerContainerFactoryClient() {
   
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(clientConsumerFactory());
        return factory;
    }
    
}
