package com.pichincha.spfmsaclientecoreservice;

import com.pichincha.spfmsaclientecoreservice.api.ClientsApi;
import com.pichincha.spfmsaclientecoreservice.domain.Client;
import com.pichincha.spfmsaclientecoreservice.exception.ResourceNotFoundException;
import com.pichincha.spfmsaclientecoreservice.model.ClientDTO;
import com.pichincha.spfmsaclientecoreservice.service.ClientService;
import com.pichincha.spfmsaclientecoreservice.service.mapper.ClientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ClientController implements ClientsApi {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    @Override
    public ResponseEntity<ClientDTO> createClient(ClientDTO clientDTO) {
        log.info("Creating client with identification: {}", clientDTO.getIdentification());
        Client client = clientMapper.toEntity(clientDTO);
        Client savedClient = clientService.createClient(client);
        log.info("Client created successfully with ID: {}", savedClient.getPersonId());
        return ResponseEntity.ok(clientMapper.toDto(savedClient));
    }

    @Override
    public ResponseEntity<Void> deleteClient(Long clientId) {
        log.info("Deleting client with ID: {}", clientId);
        clientService.deleteClient(clientId);
        log.info("Client deleted successfully with ID: {}", clientId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        log.info("Fetching all clients");
        List<Client> clients = clientService.getAllClients();
        List<ClientDTO> clientDTOs = clients.stream()
                .map(clientMapper::toDto)
                .toList();
        log.info("Total clients found: {}", clientDTOs.size());
        return ResponseEntity.ok(clientDTOs);
    }

    @Override
    public ResponseEntity<ClientDTO> findClientById(Long clientId) {
        log.info("Fetching client with ID: {}", clientId);
        Client client = clientService.findClientById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        log.info("Client found: {}", client.getName());
        return ResponseEntity.ok(clientMapper.toDto(client));
    }

    @Override
    public ResponseEntity<ClientDTO> updateClient(Long clientId, ClientDTO clientDTO) {
        log.info("Updating client with ID: {}", clientId);
        Client client = clientMapper.toEntity(clientDTO);
        Client updatedClient = clientService.updateClient(clientId, client);
        log.info("Client updated successfully with ID: {}", updatedClient.getPersonId());
        return ResponseEntity.ok(clientMapper.toDto(updatedClient));
    }
}

