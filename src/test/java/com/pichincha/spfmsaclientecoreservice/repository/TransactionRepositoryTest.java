package com.pichincha.spfmsaclientecoreservice.repository;

import com.pichincha.spfmsaclientecoreservice.domain.Account;
import com.pichincha.spfmsaclientecoreservice.domain.Client;
import com.pichincha.spfmsaclientecoreservice.domain.Transaction;
import com.pichincha.spfmsaclientecoreservice.domain.enums.AccountType;
import com.pichincha.spfmsaclientecoreservice.domain.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    @DisplayName("save and find transaction with account relationship")
    void saveAndFindTransaction() {
        // Given - Create client and account first
        Client client = new Client();
        client.setName("Test Client");
        client.setIdentification("ID-123");
        client.setPassword("pwd");
        client.setStatus(true);
        Client savedClient = clientRepository.save(client);

        Account account = new Account();
        account.setAccountNumber("ACCT-001");
        account.setAccountType(AccountType.SAVINGS);
        account.setInitialBalance(1000.0);
        account.setStatus(true);
        account.setClient(savedClient);
        Account savedAccount = accountRepository.save(account);

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(500.0);
        transaction.setBalance(1500.0);
        transaction.setDate(LocalDateTime.now());
        transaction.setAccount(savedAccount);

        // When
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Then
        assertThat(savedTransaction.getTransactionId()).isNotNull();
        assertThat(savedTransaction.getAmount()).isEqualTo(500.0);
        assertThat(savedTransaction.getBalance()).isEqualTo(1500.0);
        assertThat(savedTransaction.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(savedTransaction.getAccount()).isNotNull();
        assertThat(savedTransaction.getAccount().getAccountId()).isEqualTo(savedAccount.getAccountId());

        assertThat(transactionRepository.findById(savedTransaction.getTransactionId())).isPresent();
    }

    @Test
    @DisplayName("find all transactions by account")
    void findTransactionsByAccount() {
        // Given - Create client and account
        Client client = new Client();
        client.setName("Test Client");
        client.setIdentification("ID-456");
        client.setPassword("pwd");
        client.setStatus(true);
        Client savedClient = clientRepository.save(client);

        Account account = new Account();
        account.setAccountNumber("ACCT-002");
        account.setAccountType(AccountType.CHECKING);
        account.setInitialBalance(2000.0);
        account.setStatus(true);
        account.setClient(savedClient);
        Account savedAccount = accountRepository.save(account);

        // Create multiple transactions
        Transaction deposit = new Transaction();
        deposit.setTransactionType(TransactionType.DEPOSIT);
        deposit.setAmount(300.0);
        deposit.setBalance(2300.0);
        deposit.setDate(LocalDateTime.now().minusDays(1));
        deposit.setAccount(savedAccount);

        Transaction withdrawal = new Transaction();
        withdrawal.setTransactionType(TransactionType.WITHDRAWAL);
        withdrawal.setAmount(-100.0);
        withdrawal.setBalance(2200.0);
        withdrawal.setDate(LocalDateTime.now());
        withdrawal.setAccount(savedAccount);

        transactionRepository.save(deposit);
        transactionRepository.save(withdrawal);

        // When
        List<Transaction> transactions = transactionRepository.findAll();

        // Then
        assertThat(transactions).hasSize(2);
        assertThat(transactions).extracting("transactionType")
                .containsExactlyInAnyOrder(TransactionType.DEPOSIT, TransactionType.WITHDRAWAL);
    }
}
