package com.pichincha.spfmsaclientecoreservice.service.impl;

import com.pichincha.spfmsaclientecoreservice.domain.Account;
import com.pichincha.spfmsaclientecoreservice.domain.Client;
import com.pichincha.spfmsaclientecoreservice.exception.ResourceNotFoundException;
import com.pichincha.spfmsaclientecoreservice.repository.AccountRepository;
import com.pichincha.spfmsaclientecoreservice.repository.ClientRepository;
import com.pichincha.spfmsaclientecoreservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public Account createAccount(Account account) {
        log.info("Creating account: {}", account.getAccountNumber());

        if (account.getClient() != null && account.getClient().getPersonId() != null) {
            Long clientId = account.getClient().getPersonId();
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> {
                        log.error("Client not found with ID: {}", clientId);
                        return new ResourceNotFoundException("Client not found with id: " + clientId);
                    });
            account.setClient(client);
        }

        Account savedAccount = accountRepository.save(account);
        log.info("Account created with ID: {}", savedAccount.getAccountId());
        return savedAccount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findAccountById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    @Override
    @Transactional
    public Account updateAccount(Long accountId, Account account) {
        log.info("Updating account ID: {}", accountId);

        return accountRepository.findById(accountId)
                .map(existingAccount -> {
                    existingAccount.setAccountNumber(account.getAccountNumber());
                    existingAccount.setAccountType(account.getAccountType());
                    existingAccount.setInitialBalance(account.getInitialBalance());
                    existingAccount.setStatus(account.getStatus());

                    if (account.getClient() != null) {
                        existingAccount.setClient(account.getClient());
                    }

                    Account updatedAccount = accountRepository.save(existingAccount);
                    log.info("Account updated: {}", updatedAccount.getAccountId());
                    return updatedAccount;
                })
                .orElseThrow(() -> {
                    log.error("Account not found with ID: {}", accountId);
                    return new ResourceNotFoundException("Account not found with id: " + accountId);
                });
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        log.info("Soft deleting account ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Account not found with ID: {}", accountId);
                    return new ResourceNotFoundException("Account not found with id: " + accountId);
                });

        // ✅ SOFT DELETE: Cambiar estado a false en lugar de eliminar físicamente
        account.setStatus(false);
        accountRepository.save(account);

        log.info("Account soft deleted (status changed to inactive): {}", accountId);
    }
}
