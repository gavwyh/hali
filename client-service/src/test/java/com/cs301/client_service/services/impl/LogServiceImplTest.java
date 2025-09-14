package com.cs301.client_service.services.impl;

import com.cs301.client_service.models.Log;
import com.cs301.client_service.repositories.LogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceImplTest {

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private LogServiceImpl logService;

    private Log log1;
    private Log log2;
    private List<Log> logs;
    private Pageable pageable;
    private Page<Log> logPage;

    @BeforeEach
    void setUp() {
        // Create test data
        log1 = new Log();
        log1.setId("log-1");
        log1.setClientId("client-1");
        log1.setAgentId("agent-1");
        log1.setCrudType(Log.CrudType.CREATE);
        log1.setAttributeName("firstName");
        log1.setBeforeValue("");
        log1.setAfterValue("John");
        log1.setDateTime(LocalDateTime.now().minusDays(1));

        log2 = new Log();
        log2.setId("log-2");
        log2.setClientId("client-2");
        log2.setAgentId("agent-2");
        log2.setCrudType(Log.CrudType.UPDATE);
        log2.setAttributeName("lastName");
        log2.setBeforeValue("Doe");
        log2.setAfterValue("Smith");
        log2.setDateTime(LocalDateTime.now());

        logs = Arrays.asList(log1, log2);
        pageable = PageRequest.of(0, 10);
        logPage = new PageImpl<>(logs, pageable, logs.size());
    }

    @Test
    void getAllLogs_ShouldReturnAllLogs() {
        // Given
        when(logRepository.findAll(pageable)).thenReturn(logPage);

        // When
        Page<Log> result = logService.getAllLogs(pageable);

        // Then
        assertThat(result).isEqualTo(logPage);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo("log-1");
        assertThat(result.getContent().get(1).getId()).isEqualTo("log-2");
        verify(logRepository).findAll(pageable);
    }

    @Test
    void getLogsByAgentId_WithoutSearch_ShouldReturnLogsForAgent() {
        // Given
        String agentId = "agent-1";
        Page<Log> filteredPage = new PageImpl<>(List.of(log1), pageable, 1);
        when(logRepository.findByAgentId(agentId, pageable)).thenReturn(filteredPage);

        // When
        Page<Log> result = logService.getLogsByAgentId(agentId, pageable);

        // Then
        assertThat(result).isEqualTo(filteredPage);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAgentId()).isEqualTo(agentId);
        verify(logRepository).findByAgentId(agentId, pageable);
    }

    @Test
    void getLogsByClientId_WithoutSearch_ShouldReturnLogsForClient() {
        // Given
        String clientId = "client-1";
        Page<Log> filteredPage = new PageImpl<>(List.of(log1), pageable, 1);
        when(logRepository.findByClientId(clientId, pageable)).thenReturn(filteredPage);

        // When
        Page<Log> result = logService.getLogsByClientId(clientId, null, pageable);

        // Then
        assertThat(result).isEqualTo(filteredPage);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getClientId()).isEqualTo(clientId);
        verify(logRepository).findByClientId(clientId, pageable);
        verify(logRepository, never()).findByClientIdWithSearch(anyString(), anyString(), any(Pageable.class));
    }

    @Test
    void getLogsByClientId_WithSearch_ShouldReturnFilteredLogs() {
        // Given
        String clientId = "client-1";
        String searchQuery = "firstName";
        Page<Log> filteredPage = new PageImpl<>(List.of(log1), pageable, 1);
        when(logRepository.findByClientIdWithSearch(clientId, searchQuery, pageable)).thenReturn(filteredPage);

        // When
        Page<Log> result = logService.getLogsByClientId(clientId, searchQuery, pageable);

        // Then
        assertThat(result).isEqualTo(filteredPage);
        assertThat(result.getContent()).hasSize(1);
        verify(logRepository).findByClientIdWithSearch(clientId, searchQuery, pageable);
        verify(logRepository, never()).findByClientId(anyString(), any(Pageable.class));
    }

    @Test
    void getLogsByCrudType_ShouldReturnLogsByType() {
        // Given
        Log.CrudType crudType = Log.CrudType.CREATE;
        Page<Log> filteredPage = new PageImpl<>(List.of(log1), pageable, 1);
        when(logRepository.findByCrudType(crudType, pageable)).thenReturn(filteredPage);

        // When
        Page<Log> result = logService.getLogsByCrudType(crudType, pageable);

        // Then
        assertThat(result).isEqualTo(filteredPage);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCrudType()).isEqualTo(crudType);
        verify(logRepository).findByCrudType(crudType, pageable);
    }

    @Test
    void getLogsByCrudTypeAndAgentId_ShouldReturnFilteredLogs() {
        // Given
        Log.CrudType crudType = Log.CrudType.CREATE;
        String agentId = "agent-1";
        Page<Log> filteredPage = new PageImpl<>(List.of(log1), pageable, 1);
        when(logRepository.findByAgentIdAndCrudType(agentId, crudType, pageable)).thenReturn(filteredPage);

        // When
        Page<Log> result = logService.getLogsByCrudTypeAndAgentId(crudType, agentId, pageable);

        // Then
        assertThat(result).isEqualTo(filteredPage);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCrudType()).isEqualTo(crudType);
        assertThat(result.getContent().get(0).getAgentId()).isEqualTo(agentId);
        verify(logRepository).findByAgentIdAndCrudType(agentId, crudType, pageable);
    }

    @Test
    void getLogsByAgentId_WithSearch_ShouldReturnFilteredLogs() {
        // Given
        String agentId = "agent-1";
        String searchQuery = "firstName";
        Page<Log> filteredPage = new PageImpl<>(List.of(log1), pageable, 1);
        when(logRepository.findByAgentIdWithSearch(agentId, searchQuery, pageable)).thenReturn(filteredPage);

        // When
        Page<Log> result = logService.getLogsByAgentId(agentId, searchQuery, pageable);

        // Then
        assertThat(result).isEqualTo(filteredPage);
        assertThat(result.getContent()).hasSize(1);
        verify(logRepository).findByAgentIdWithSearch(agentId, searchQuery, pageable);
        verify(logRepository, never()).findByAgentId(anyString(), any(Pageable.class));
    }

    @Test
    void getLogsByAgentId_WithEmptySearch_ShouldCallFindByAgentId() {
        // Given
        String agentId = "agent-1";
        String searchQuery = "";
        Page<Log> filteredPage = new PageImpl<>(List.of(log1), pageable, 1);
        when(logRepository.findByAgentId(agentId, pageable)).thenReturn(filteredPage);

        // When
        Page<Log> result = logService.getLogsByAgentId(agentId, searchQuery, pageable);

        // Then
        assertThat(result).isEqualTo(filteredPage);
        assertThat(result.getContent()).hasSize(1);
        verify(logRepository).findByAgentId(agentId, pageable);
        verify(logRepository, never()).findByAgentIdWithSearch(anyString(), anyString(), any(Pageable.class));
    }
} 