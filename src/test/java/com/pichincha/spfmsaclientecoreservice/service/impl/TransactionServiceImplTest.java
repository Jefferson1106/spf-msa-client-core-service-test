package com.pichincha.spfmsaclientecoreservice.service.impl;

import com.pichincha.spfmsaclientecoreservice.domain.Account;
import com.pichincha.spfmsaclientecoreservice.domain.Transaction;
import com.pichincha.spfmsaclientecoreservice.domain.enums.TransactionType;
import com.pichincha.spfmsaclientecoreservice.exception.InsufficientBalanceException;
import com.pichincha.spfmsaclientecoreservice.exception.ResourceNotFoundException;
import com.pichincha.spfmsaclientecoreservice.repository.AccountRepository;
import com.pichincha.spfmsaclientecoreservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setAccountId(1L);
        account.setInitialBalance(100.0);
        account.setTransactions(new ArrayList<>());
    }

    @Test
    void createTransaction_deposit_updatesBalanceAndSaves() {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setAmount(50.0);
        tx.setTransactionType(TransactionType.DEPOSIT);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setTransactionId(100L);
            return t;
        });

        Transaction saved = transactionService.createTransaction(tx);

        assertNotNull(saved);
        assertEquals(100L, saved.getTransactionId());
        assertEquals(150.0, saved.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void createTransaction_withdraw_insufficientBalance_throws() {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setAmount(200.0);
        tx.setTransactionType(TransactionType.WITHDRAWAL);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(InsufficientBalanceException.class, () -> transactionService.createTransaction(tx));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_accountNotFound_throwsResourceNotFound() {
        Transaction tx = new Transaction();
        Account a = new Account();
        a.setAccountId(99L);
        tx.setAccount(a);

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.createTransaction(tx));
        verify(transactionRepository, never()).save(any());
    }
}

