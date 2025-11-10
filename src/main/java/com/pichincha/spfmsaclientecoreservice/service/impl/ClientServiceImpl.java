package com.pichincha.spfmsaclientecoreservice.service.impl;

import com.pichincha.spfmsaclientecoreservice.domain.Client;
import com.pichincha.spfmsaclientecoreservice.exception.ResourceNotFoundException;
import com.pichincha.spfmsaclientecoreservice.repository.ClientRepository;
import com.pichincha.spfmsaclientecoreservice.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public Client createClient(Client client) {
        log.info("Creating client: {}", client.getIdentification());
        Client savedClient = clientRepository.save(client);
        log.info("Client created with ID: {}", savedClient.getPersonId());
        return savedClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> findClientById(Long clientId) {
        return clientRepository.findById(clientId);
    }

    @Override
    @Transactional
    public Client updateClient(Long clientId, Client client) {
        log.info("Updating client ID: {}", clientId);

        return clientRepository.findById(clientId)
                .map(existingClient -> {
                    existingClient.setName(client.getName());
                    existingClient.setGender(client.getGender());
                    existingClient.setAge(client.getAge());
                    existingClient.setIdentification(client.getIdentification());
                    existingClient.setAddress(client.getAddress());
                    existingClient.setPhone(client.getPhone());
                    existingClient.setPassword(client.getPassword());
                    existingClient.setStatus(client.getStatus());

                    Client updatedClient = clientRepository.save(existingClient);
                    log.info("Client updated: {}", updatedClient.getPersonId());
                    return updatedClient;
                })
                .orElseThrow(() -> {
                    log.error("Client not found with ID: {}", clientId);
                    return new ResourceNotFoundException("Client not found with id: " + clientId);
                });
    }

    @Override
    @Transactional
    public void deleteClient(Long clientId) {
        log.info("Soft deleting client ID: {}", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> {
                    log.error("Client not found with ID: {}", clientId);
                    return new ResourceNotFoundException("Client not found with id: " + clientId);
                });

        // ✅ SOFT DELETE: Cambiar estado a false en lugar de eliminar físicamente
        client.setStatus(false);
        clientRepository.save(client);

        log.info("Client soft deleted (status changed to inactive): {}", clientId);
    }
}

