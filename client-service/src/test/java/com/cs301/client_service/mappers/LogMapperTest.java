package com.cs301.client_service.mappers;

import com.cs301.client_service.dtos.LogDTO;
import com.cs301.client_service.models.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@org.junit.jupiter.api.Disabled("Disabled for unit testing")
public class LogMapperTest {

    @Autowired
    private LogMapper logMapper;

    private Log createLog;
    private Log readLog;
    private Log updateLog;
    private Log deleteLog;
    private Log verificationLog;
    private Log accountLog;

    @BeforeEach
    void setUp() {
        // Create sample logs with the new format
        createLog = Log.builder()
                .id("create-log-id")
                .clientId("client-id-1")
                .agentId("agent-id-1")
                .crudType(Log.CrudType.CREATE)
                .attributeName("client-id-1")
                .beforeValue("")
                .afterValue("{\"clientId\":\"client-id-1\",\"firstName\":\"John\",\"lastName\":\"Doe\"}")
                .dateTime(LocalDateTime.now())
                .build();

        readLog = Log.builder()
                .id("read-log-id")
                .clientId("client-id-2")
                .agentId("agent-id-1")
                .crudType(Log.CrudType.READ)
                .attributeName("client-id-2")
                .beforeValue("{\"clientId\":\"client-id-2\",\"firstName\":\"Jane\",\"lastName\":\"Smith\"}")
                .afterValue("{\"clientId\":\"client-id-2\",\"firstName\":\"Jane\",\"lastName\":\"Smith\"}")
                .dateTime(LocalDateTime.now())
                .build();

        updateLog = Log.builder()
                .id("update-log-id")
                .clientId("client-id-3")
                .agentId("agent-id-2")
                .crudType(Log.CrudType.UPDATE)
                .attributeName("firstName|lastName|address")
                .beforeValue("Michael|Wong|123 Main St")
                .afterValue("Michael James|Wong|456 Oak Ave")
                .dateTime(LocalDateTime.now())
                .build();

        deleteLog = Log.builder()
                .id("delete-log-id")
                .clientId("client-id-4")
                .agentId("agent-id-2")
                .crudType(Log.CrudType.DELETE)
                .attributeName("client-id-4")
                .beforeValue("{\"clientId\":\"client-id-4\",\"firstName\":\"Robert\",\"lastName\":\"Johnson\"}")
                .afterValue("")
                .dateTime(LocalDateTime.now())
                .build();

        verificationLog = Log.builder()
                .id("verification-log-id")
                .clientId("client-id-5")
                .agentId("agent-id-3")
                .crudType(Log.CrudType.UPDATE)
                .attributeName("verificationStatus")
                .beforeValue("PENDING")
                .afterValue("VERIFIED")
                .dateTime(LocalDateTime.now())
                .build();

        accountLog = Log.builder()
                .id("account-log-id")
                .clientId("client-id-6")
                .agentId("agent-id-3")
                .crudType(Log.CrudType.CREATE)
                .attributeName("client-id-6")
                .beforeValue("")
                .afterValue("{\"accountId\":\"account-id-1\",\"accountType\":\"SAVINGS\",\"accountStatus\":\"ACTIVE\"}")
                .dateTime(LocalDateTime.now())
                .build();
    }

    @Test
    void testMapCreateLogToDTO() {
        // Act
        LogDTO dto = logMapper.toDTO(createLog);

        // Assert
        assertEquals(createLog.getId(), dto.getId());
        assertEquals(createLog.getClientId(), dto.getClientId());
        assertNotNull(dto.getClientName());
        assertEquals(createLog.getAgentId(), dto.getAgentId());
        assertEquals(createLog.getDateTime().toString(), dto.getDateTime());
    }

    @Test
    void testMapReadLogToDTO() {
        // Act
        LogDTO dto = logMapper.toDTO(readLog);

        // Assert
        assertEquals(readLog.getId(), dto.getId());
        assertEquals(readLog.getClientId(), dto.getClientId());
        assertNotNull(dto.getClientName());
        assertEquals(readLog.getAgentId(), dto.getAgentId());
        assertEquals(readLog.getDateTime().toString(), dto.getDateTime());
    }

    @Test
    void testMapUpdateLogToDTO() {
        // Act
        LogDTO dto = logMapper.toDTO(updateLog);

        // Assert
        assertEquals(updateLog.getId(), dto.getId());
        assertEquals(updateLog.getClientId(), dto.getClientId());
        assertNotNull(dto.getClientName());
        assertEquals(updateLog.getAgentId(), dto.getAgentId());
        assertEquals(updateLog.getDateTime().toString(), dto.getDateTime());
        assertEquals(updateLog.getAttributeName(), dto.getAttributeName());
        assertEquals(updateLog.getBeforeValue(), dto.getBeforeValue());
        assertEquals(updateLog.getAfterValue(), dto.getAfterValue());
    }

    @Test
    void testMapDeleteLogToDTO() {
        // Act
        LogDTO dto = logMapper.toDTO(deleteLog);

        // Assert
        assertEquals(deleteLog.getId(), dto.getId());
        assertEquals(deleteLog.getClientId(), dto.getClientId());
        assertNotNull(dto.getClientName());
        assertEquals(deleteLog.getAgentId(), dto.getAgentId());
        assertEquals(deleteLog.getDateTime().toString(), dto.getDateTime());
    }

    @Test
    void testMapVerificationLogToDTO() {
        // Act
        LogDTO dto = logMapper.toDTO(verificationLog);

        // Assert
        assertEquals(verificationLog.getId(), dto.getId());
        assertEquals(verificationLog.getClientId(), dto.getClientId());
        assertNotNull(dto.getClientName());
        assertEquals(verificationLog.getAgentId(), dto.getAgentId());
        assertEquals(verificationLog.getDateTime().toString(), dto.getDateTime());
        assertEquals(verificationLog.getAttributeName(), dto.getAttributeName());
        assertEquals(verificationLog.getBeforeValue(), dto.getBeforeValue());
        assertEquals(verificationLog.getAfterValue(), dto.getAfterValue());
    }

    @Test
    void testMapAccountLogToDTO() {
        // Act
        LogDTO dto = logMapper.toDTO(accountLog);

        // Assert
        assertEquals(accountLog.getId(), dto.getId());
        assertEquals(accountLog.getClientId(), dto.getClientId());
        assertNotNull(dto.getClientName());
        assertEquals(accountLog.getAgentId(), dto.getAgentId());
        assertEquals(accountLog.getDateTime().toString(), dto.getDateTime());
    }

    @Test
    void testMapListOfLogsToListOfDTOs() {
        // Arrange
        List<Log> logs = Arrays.asList(createLog, readLog, updateLog, deleteLog, verificationLog, accountLog);

        // Act
        List<LogDTO> dtos = logMapper.toDTOList(logs);

        // Assert
        assertEquals(logs.size(), dtos.size());
        for (int i = 0; i < logs.size(); i++) {
            assertEquals(logs.get(i).getId(), dtos.get(i).getId());
            assertEquals(logs.get(i).getClientId(), dtos.get(i).getClientId());
            assertNotNull(dtos.get(i).getClientName());
            assertEquals(logs.get(i).getAgentId(), dtos.get(i).getAgentId());
            assertEquals(logs.get(i).getDateTime().toString(), dtos.get(i).getDateTime());
        }
    }
}
