package com.pichincha.spfmsaclientecoreservice.service;

import com.pichincha.spfmsaclientecoreservice.domain.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionService {

    Transaction createTransaction(Transaction transaction);

    List<Transaction> getAllTransactions();

    Optional<Transaction> findTransactionById(Long transactionId);

    Transaction updateTransaction(Long transactionId, Transaction transaction);

    void deleteTransaction(Long transactionId);
}
