package com.cs301.client_service.repositories;

import com.cs301.client_service.models.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, String> {
    
    List<Log> findByClientId(String clientId);
    
    Page<Log> findByClientId(String clientId, Pageable pageable);
    
    List<Log> findByAgentId(String agentId);
    
    Page<Log> findByAgentId(String agentId, Pageable pageable);
    
    List<Log> findByCrudType(Log.CrudType crudType);
    
    Page<Log> findByCrudType(Log.CrudType crudType, Pageable pageable);
    
    List<Log> findByClientIdAndCrudType(String clientId, Log.CrudType crudType);
    
    Page<Log> findByClientIdAndCrudType(String clientId, Log.CrudType crudType, Pageable pageable);
    
    Page<Log> findByAgentIdAndCrudType(String agentId, Log.CrudType crudType, Pageable pageable);
    
    List<Log> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
    
    Page<Log> findByDateTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    @Query(value = "SELECT l FROM Log l WHERE l.clientId = :clientId AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(CAST(l.id as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.attributeName as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.beforeValue as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.afterValue as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.crudType as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.clientId as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.agentId as text)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Log> findByClientIdWithSearch(
            @Param("clientId") String clientId,
            @Param("search") String search,
            Pageable pageable);
    
    @Query(value = "SELECT l FROM Log l WHERE l.agentId = :agentId AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(CAST(l.id as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.attributeName as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.beforeValue as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.afterValue as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.crudType as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.clientId as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.agentId as text)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Log> findByAgentIdWithSearch(
            @Param("agentId") String agentId,
            @Param("search") String search,
            Pageable pageable);
            
    @Query(value = "SELECT l FROM Log l WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(CAST(l.id as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.attributeName as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.beforeValue as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.afterValue as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.crudType as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.clientId as text)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(l.agentId as text)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Log> findAllWithSearch(
            @Param("search") String search,
            Pageable pageable);
}
