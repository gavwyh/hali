package com.cs301.communication_service.consumer;

import com.cs301.shared.protobuf.C2C;
import com.cs301.communication_service.models.CRUDInfo;
import com.cs301.communication_service.services.impl.CommunicationServiceImpl;
import com.cs301.communication_service.mappers.CommunicationMapper;
import com.cs301.communication_service.models.Communication;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ClientConsumer {

    private final CommunicationServiceImpl communicationService;
    private final CommunicationMapper communicationMapper;

    public ClientConsumer(CommunicationServiceImpl communicationService, CommunicationMapper communicationMapper) {
        this.communicationService = communicationService;
        this.communicationMapper = communicationMapper;
    }

    @KafkaListener(
        topics = "c2c", 
        // groupId = "communication-group", 
        containerFactory = "kafkaListenerContainerFactoryClient"
    )
    public void consumeC2C(ConsumerRecord<String, C2C> record) {
        System.out.println("received message!");
        Communication communication = communicationMapper.c2cToModel(record);
        CRUDInfo crudInfo = communicationMapper.getc2cCrudInfo(record);
        System.out.println("converted message!");

        // Now process the DTO (store in DB, send email, etc.)
        communicationService.createCommunication(communication, crudInfo);
    }
}

