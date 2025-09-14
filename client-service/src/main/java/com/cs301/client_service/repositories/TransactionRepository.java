package com.cs301.client_service.repositories;

import com.cs301.client_service.constants.TransactionStatus;
import com.cs301.client_service.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    Page<Transaction> findByClientClientId(String clientId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.client.agentId = :agentId")
    Page<Transaction> findByClientAgentId(@Param("agentId") String agentId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.client.clientId = :clientId " +
           "AND (:searchQuery IS NULL OR :searchQuery = '' OR " +
           "LOWER(CAST(t.client.firstName as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(CAST(t.client.lastName as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(CAST(t.amount as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(CAST(t.status as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(CAST(t.description as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
    Page<Transaction> searchByClientId(@Param("clientId") String clientId, 
                                      @Param("searchQuery") String searchQuery, 
                                      Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.client.agentId = :agentId " +
           "AND (:searchQuery IS NULL OR :searchQuery = '' OR " +
           "LOWER(CAST(t.client.firstName as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(CAST(t.client.lastName as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(CAST(t.amount as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(CAST(t.status as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(CAST(t.description as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
    Page<Transaction> searchByAgentId(@Param("agentId") String agentId, 
                                     @Param("searchQuery") String searchQuery, 
                                     Pageable pageable);
    
    List<Transaction> findByAccountAccountId(String accountId);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:searchQuery IS NULL OR :searchQuery = '' OR " +
           "LOWER(CAST(t.client.firstName as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(CAST(t.client.lastName as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(CAST(t.amount as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(CAST(t.status as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(CAST(t.description as text)) LIKE LOWER(CONCAT('%', :searchQuery, '%')))")
    Page<Transaction> searchAllTransactions(@Param("searchQuery") String searchQuery, Pageable pageable);
}
