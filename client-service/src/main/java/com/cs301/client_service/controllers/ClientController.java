package com.cs301.client_service.controllers;

import com.cs301.client_service.dtos.ClientDTO;
import com.cs301.client_service.dtos.ClientListDTO;
import com.cs301.client_service.dtos.VerificationResponseDTO;
import com.cs301.client_service.exceptions.ClientNotFoundException;
import com.cs301.client_service.exceptions.UnauthorizedAccessException;
import com.cs301.client_service.exceptions.VerificationException;
import com.cs301.client_service.mappers.ClientMapper;
import com.cs301.client_service.services.ClientService;
import com.cs301.client_service.utils.JwtAuthorizationUtil;
import com.cs301.client_service.utils.JWTUtil;

import com.cs301.client_service.models.Client;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
    private static final String VERIFIED = "verified";

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    public ClientController(ClientService clientService, ClientMapper clientMapper) {
        this.clientService = clientService;
        this.clientMapper = clientMapper;
    }

    /**
     * Create a new client
     * Requires: authenticated user
     */
    @PostMapping
    public ResponseEntity<ClientDTO> createClient(
            Authentication authentication,
            @Valid @RequestBody ClientDTO clientDTO) {
        
        // Set agentId if not provided
        if (clientDTO.getAgentId() == null || clientDTO.getAgentId().isEmpty()) {
            // If ADMIN, agentId is required; if AGENT, use their ID from JWT
            if (JwtAuthorizationUtil.isAdmin(authentication)) {
                throw new IllegalArgumentException("Admin must provide agentId when creating a client");
            } else if (JwtAuthorizationUtil.isAgent(authentication)) {
                String agentId = JwtAuthorizationUtil.getAgentId(authentication);
                clientDTO.setAgentId(agentId);
            }
        }
        
        var clientModel = clientMapper.toModel(clientDTO);
        var savedClient = clientService.createClient(clientModel);
        var response = clientMapper.toDto(savedClient);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get clients with pagination and optional filtering
     * - For ROLE_AGENT: Only returns clients assigned to the authenticated agent
     * - For ROLE_ADMIN: Returns all clients, can be filtered by agentId
     * - Search query applies to client fields (name, email, etc.)
     */
    @GetMapping
    public ResponseEntity<List<ClientListDTO>> getClients(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String agentId) {
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Client> clientsPage;
        
        // Handle null or empty searchQuery
        String normalizedSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim() : null;
        
        // For agents, always filter by their own agentId (ignore any provided agentId)
        if (JwtAuthorizationUtil.isAgent(authentication)) {
            String agentIdFromJwt = JwtAuthorizationUtil.getAgentId(authentication);
            // Agent request: filter by their own ID
            
            if (normalizedSearchQuery != null) {
                clientsPage = clientService.getClientsWithSearchAndAgentId(agentIdFromJwt, normalizedSearchQuery, pageable);
            } else {
                clientsPage = clientService.getClientsByAgentIdPaginated(agentIdFromJwt, pageable);
            }
        }
        // For admins, allow filtering by provided agentId or show all
        else if (JwtAuthorizationUtil.isAdmin(authentication)) {
            if (agentId != null && !agentId.isEmpty()) {
                // Admin request: filter by provided agent ID
                
                if (normalizedSearchQuery != null) {
                    clientsPage = clientService.getClientsWithSearchAndAgentId(agentId, normalizedSearchQuery, pageable);
                } else {
                    clientsPage = clientService.getClientsByAgentIdPaginated(agentId, pageable);
                }
            } else {
                // Admin request: retrieve all clients
                
                if (normalizedSearchQuery != null) {
                    clientsPage = clientService.getAllClientsPaginated(pageable, normalizedSearchQuery);
                } else {
                    clientsPage = clientService.getAllClientsPaginated(pageable, null);
                }
            }
        } 
        // In case of invalid jwt
        else {
            logger.warn("Unauthorized access attempt to client list");
            throw new UnauthorizedAccessException("Insufficient permissions to access client data");
        }
        
        List<ClientListDTO> clientDTOs = clientMapper.toListDtoList(clientsPage.getContent());
        return ResponseEntity.ok(clientDTOs);
    }

    /**
     * Get a client by ID
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if agentId from JWT subj == client's agentID
     * - ROLE_ADMIN: no requirements
     */
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientDTO> getClient(
            Authentication authentication,
            @PathVariable String clientId) {
        
        Client client = clientService.getClient(clientId);
        
        // Validate if the authenticated user has access to this client
        JwtAuthorizationUtil.validateAgentAccess(authentication, client);
        
        var response = clientMapper.toDto(client);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get clients by agent ID
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if pathvariable agentId == JWT sub agentID
     * - ROLE_ADMIN: no requirements
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ClientListDTO>> getClientsByAgentId(
            Authentication authentication,
            @PathVariable String agentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String searchQuery) {
        
        // For agents, only allow accessing their own clients
        if (JwtAuthorizationUtil.isAgent(authentication)) {
            String agentIdFromJwt = JwtAuthorizationUtil.getAgentId(authentication);
            if (!agentIdFromJwt.equals(agentId)) {
                throw new UnauthorizedAccessException("Agent can only view clients assigned to them");
            }
        }
        // Admin can access any agent's clients, no check needed
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Client> clientsPage;
        
        // Handle null or empty searchQuery
        String normalizedSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim() : null;
        
        if (normalizedSearchQuery != null) {
            clientsPage = clientService.getClientsWithSearchAndAgentId(agentId, normalizedSearchQuery, pageable);
        } else {
            clientsPage = clientService.getClientsByAgentIdPaginated(agentId, pageable);
        }
        
        List<ClientListDTO> clientDTOs = clientMapper.toListDtoList(clientsPage.getContent());
        
        return ResponseEntity.ok(clientDTOs);
    }

    /**
     * Update a client
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if agentId from JWT subj == client's agentID
     * - ROLE_ADMIN: no requirements
     */
    @PutMapping("/{clientId}")
    public ResponseEntity<ClientDTO> updateClient(
            Authentication authentication,
            @PathVariable String clientId,
            @RequestBody ClientDTO clientDTO) {
        
        // Fetch but don't modify the existing client for access check only
        Client existingClient = clientService.getClient(clientId);
        
        // Validate access before update
        JwtAuthorizationUtil.validateAgentAccess(authentication, existingClient);
        
        // Set agentId if provided
        if (clientDTO.getAgentId() != null && !clientDTO.getAgentId().isEmpty()) {
            // If agent tries to change agentId to something other than their own ID, prevent it
            if (JwtAuthorizationUtil.isAgent(authentication)) {
                String authAgentId = JwtAuthorizationUtil.getAgentId(authentication);
                if (!authAgentId.equals(clientDTO.getAgentId())) {
                    throw new UnauthorizedAccessException("Agent cannot assign client to a different agent");
                }
            }
            // Using provided agentId
        } else {
            // Keep the existing agentId
            clientDTO.setAgentId(existingClient.getAgentId());
            // Using existing agentId
        }
        
        // Set the clientId to ensure it's preserved in the update
        clientDTO.setClientId(clientId);
        
        // Pass the DTO directly to the service layer
        // The service will handle the conversion and comparison
        
        var savedClient = clientService.updateClient(clientId, clientDTO);
        
        var response = clientMapper.toDto(savedClient);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a client
     * Requires: authenticated user
     * - ROLE_AGENT: can only access if agentId from JWT subj == client's agentID
     * - ROLE_ADMIN: no requirements
     */
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(
            Authentication authentication,
            @PathVariable String clientId) {
        
        // Validate access before deletion
        Client existingClient = clientService.getClient(clientId);
        JwtAuthorizationUtil.validateAgentAccess(authentication, existingClient);
        
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verify a client to activate their profile
     * This changes their status from PENDING to VERIFIED
     * No authentication needed
     */
    @PostMapping("/{clientId}/verify")
    public ResponseEntity<VerificationResponseDTO> verifyClient(@PathVariable String clientId) {
        try {
            clientService.verifyClient(clientId);
            return ResponseEntity.ok(VerificationResponseDTO.builder().verified(true).build());
        } catch (ClientNotFoundException ex) {
            // Return a consistent response format for the not found case
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(VerificationResponseDTO.builder().verified(false).build());
        }
    }
}
