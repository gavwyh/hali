package com.cs301.crm.producers;

import com.cs301.shared.protobuf.Log;
import com.cs301.shared.protobuf.U2C;
import com.cs301.shared.protobuf.Otp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaProducer {
    @Value("${kafka.topic.log}")
    private String logTopic;

    @Value("${kafka.topic.u2c}")
    private String u2cTopic;

    @Value("${kafka.topic.otp}")
    private String otpTopic;

    private final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Autowired
    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void produceMessage(Object message) {
        String topic = getTopic(message);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, message);
        future.whenComplete( (result, ex) -> {
            if (ex == null) {
                logger.info("Sent message=[{}] with offset [{}]", message.toString(), result.getRecordMetadata().offset());
            } else {
                logger.error("Unable to send message=[{}] due to [{}]", message.toString(), ex.getMessage());
            }
        });
    }
    private String getTopic(Object message) {
        if (message instanceof U2C) {
            return u2cTopic;
        } else if (message instanceof Log) {
            return logTopic;
        } else if (message instanceof Otp) {
            return otpTopic;
        }
        throw new IllegalArgumentException("Message is not of type u2c, log, otp" + message.getClass());
    }
}
