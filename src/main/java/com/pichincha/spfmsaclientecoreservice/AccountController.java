package com.pichincha.spfmsaclientecoreservice;

import com.pichincha.spfmsaclientecoreservice.api.AccountsApi;
import com.pichincha.spfmsaclientecoreservice.domain.Account;
import com.pichincha.spfmsaclientecoreservice.exception.ResourceNotFoundException;
import com.pichincha.spfmsaclientecoreservice.model.AccountDTO;
import com.pichincha.spfmsaclientecoreservice.service.AccountService;
import com.pichincha.spfmsaclientecoreservice.service.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AccountController implements AccountsApi {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @Override
    public ResponseEntity<AccountDTO> createAccount(AccountDTO accountDTO) {
        log.info("Creating account with number: {}", accountDTO.getAccountNumber());
        Account account = accountMapper.toEntity(accountDTO);
        Account savedAccount = accountService.createAccount(account);
        log.info("Account created successfully with ID: {}", savedAccount.getAccountId());
        return ResponseEntity.ok(accountMapper.toDto(savedAccount));
    }

    @Override
    public ResponseEntity<Void> deleteAccount(Long accountId) {
        log.info("Deleting account with ID: {}", accountId);
        accountService.deleteAccount(accountId);
        log.info("Account deleted successfully with ID: {}", accountId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        log.info("Fetching all accounts");
        List<Account> accounts = accountService.getAllAccounts();
        List<AccountDTO> accountDTOs = accounts.stream()
                .map(accountMapper::toDto)
                .toList();
        log.info("Total accounts found: {}", accountDTOs.size());
        return ResponseEntity.ok(accountDTOs);
    }

    @Override
    public ResponseEntity<AccountDTO> findAccountById(Long accountId) {
        log.info("Fetching account with ID: {}", accountId);
        Account account = accountService.findAccountById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        log.info("Account found: {}", account.getAccountNumber());
        return ResponseEntity.ok(accountMapper.toDto(account));
    }

    @Override
    public ResponseEntity<AccountDTO> updateAccount(Long accountId, AccountDTO accountDTO) {
        log.info("Updating account with ID: {}", accountId);
        Account account = accountMapper.toEntity(accountDTO);
        Account updatedAccount = accountService.updateAccount(accountId, account);
        log.info("Account updated successfully with ID: {}", updatedAccount.getAccountId());
        return ResponseEntity.ok(accountMapper.toDto(updatedAccount));
    }
}
