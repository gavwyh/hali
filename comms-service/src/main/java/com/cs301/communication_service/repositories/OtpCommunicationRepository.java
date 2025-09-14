package com.cs301.communication_service.repositories;

import com.cs301.communication_service.models.OtpCommunication;
import com.cs301.communication_service.constants.CommunicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.util.List;


public interface OtpCommunicationRepository extends JpaRepository<OtpCommunication, UUID> {

    List<OtpCommunication> findByStatus(CommunicationStatus status);
    
}
