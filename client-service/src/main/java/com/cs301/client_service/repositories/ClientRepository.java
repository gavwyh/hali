package com.cs301.client_service.repositories;

import com.cs301.client_service.models.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, String> {
    List<Client> findByAgentId(String agentId);
    
    Page<Client> findByAgentId(String agentId, Pageable pageable);
    
    @Query(value = "SELECT c FROM Client c WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(CAST(c.clientId as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.firstName as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.lastName as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.emailAddress as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.phoneNumber as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.address as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.city as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.state as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.country as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.postalCode as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.nric as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.agentId as text)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Client> findAllWithSearch(@Param("search") String search, Pageable pageable);
    
    @Query(value = "SELECT c FROM Client c WHERE " +
           "(:agentId IS NULL OR c.agentId = :agentId) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(CAST(c.clientId as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.firstName as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.lastName as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.emailAddress as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.phoneNumber as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.address as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.city as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.state as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.country as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.postalCode as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.nric as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.agentId as text)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Client> findWithSearchAndAgentId(
            @Param("agentId") String agentId,
            @Param("search") String search,
            Pageable pageable);
}
