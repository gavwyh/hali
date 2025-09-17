package com.cs301.communication_service.consumer;

import com.cs301.shared.protobuf.U2C;
import com.cs301.communication_service.services.impl.CommunicationServiceImpl;
import com.cs301.communication_service.mappers.CommunicationMapper;
import com.cs301.communication_service.models.UserCommunication;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserConsumer {

    private final CommunicationServiceImpl communicationService;
    private final CommunicationMapper communicationMapper;

    public UserConsumer(CommunicationServiceImpl communicationService, CommunicationMapper communicationMapper) {
        this.communicationService = communicationService;
        this.communicationMapper = communicationMapper;
    }

    @KafkaListener(
        topics = "notifications", 
        // groupId = "communication-group", 
        containerFactory = "kafkaListenerContainerFactoryUser"
    )
    public void consumeU2C(ConsumerRecord<String, U2C> record) {
        System.out.println("received u2c message!");
        UserCommunication communication = communicationMapper.u2cToModel(record);
        System.out.println("converted u2c message!");

        // Now process the DTO (store in DB, send email, etc.)
        communicationService.createUserCommunication(communication);
    }
}

