package com.cs301.client_service.services;

import com.cs301.client_service.models.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LogService {
    
    /**
     * Get all logs with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of logs
     */
    Page<Log> getAllLogs(Pageable pageable);
    
    /**
     * Get all logs with pagination and search
     * 
     * @param searchQuery The search query to filter logs
     * @param pageable Pagination parameters
     * @return Page of logs matching the search query
     */
    Page<Log> getAllLogsWithSearch(String searchQuery, Pageable pageable);
    
    /**
     * Get all logs for a specific agent with pagination
     * 
     * @param agentId The agent ID to filter by
     * @param pageable Pagination parameters
     * @return Page of logs
     */
    Page<Log> getLogsByAgentId(String agentId, Pageable pageable);
    
    /**
     * Get logs by client ID with pagination
     * 
     * @param clientId The client ID to filter by
     * @param searchQuery Optional search query
     * @param pageable Pagination parameters
     * @return Page of logs
     */
    Page<Log> getLogsByClientId(String clientId, String searchQuery, Pageable pageable);
    
    /**
     * Get logs by CRUD type with pagination
     * 
     * @param crudType The CRUD type to filter by
     * @param pageable Pagination parameters
     * @return Page of logs
     */
    Page<Log> getLogsByCrudType(Log.CrudType crudType, Pageable pageable);
    
    /**
     * Get logs by CRUD type for a specific agent with pagination
     * 
     * @param crudType The CRUD type to filter by
     * @param agentId The agent ID to filter by
     * @param pageable Pagination parameters
     * @return Page of logs
     */
    Page<Log> getLogsByCrudTypeAndAgentId(Log.CrudType crudType, String agentId, Pageable pageable);
    
    /**
     * Get logs by agent ID with pagination and optional search
     * 
     * @param agentId The agent ID to filter by
     * @param searchQuery Optional search query
     * @param pageable Pagination parameters
     * @return Page of logs
     */
    Page<Log> getLogsByAgentId(String agentId, String searchQuery, Pageable pageable);
}
