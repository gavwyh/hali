package com.cs301.client_service.services.impl;

import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.LogRepository;
import com.cs301.client_service.services.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LogServiceImpl implements LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);
    
    private final LogRepository logRepository;
    
    public LogServiceImpl(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public Page<Log> getAllLogs(Pageable pageable) {
        return logRepository.findAll(pageable);
    }
    
    @Override
    public Page<Log> getLogsByAgentId(String agentId, Pageable pageable) {
        return logRepository.findByAgentId(agentId, pageable);
    }

    @Override
    public Page<Log> getLogsByClientId(String clientId, String searchQuery, Pageable pageable) {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            return logRepository.findByClientIdWithSearch(clientId, searchQuery, pageable);
        } else {
            return logRepository.findByClientId(clientId, pageable);
        }
    }

    @Override
    public Page<Log> getLogsByCrudType(Log.CrudType crudType, Pageable pageable) {
        return logRepository.findByCrudType(crudType, pageable);
    }
    
    @Override
    public Page<Log> getLogsByCrudTypeAndAgentId(Log.CrudType crudType, String agentId, Pageable pageable) {
        return logRepository.findByAgentIdAndCrudType(agentId, crudType, pageable);
    }

    @Override
    public Page<Log> getLogsByAgentId(String agentId, String searchQuery, Pageable pageable) {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            return logRepository.findByAgentIdWithSearch(agentId, searchQuery, pageable);
        } else {
            return logRepository.findByAgentId(agentId, pageable);
        }
    }
    
    @Override
    public Page<Log> getAllLogsWithSearch(String searchQuery, Pageable pageable) {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            return logRepository.findAllWithSearch(searchQuery, pageable);
        } else {
            return logRepository.findAll(pageable);
        }
    }
}
