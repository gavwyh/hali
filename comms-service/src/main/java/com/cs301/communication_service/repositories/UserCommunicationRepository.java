package com.cs301.communication_service.repositories;

import com.cs301.communication_service.models.UserCommunication;
import com.cs301.communication_service.constants.CommunicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.util.List;


public interface UserCommunicationRepository extends JpaRepository<UserCommunication, UUID> {

    // List<UserCommunication> findByUserId(String userId);
    
    // List<UserCommunication> findByUserEmail(String userEmail);

    List<UserCommunication> findByStatus(CommunicationStatus status);
    List<UserCommunication> findByUsername(String username);
    
}
