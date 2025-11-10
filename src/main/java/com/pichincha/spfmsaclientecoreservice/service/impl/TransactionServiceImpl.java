package com.pichincha.spfmsaclientecoreservice.service.impl;

import com.pichincha.spfmsaclientecoreservice.domain.Account;
import com.pichincha.spfmsaclientecoreservice.domain.Transaction;
import com.pichincha.spfmsaclientecoreservice.domain.enums.TransactionType;
import com.pichincha.spfmsaclientecoreservice.exception.InsufficientBalanceException;
import com.pichincha.spfmsaclientecoreservice.exception.ResourceNotFoundException;
import com.pichincha.spfmsaclientecoreservice.repository.AccountRepository;
import com.pichincha.spfmsaclientecoreservice.repository.TransactionRepository;
import com.pichincha.spfmsaclientecoreservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        if (transaction.getAccount() == null || transaction.getAccount().getAccountId() == null) {
            log.error("Account is required for transaction");
            throw new IllegalArgumentException("Account is required for transaction");
        }

        Long accountId = transaction.getAccount().getAccountId();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Account not found with ID: {}", accountId);
                    return new ResourceNotFoundException("Account not found with id: " + accountId);
                });

        Double currentBalance = calculateCurrentBalance(account);
        Double newBalance = calculateNewBalance(currentBalance, transaction);

        transaction.setAccount(account);
        transaction.setDate(LocalDateTime.now());
        transaction.setBalance(newBalance);

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created with ID: {}", savedTransaction.getTransactionId());
        return savedTransaction;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> findTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }

    @Override
    @Transactional
    public Transaction updateTransaction(Long transactionId, Transaction transaction) {
        log.info("Updating transaction ID: {}", transactionId);

        return transactionRepository.findById(transactionId)
                .map(existingTransaction -> {
                    Account account = existingTransaction.getAccount();
                    List<Transaction> allTransactions = transactionRepository
                            .findByAccountOrderByDateAsc(account);

                    existingTransaction.setDate(transaction.getDate());
                    existingTransaction.setTransactionType(transaction.getTransactionType());
                    existingTransaction.setAmount(transaction.getAmount());

                    recalculateBalancesFromTransaction(allTransactions, existingTransaction, account);

                    Transaction updatedTransaction = transactionRepository.save(existingTransaction);
                    log.info("Transaction updated: {}", updatedTransaction.getTransactionId());
                    return updatedTransaction;
                })
                .orElseThrow(() -> {
                    log.error("Transaction not found with ID: {}", transactionId);
                    return new ResourceNotFoundException("Transaction not found with id: " + transactionId);
                });
    }

    /**
     * Recalculates balances from modified transaction onwards
     */
    private void recalculateBalancesFromTransaction(List<Transaction> allTransactions,
                                                    Transaction modifiedTransaction,
                                                    Account account) {
        allTransactions.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));

        boolean foundModified = false;
        Double previousBalance = account.getInitialBalance();

        for (Transaction trans : allTransactions) {
            if (!foundModified && !trans.getTransactionId().equals(modifiedTransaction.getTransactionId())) {
                previousBalance = trans.getBalance();
            } else {
                foundModified = true;
                Double newBalance = calculateNewBalanceForUpdate(previousBalance, trans);
                trans.setBalance(newBalance);
                transactionRepository.save(trans);
                previousBalance = newBalance;
            }
        }
    }

    /**
     * Calculates new balance during update
     */
    private Double calculateNewBalanceForUpdate(Double previousBalance, Transaction transaction) {
        Double amount = transaction.getAmount();
        Double newBalance = previousBalance;

        if (transaction.getTransactionType() == TransactionType.WITHDRAWAL) {
            if (amount > 0) {
                amount = -amount;
                transaction.setAmount(amount);
            }
            newBalance = previousBalance + amount;

            if (newBalance < 0) {
                log.error("Insufficient balance. Previous: {}, Amount: {}, Result: {}",
                        previousBalance, amount, newBalance);
                throw new InsufficientBalanceException("Saldo no disponible al recalcular transacciones");
            }
        } else if (transaction.getTransactionType() == TransactionType.DEPOSIT) {
            if (amount < 0) {
                amount = Math.abs(amount);
                transaction.setAmount(amount);
            }
            newBalance = previousBalance + amount;
        }

        return newBalance;
    }

    @Override
    @Transactional
    public void deleteTransaction(Long transactionId) {
        log.info("Deleting transaction ID: {}", transactionId);

        Transaction transactionToDelete = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.error("Transaction not found with ID: {}", transactionId);
                    return new ResourceNotFoundException("Transaction not found with id: " + transactionId);
                });

        Account account = transactionToDelete.getAccount();
        LocalDateTime deletedTransactionDate = transactionToDelete.getDate();

        List<Transaction> allTransactions = transactionRepository
                .findByAccountOrderByDateAsc(account);

        transactionRepository.deleteById(transactionId);
        recalculateBalancesAfterDeletion(allTransactions, transactionId, deletedTransactionDate, account);
    }

    /**
     * Recalculates balances after transaction deletion
     */
    private void recalculateBalancesAfterDeletion(List<Transaction> allTransactions,
                                                  Long deletedTransactionId,
                                                  LocalDateTime deletedTransactionDate,
                                                  Account account) {
        List<Transaction> subsequentTransactions = allTransactions.stream()
                .filter(t -> !t.getTransactionId().equals(deletedTransactionId))
                .filter(t -> t.getDate().isAfter(deletedTransactionDate) ||
                        t.getDate().isEqual(deletedTransactionDate))
                .sorted((t1, t2) -> t1.getDate().compareTo(t2.getDate()))
                .toList();

        if (subsequentTransactions.isEmpty()) {
            return;
        }

        Double previousBalance = calculateBalanceBeforeTransaction(
                allTransactions,
                subsequentTransactions.get(0),
                deletedTransactionId,
                account
        );

        for (Transaction trans : subsequentTransactions) {
            Double newBalance = calculateNewBalanceForUpdate(previousBalance, trans);
            trans.setBalance(newBalance);
            transactionRepository.save(trans);
            previousBalance = newBalance;
        }
    }

    /**
     * Calculates balance before a specific transaction (excluding deleted one)
     */
    private Double calculateBalanceBeforeTransaction(List<Transaction> allTransactions,
                                                     Transaction targetTransaction,
                                                     Long excludedTransactionId,
                                                     Account account) {
        List<Transaction> previousTransactions = allTransactions.stream()
                .filter(t -> !t.getTransactionId().equals(excludedTransactionId))
                .filter(t -> !t.getTransactionId().equals(targetTransaction.getTransactionId()))
                .filter(t -> t.getDate().isBefore(targetTransaction.getDate()))
                .sorted((t1, t2) -> t1.getDate().compareTo(t2.getDate()))
                .toList();

        if (previousTransactions.isEmpty()) {
            return account.getInitialBalance();
        }

        return previousTransactions.get(previousTransactions.size() - 1).getBalance();
    }

    private Double calculateCurrentBalance(Account account) {
        List<Transaction> transactions = account.getTransactions();

        if (transactions.isEmpty()) {
            return account.getInitialBalance();
        }

        return transactions.get(transactions.size() - 1).getBalance();
    }

    private Double calculateNewBalance(Double currentBalance, Transaction transaction) {
        Double newBalance = currentBalance;

        if (transaction.getTransactionType() == TransactionType.WITHDRAWAL) {
            if (transaction.getAmount() > 0) {
                transaction.setAmount(-transaction.getAmount());
            }
            newBalance = currentBalance + transaction.getAmount();

            if (newBalance < 0) {
                log.error("Insufficient balance. Current: {}, Withdrawal: {}, Result: {}",
                        currentBalance, transaction.getAmount(), newBalance);
                throw new InsufficientBalanceException("Saldo no disponible");
            }
        } else if (transaction.getTransactionType() == TransactionType.DEPOSIT) {
            if (transaction.getAmount() < 0) {
                transaction.setAmount(Math.abs(transaction.getAmount()));
            }
            newBalance = currentBalance + transaction.getAmount();
        }

        return newBalance;
    }
}

