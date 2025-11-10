package com.pichincha.spfmsaclientecoreservice.repository;

import com.pichincha.spfmsaclientecoreservice.domain.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    @DisplayName("save and find client by ID")
    void saveAndFindClient() {
        // Given
        Client client = new Client();
        client.setName("John Doe");
        client.setIdentification("1234567890");
        client.setPassword("password123");
        client.setStatus(true);
        client.setAge(30);
        client.setGender("M");
        client.setAddress("123 Main St");
        client.setPhone("555-0123");

        // When
        Client savedClient = clientRepository.save(client);

        // Then
        assertThat(savedClient.getPersonId()).isNotNull();
        assertThat(savedClient.getName()).isEqualTo("John Doe");
        assertThat(savedClient.getIdentification()).isEqualTo("1234567890");

        Optional<Client> foundClient = clientRepository.findById(savedClient.getPersonId());
        assertThat(foundClient).isPresent();
        assertThat(foundClient.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("find all clients returns list")
    void findAllClients() {
        // Given
        Client client1 = new Client();
        client1.setName("Client 1");
        client1.setIdentification("ID-001");
        client1.setPassword("pwd1");
        client1.setStatus(true);

        Client client2 = new Client();
        client2.setName("Client 2");
        client2.setIdentification("ID-002");
        client2.setPassword("pwd2");
        client2.setStatus(false);

        clientRepository.save(client1);
        clientRepository.save(client2);

        // When
        var clients = clientRepository.findAll();

        // Then
        assertThat(clients).hasSize(2);
        assertThat(clients).extracting("name").containsExactlyInAnyOrder("Client 1", "Client 2");
    }
}
