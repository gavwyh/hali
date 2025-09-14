package com.cs301.communication_service.consumer;

import com.cs301.shared.protobuf.Otp;
import com.cs301.communication_service.services.impl.CommunicationServiceImpl;
import com.cs301.communication_service.mappers.CommunicationMapper;
import com.cs301.communication_service.models.OtpCommunication;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OtpConsumer {

    private final CommunicationServiceImpl communicationService;
    private final CommunicationMapper communicationMapper;

    public OtpConsumer(CommunicationServiceImpl communicationService, CommunicationMapper communicationMapper) {
        this.communicationService = communicationService;
        this.communicationMapper = communicationMapper;
    }

    @KafkaListener(
        topics = "otps", 
        // groupId = "communication-group", 
        containerFactory = "kafkaListenerContainerFactoryOtp"
    )
    public void consumeOtp(ConsumerRecord<String, Otp> record) {
        System.out.println("received otp message!");
        OtpCommunication communication = communicationMapper.otpToModel(record);
        System.out.println("converted otp message!");

        // Now process the DTO (store in DB, send email, etc.)
        communicationService.createOtpCommunication(communication);
    }
}

