package com.cs301.client_service.repositories;

import com.cs301.client_service.constants.AccountStatus;
import com.cs301.client_service.constants.AccountType;
import com.cs301.client_service.models.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByClientClientId(String clientId);
    
    Page<Account> findByClientClientId(String clientId, Pageable pageable);
    
    @Query("SELECT a FROM Account a WHERE " +
           "(:type IS NULL OR a.accountType = :type) AND " +
           "(:status IS NULL OR a.accountStatus = :status)")
    Page<Account> findAllWithFilters(
            @Param("type") AccountType type,
            @Param("status") AccountStatus status,
            Pageable pageable);
    
    @Query(value = "SELECT a FROM Account a WHERE " +
           "(:agentId IS NULL OR a.client.agentId = :agentId) AND " +
           "(:type IS NULL OR a.accountType = :type) AND " +
           "(:status IS NULL OR a.accountStatus = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(CAST(a.accountId as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(a.currency as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(a.branchId as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(a.client.firstName as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(a.client.lastName as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(a.client.emailAddress as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(a.client.phoneNumber as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(a.client.nric as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(a.client.clientId as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(a.client.agentId as text)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Account> findWithSearchAndFilters(
            @Param("agentId") String agentId,
            @Param("type") AccountType type,
            @Param("status") AccountStatus status,
            @Param("search") String search,
            Pageable pageable);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Account a WHERE a.client.clientId = :clientId")
    void deleteByClientClientId(@Param("clientId") String clientId);
}
