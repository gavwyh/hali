package com.cs301.communication_service.services;

import com.cs301.communication_service.models.*;
import com.cs301.communication_service.constants.*;
import com.cs301.communication_service.dtos.RestCommunicationDTO;

import java.util.*;

public interface CommunicationService {
        Communication createCommunication(Communication communication, CRUDInfo crudInfo);
        CommunicationStatus getCommunicationStatus(UUID communicationId);
        UserCommunication createUserCommunication(UserCommunication communication);
        OtpCommunication createOtpCommunication(OtpCommunication communication);
        AccountCommunication createAccountCommunication(AccountCommunication communication);
        public List<RestCommunicationDTO> getRestCommunicationsDTOs(String userId);
}