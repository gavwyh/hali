package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.LogDTO;
import com.cs301.client_service.exceptions.UnauthorizedAccessException;
import com.cs301.client_service.mappers.LogMapper;
import com.cs301.client_service.models.Client;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.services.ClientService;
import com.cs301.client_service.services.LogService;
import com.cs301.client_service.utils.JwtAuthorizationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client-logs")
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);
    private static final String DATE_TIME = "dateTime";

    private final LogService logService;
    private final LogMapper logMapper;
    private final ClientService clientService;
    
    public LogController(LogService logService, LogMapper logMapper, ClientService clientService) {
        this.logService = logService;
        this.logMapper = logMapper;
        this.clientService = clientService;
        logger.info("LogController initialized");
    }

    /**
     * Get all logs
     * Requires: authenticated user
     * - ROLE_AGENT: Only retrieve logs where agentId from JWT subj matches log's agentId
     * - ROLE_ADMIN: no requirements
     */
    @GetMapping
    public ResponseEntity<List<LogDTO>> getAllLogs(
            Authentication authentication,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, DATE_TIME));
        Page<Log> logsPage;
        
        // Handle null or empty searchQuery
        String normalizedSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim() : null;
        
        // Authorization check based on user role
        if (JwtAuthorizationUtil.isAdmin(authentication)) {
            // Admin can see all logs
            logsPage = logService.getAllLogsWithSearch(normalizedSearchQuery, pageable);
        } else if (JwtAuthorizationUtil.isAgent(authentication)) {
            // Agent can only see logs related to their agentId
            String agentId = JwtAuthorizationUtil.getAgentId(authentication);
            logsPage = logService.getLogsByAgentId(agentId, normalizedSearchQuery, pageable);
        } else {
            throw new UnauthorizedAccessException("Insufficient permissions to access logs");
        }
        
        List<LogDTO> logDTOs = logMapper.toDTOList(logsPage.getContent());
        
        return ResponseEntity.ok(logDTOs);
    }

    /**
     * Get logs by client ID
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if agentId from JWT subj == client's agentID
     * - ROLE_ADMIN: no requirements
     */
    @GetMapping("/client")
    public ResponseEntity<List<LogDTO>> getLogsByClientId(
            Authentication authentication,
            @RequestParam String clientId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        // Authorization check based on user role
        if (JwtAuthorizationUtil.isAgent(authentication)) {
            // For agents, verify they can access this client
            String agentId = JwtAuthorizationUtil.getAgentId(authentication);
            Client client = clientService.getClient(clientId);
            
            if (!agentId.equals(client.getAgentId())) {
                throw new UnauthorizedAccessException("Agent does not have access to logs for this client");
            }
        }
        // Admin can access any client's logs, no verification needed
        
        // Handle null or empty searchQuery
        String normalizedSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim() : null;
        
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, DATE_TIME));
        Page<Log> logsPage = logService.getLogsByClientId(clientId, normalizedSearchQuery, pageable);
        
        List<LogDTO> logDTOs = logMapper.toDTOList(logsPage.getContent());
        
        return ResponseEntity.ok(logDTOs);
    }
    
    /**
     * Get logs by agent ID
     * Requires: authenticated user
     * - ROLE_AGENT: ensure agentId == JWT's agentId; get all logs with the JWT of the agentId
     * - ROLE_ADMIN: no requirements
     */
    @GetMapping("/agent")
    public ResponseEntity<List<LogDTO>> getLogsByAgentId(
            Authentication authentication,
            @RequestParam String agentId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        // Authorization check based on user role
        if (JwtAuthorizationUtil.isAgent(authentication)) {
            // For agents, verify they can only access their own logs
            String authAgentId = JwtAuthorizationUtil.getAgentId(authentication);
            if (!authAgentId.equals(agentId)) {
                throw new UnauthorizedAccessException("Agent can only view their own logs");
            }
        }
        // Admin can access any agent's logs, no verification needed
        
        // Handle null or empty searchQuery
        String normalizedSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim() : null;
        
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, DATE_TIME));
        Page<Log> logsPage = logService.getLogsByAgentId(agentId, normalizedSearchQuery, pageable);
        
        List<LogDTO> logDTOs = logMapper.toDTOList(logsPage.getContent());
        
        return ResponseEntity.ok(logDTOs);
    }
}
