package com.cs301.communication_service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.cs301.communication_service.models.AccountCommunication;
import com.cs301.communication_service.models.Communication;
import com.cs301.communication_service.constants.CommunicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;



public interface AccountCommunicationRepository extends JpaRepository<AccountCommunication, UUID> {

    List<AccountCommunication> findByStatus(CommunicationStatus status);
    List<AccountCommunication> findByAgentId(String agentId);
    Page<AccountCommunication> findByAgentId(String agentId, Pageable pageable);
    
    // With filtering on subject (assuming that's what searchQuery is intended for):
    Page<AccountCommunication> findByAgentIdAndSubjectContainingIgnoreCase(String agentId, String searchQuery, Pageable pageable);
}