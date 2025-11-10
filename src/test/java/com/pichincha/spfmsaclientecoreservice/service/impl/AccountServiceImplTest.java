package com.pichincha.spfmsaclientecoreservice.service.impl;

import com.pichincha.spfmsaclientecoreservice.domain.Account;
import com.pichincha.spfmsaclientecoreservice.domain.Client;
import com.pichincha.spfmsaclientecoreservice.exception.ResourceNotFoundException;
import com.pichincha.spfmsaclientecoreservice.repository.AccountRepository;
import com.pichincha.spfmsaclientecoreservice.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setAccountNumber("ACC-123");
    }

    @Test
    void createAccount_withExistingClient_savesAccountAndReturns() {
        Client client = new Client();
        client.setPersonId(1L);

        Account toSave = new Account();
        toSave.setAccountNumber("ACC-123");
        toSave.setClient(client);

        Client persistedClient = new Client();
        persistedClient.setPersonId(1L);

        Account saved = new Account();
        saved.setAccountId(10L);
        saved.setAccountNumber("ACC-123");
        saved.setClient(persistedClient);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(persistedClient));
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        Account result = accountService.createAccount(toSave);

        assertNotNull(result);
        assertEquals(10L, result.getAccountId());
        assertNotNull(result.getClient());
        verify(clientRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createAccount_withNonExistingClient_throwsResourceNotFound() {
        Client client = new Client();
        client.setPersonId(99L);

        Account toSave = new Account();
        toSave.setAccountNumber("ACC-999");
        toSave.setClient(client);

        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> accountService.createAccount(toSave));

        assertTrue(ex.getMessage().contains("Client not found"));
        verify(clientRepository, times(1)).findById(99L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_withoutClient_savesAccountDirectly() {
        // Given
        Account toSave = new Account();
        toSave.setAccountNumber("ACC-NO-CLIENT");

        Account saved = new Account();
        saved.setAccountId(20L);
        saved.setAccountNumber("ACC-NO-CLIENT");

        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        // When
        Account result = accountService.createAccount(toSave);

        // Then
        assertNotNull(result);
        assertEquals(20L, result.getAccountId());
        verify(clientRepository, never()).findById(any());
        verify(accountRepository, times(1)).save(toSave);
    }

    @Test
    void getAllAccounts_returnsAllAccounts() {
        // Given
        List<Account> accounts = Arrays.asList(
                createTestAccount(1L, "ACC-001"),
                createTestAccount(2L, "ACC-002")
        );
        when(accountRepository.findAll()).thenReturn(accounts);

        // When
        List<Account> result = accountService.getAllAccounts();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAccountNumber()).isEqualTo("ACC-001");
        assertThat(result.get(1).getAccountNumber()).isEqualTo("ACC-002");
        verify(accountRepository, times(1)).findAll();
    }

    @Test
    void findAccountById_existingAccount_returnsOptionalWithAccount() {
        // Given
        Account testAccount = createTestAccount(1L, "ACC-FIND");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When
        Optional<Account> result = accountService.findAccountById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAccountNumber()).isEqualTo("ACC-FIND");
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void findAccountById_nonExistingAccount_returnsEmptyOptional() {
        // Given
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Account> result = accountService.findAccountById(99L);

        // Then
        assertThat(result).isEmpty();
        verify(accountRepository, times(1)).findById(99L);
    }

    @Test
    void updateAccount_existingAccount_updatesAndReturns() {
        // Given
        Account existingAccount = createTestAccount(1L, "ACC-OLD");
        Account updateData = new Account();
        updateData.setAccountNumber("ACC-UPDATED");
        updateData.setInitialBalance(2000.0);
        updateData.setStatus(false);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        // When
        Account result = accountService.updateAccount(1L, updateData);

        // Then
        assertThat(result.getAccountNumber()).isEqualTo("ACC-UPDATED");
        assertThat(result.getInitialBalance()).isEqualTo(2000.0);
        assertThat(result.getStatus()).isFalse();
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(existingAccount);
    }

    @Test
    void updateAccount_nonExistingAccount_throwsResourceNotFoundException() {
        // Given
        Account updateData = new Account();
        updateData.setAccountNumber("ACC-UPDATE-FAIL");

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> accountService.updateAccount(99L, updateData));

        assertThat(exception.getMessage()).contains("Account not found with id: 99");
        verify(accountRepository, times(1)).findById(99L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void deleteAccount_existingAccount_deletesSuccessfully() {
        // Given
        when(accountRepository.existsById(1L)).thenReturn(true);

        // When
        accountService.deleteAccount(1L);

        // Then
        verify(accountRepository, times(1)).existsById(1L);
        verify(accountRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteAccount_nonExistingAccount_throwsResourceNotFoundException() {
        // Given
        when(accountRepository.existsById(99L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> accountService.deleteAccount(99L));

        assertThat(exception.getMessage()).contains("Account not found with id: 99");
        verify(accountRepository, times(1)).existsById(99L);
        verify(accountRepository, never()).deleteById(any());
    }

    private Account createTestAccount(Long id, String accountNumber) {
        Account account = new Account();
        account.setAccountId(id);
        account.setAccountNumber(accountNumber);
        account.setInitialBalance(1000.0);
        account.setStatus(true);
        return account;
    }
}
