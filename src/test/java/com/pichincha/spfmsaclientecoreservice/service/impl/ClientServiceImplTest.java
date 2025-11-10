package com.pichincha.spfmsaclientecoreservice.service.impl;

import com.pichincha.spfmsaclientecoreservice.domain.Client;
import com.pichincha.spfmsaclientecoreservice.exception.ResourceNotFoundException;
import com.pichincha.spfmsaclientecoreservice.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setPersonId(1L);
        testClient.setName("John Doe");
        testClient.setIdentification("1234567890");
        testClient.setPassword("password123");
        testClient.setStatus(true);
        testClient.setAge(30);
        testClient.setGender("M");
        testClient.setAddress("123 Main St");
        testClient.setPhone("555-0123");
    }

    @Test
    @DisplayName("createClient - saves and returns client")
    void createClient_savesAndReturnsClient() {
        // Given
        Client clientToSave = new Client();
        clientToSave.setName("John Doe");
        clientToSave.setIdentification("1234567890");

        Client savedClient = new Client();
        savedClient.setPersonId(1L);
        savedClient.setName("John Doe");
        savedClient.setIdentification("1234567890");

        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        // When
        Client result = clientService.createClient(clientToSave);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPersonId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        verify(clientRepository, times(1)).save(clientToSave);
    }

    @Test
    @DisplayName("getAllClients - returns list of clients")
    void getAllClients_returnsListOfClients() {
        // Given
        List<Client> clients = Arrays.asList(testClient, new Client());
        when(clientRepository.findAll()).thenReturn(clients);

        // When
        List<Client> result = clientService.getAllClients();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testClient, clients.get(1));
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findClientById - existing client returns Optional with client")
    void findClientById_existingClient_returnsOptionalWithClient() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));

        // When
        Optional<Client> result = clientService.findClientById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testClient);
        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findClientById - non-existing client returns empty Optional")
    void findClientById_nonExistingClient_returnsEmptyOptional() {
        // Given
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Client> result = clientService.findClientById(99L);

        // Then
        assertThat(result).isEmpty();
        verify(clientRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("updateClient - existing client updates and returns client")
    void updateClient_existingClient_updatesAndReturnsClient() {
        // Given
        Client updatedClient = new Client();
        updatedClient.setName("Jane Doe");
        updatedClient.setIdentification("0987654321");
        updatedClient.setStatus(false);

        Client existingClient = new Client();
        existingClient.setPersonId(1L);
        existingClient.setName("John Doe");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        when(clientRepository.save(any(Client.class))).thenReturn(existingClient);

        // When
        Client result = clientService.updateClient(1L, updatedClient);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Jane Doe");
        assertThat(result.getIdentification()).isEqualTo("0987654321");
        assertThat(result.getStatus()).isFalse();
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).save(existingClient);
    }

    @Test
    @DisplayName("updateClient - non-existing client throws ResourceNotFoundException")
    void updateClient_nonExistingClient_throwsResourceNotFoundException() {
        // Given
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> clientService.updateClient(99L, testClient));

        assertThat(exception.getMessage()).contains("Client not found with id: 99");
        verify(clientRepository, times(1)).findById(99L);
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteClient - existing client deletes successfully")
    void deleteClient_existingClient_deletesSuccessfully() {
        // Given
        when(clientRepository.existsById(1L)).thenReturn(true);

        // When
        clientService.deleteClient(1L);

        // Then
        verify(clientRepository, times(1)).existsById(1L);
        verify(clientRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteClient - non-existing client throws ResourceNotFoundException")
    void deleteClient_nonExistingClient_throwsResourceNotFoundException() {
        // Given
        when(clientRepository.existsById(99L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> clientService.deleteClient(99L));

        assertThat(exception.getMessage()).contains("Client not found with id: 99");
        verify(clientRepository, times(1)).existsById(99L);
        verify(clientRepository, never()).deleteById(anyLong());
    }
}
