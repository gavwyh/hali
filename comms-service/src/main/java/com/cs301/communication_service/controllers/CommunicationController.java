package com.cs301.communication_service.controllers;

import com.cs301.communication_service.services.impl.CommunicationServiceImpl;
import com.cs301.communication_service.constants.CommunicationStatus;
import com.cs301.communication_service.dtos.*;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/communications")
public class CommunicationController {
    private static final Logger logger = LoggerFactory.getLogger(CommunicationController.class);

    private final CommunicationServiceImpl communicationService;

    public CommunicationController(CommunicationServiceImpl communicationService) {
        this.communicationService = communicationService;
        logger.info("CommunicationController initialised");
    }

    @GetMapping("/{communicationId}/status")
    public ResponseEntity<CommunicationStatusResponse> getCommunicationStatus(@PathVariable String communicationId) {
        CommunicationStatus status = communicationService.getCommunicationStatus(UUID.fromString(communicationId));
        return ResponseEntity.ok(new CommunicationStatusResponse(status.name()));
    }

    // /communications/{agentId}?searchQuery=&page=1&limit=10
    @GetMapping("/{agentId}")
    public ResponseEntity<List<RestCommunicationDTO>> getCommunicationsForAgent(
            @PathVariable("agentId") String agentId,
            @RequestParam(name = "searchQuery", required = false, defaultValue = "") String searchQuery,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {

        // Pass these values to your service for further processing (e.g., search/filter, pagination)
        // List<RestCommunicationDTO> comms = communicationService.getRestCommunicationsDTOs(agentId);
        List<RestCommunicationDTO> comms = communicationService.getRestCommunicationsDTOs(agentId, searchQuery, page, limit);
        // filter out necessary searchquery, page and limit based on comms
        return ResponseEntity.status(HttpStatus.SC_OK).body(comms);
    }


    
}
