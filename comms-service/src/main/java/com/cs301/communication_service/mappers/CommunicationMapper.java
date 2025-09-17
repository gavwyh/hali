package com.cs301.communication_service.mappers;


import com.cs301.shared.protobuf.A2C;
import com.cs301.shared.protobuf.C2C;
import com.cs301.shared.protobuf.Otp;
import com.cs301.shared.protobuf.U2C;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.cs301.communication_service.models.*;
import com.cs301.communication_service.constants.CRUDType;
import com.cs301.communication_service.constants.CommunicationStatus;
import com.cs301.communication_service.dtos.CommunicationDTO;
import com.cs301.communication_service.dtos.RestCommunicationDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CommunicationMapper {
    private static final Logger logger = LoggerFactory.getLogger(CommunicationMapper.class);

    public AccountCommunication a2cToModel(ConsumerRecord<String, A2C> record) {
        A2C a2cMessage = record.value();

        AccountCommunication model = new AccountCommunication();
        model.setAgentId(a2cMessage.getAgentId());
        model.setClientId(a2cMessage.getClientId());
        model.setClientEmail(a2cMessage.getClientEmail());
        model.setCrudType(mapCrudType(a2cMessage.getCrudType()));
        model.setAccountId(a2cMessage.getAccountId());
        model.setAccountType(a2cMessage.getAccountType());
        model.setSubject(getAccountSubject(a2cMessage.getCrudType()));
        model.setStatus(CommunicationStatus.SENT);
        
        return model;
    }

    public OtpCommunication otpToModel(ConsumerRecord<String, Otp> record) {
        Otp otpMessage = record.value();

        OtpCommunication model = new OtpCommunication();
        model.setEmail(otpMessage.getUserEmail());
        model.setOtp(otpMessage.getOtp());
        model.setSubject("Your OTP for Verification with Scrooge Bank");
        model.setStatus(CommunicationStatus.SENT);
        
        return model;
    }

    public UserCommunication u2cToModel(ConsumerRecord<String, U2C> record) {
        U2C u2cMessage = record.value();

        UserCommunication model = new UserCommunication();
        model.setUserEmail(u2cMessage.getUserEmail());
        model.setUsername(u2cMessage.getUsername());
        model.setTempPassword(u2cMessage.getTempPassword());
        model.setUserRole(u2cMessage.getUserRole());
        model.setSubject("Welcome Access to Scrooge Bank CRM System");
        model.setStatus(CommunicationStatus.SENT);
        
        return model;
    }

    public Communication c2cToModel(ConsumerRecord<String, C2C> record) {
        C2C c2cMessage = record.value();

        Communication model = new Communication();
        model.setAgentId(c2cMessage.getAgentId());
        model.setClientId(c2cMessage.getClientId());
        model.setClientEmail(c2cMessage.getClientEmail());
        model.setCrudType(mapCrudType(c2cMessage.getCrudType()));
        model.setSubject(getSubjectFromCrudType(mapCrudType(c2cMessage.getCrudType())));
        model.setStatus(CommunicationStatus.SENT);

        return model;
    }

    private CRUDType mapCrudType(String protoCrudType) {
        // Convert string from Protobuf to your enum
        // e.g., "CREATE" -> CRUDType.CREATE
        try {
            return CRUDType.valueOf(protoCrudType.toUpperCase());
        } catch (Exception e) {
            return CRUDType.UPDATE; // or some default
        }
    }

    public CRUDInfo getc2cCrudInfo(ConsumerRecord<String, C2C> record) {
        C2C c2cMessage = record.value();

        CRUDInfo crudInfo = new CRUDInfo(
            c2cMessage.getCrudInfo().getAttribute(),
            c2cMessage.getCrudInfo().getBeforeValue(),
            c2cMessage.getCrudInfo().getAfterValue()
        );

        return crudInfo;
    }

    public CRUDInfo getCrudInfo(CommunicationDTO dto) {
        if (dto == null) return null;

        return dto.getCrudInfo();
    }

    public String getSubjectFromCrudType(CRUDType crudType) {
        switch (crudType) {
            case CREATE:
                return "Verify Your New Profile";
            case UPDATE:
                return "Profile Activity Alert";
            case DELETE:
                return "Your Profile Has Been Deleted";
            default:
                return "Profile Activity Alert";
        }
            
    }

    public String getAccountSubject(String crudType) {
        if (crudType.equals("CREATE")) {
            return "Successfully Created Your Account"; 
        } else if (crudType.equals("DELETE")) {
            return "Successfully Closed Your Account";
        } else {
            return "Account Activity Alert";
        }
    }

    public RestCommunication communicationToRest(Communication communication) {
        return new RestCommunication(communication.getSubject(), communication.getTimestamp(), communication.getStatus(), communication.getClientEmail(), communication.getClientId());
    }

    public RestCommunication userCommunicationToRest(UserCommunication communication) {
        return new RestCommunication(communication.getSubject(), communication.getTimestamp(), communication.getStatus(), communication.getUserEmail(), communication.getUsername());
    }

    public RestCommunication accountCommunicationToRest(AccountCommunication communication) {
        return new RestCommunication(communication.getSubject(), communication.getTimestamp(), communication.getStatus(), communication.getClientEmail(), communication.getClientId());
    }

    public RestCommunicationDTO restToDTO(RestCommunication comm) {
        return RestCommunicationDTO.builder()
            .subject(comm.getSubject())
            .status(comm.geCommunicationStatus().toString()) // convert enum to string
            .timestamp(timeToString(comm.getTimeStamp())) // format as needed
            .build();
    }

    public String timeToString(LocalDateTime time) {          
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm '(SGT)'");
        return time.atZone(ZoneId.of("Asia/Singapore")).format(formatter);
    }

    public List<RestCommunicationDTO> restToDTOList(List<RestCommunication> rcomms) {
        List<RestCommunicationDTO> comms = new ArrayList<>();
        for (RestCommunication c:rcomms) {
            comms.add(restToDTO(c));
        }
        return comms;
    }

    public List<RestCommunicationDTO> getRestCommunicationDTOs(List<Communication> communications, List<AccountCommunication> accountCommunications) {
        List<RestCommunication> comms = new ArrayList<>();
        for (Communication c:communications) {
            comms.add(communicationToRest(c));
        }
        for (AccountCommunication a:accountCommunications) {
            comms.add(accountCommunicationToRest(a));
        }
        comms.sort(Comparator.comparing(RestCommunication::getTimeStamp).reversed());
        return restToDTOList(comms); 
    }
    
}
