package com.cs301.communication_service.consumer;

import com.cs301.shared.protobuf.A2C;
import com.cs301.communication_service.services.impl.CommunicationServiceImpl;
import com.cs301.communication_service.mappers.CommunicationMapper;
import com.cs301.communication_service.models.AccountCommunication;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AccountConsumer {

    private final CommunicationServiceImpl communicationService;
    private final CommunicationMapper communicationMapper;

    public AccountConsumer(CommunicationServiceImpl communicationService, CommunicationMapper communicationMapper) {
        this.communicationService = communicationService;
        this.communicationMapper = communicationMapper;
    }

    @KafkaListener(
        topics = "a2c", 
        // groupId = "communication-group", 
        containerFactory = "kafkaListenerContainerFactoryAccount"
    )
    public void consumeA2C(ConsumerRecord<String, A2C> record) {
        System.out.println("received message!");
        AccountCommunication communication = communicationMapper.a2cToModel(record);
        System.out.println("converted message!");

        // Now process the DTO (store in DB, send email, etc.)
        communicationService.createAccountCommunication(communication);
    }
}

