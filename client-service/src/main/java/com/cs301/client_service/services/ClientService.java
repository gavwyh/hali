package com.cs301.client_service.services;

import com.cs301.client_service.models.Client;
import com.cs301.client_service.dtos.ClientDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ClientService {
    Client createClient(Client client);
    Client getClient(String clientId);
    List<Client> getAllClients();
    Page<Client> getAllClientsPaginated(Pageable pageable, String search);
    List<Client> getClientsByAgentId(String agentId);
    Page<Client> getClientsByAgentIdPaginated(String agentId, Pageable pageable);
    Page<Client> getClientsWithSearchAndAgentId(String agentId, String searchQuery, Pageable pageable);
    Client updateClient(String clientId, ClientDTO clientDTO);
    void deleteClient(String clientId);
    void verifyClient(String clientId);
}
