package com.pichincha.spfmsaclientecoreservice.repository;

import com.pichincha.spfmsaclientecoreservice.domain.Account;
import com.pichincha.spfmsaclientecoreservice.domain.Client;
import com.pichincha.spfmsaclientecoreservice.domain.enums.AccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    @DisplayName("save and find account with client relationship")
    void saveAndFindAccount() {
        Client client = new Client();
        client.setName("Test Client");
        client.setIdentification("ID-123");
        client.setPassword("pwd");
        client.setStatus(true);

        Client savedClient = clientRepository.save(client);

        Account account = new Account();
        account.setAccountNumber("ACCT-001");
        account.setAccountType(AccountType.SAVINGS);
        account.setInitialBalance(100.0);
        account.setStatus(true);
        account.setClient(savedClient);

        Account saved = accountRepository.save(account);

        assertThat(saved.getAccountId()).isNotNull();
        assertThat(saved.getClient()).isNotNull();
        assertThat(saved.getClient().getPersonId()).isEqualTo(savedClient.getPersonId());

        assertThat(accountRepository.findById(saved.getAccountId())).isPresent();
    }
}

