package com.cs301.client_service.mappers;

import com.cs301.client_service.dtos.LogDTO;
import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.ClientRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogMapper {
    @Autowired
    private ClientRepository clientRepository;

    /**
     * Convert a Log entity to a LogDTO with a simplified message
     */
    public LogDTO toDTO(Log log) {
        // Generate client name from clientId
        String clientId = log.getClientId() != null ? log.getClientId() : "";
        final StringBuilder clientNameBuilder = new StringBuilder();
        
        if (!clientId.isEmpty()) {
            // Use findById() which returns Optional and handle the case where client might not exist
            clientRepository.findById(clientId).ifPresent(client -> 
                clientNameBuilder.append(client.getFirstName()).append(" ").append(client.getLastName())
            );
        }
        
        return LogDTO.builder()
                .id(log.getId())
                .agentId(log.getAgentId())
                .clientId(log.getClientId())
                .clientName(clientNameBuilder.toString())
                .crudType(log.getCrudType() != null ? log.getCrudType().name() : null)
                .dateTime(log.getDateTime() != null ? log.getDateTime().toString() : null)
                .attributeName(log.getAttributeName())
                .beforeValue(log.getBeforeValue())
                .afterValue(log.getAfterValue())
                .build();
    }

    /**
     * Convert a list of Log entities to a list of LogDTOs
     */
    public List<LogDTO> toDTOList(List<Log> logs) {
        return logs.stream()
                .map(this::toDTO)
                .toList();
    }
}
