package com.cs301.communication_service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.cs301.communication_service.models.Communication;
import com.cs301.communication_service.constants.CommunicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CommunicationRepository extends JpaRepository<Communication, UUID> {

    List<Communication> findByAgentId(String agentId);
    
    List<Communication> findByClientId(String clientId);

    List<Communication> findByStatus(CommunicationStatus status);

    // Without filtering by search query:
    Page<Communication> findByAgentId(String agentId, Pageable pageable);
    
    // With filtering on subject (assuming that's what searchQuery is intended for):
    Page<Communication> findByAgentIdAndSubjectContainingIgnoreCase(String agentId, String searchQuery, Pageable pageable);

}
