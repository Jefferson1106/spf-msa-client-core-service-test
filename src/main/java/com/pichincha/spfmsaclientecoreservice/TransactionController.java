package com.pichincha.spfmsaclientecoreservice;

import com.pichincha.spfmsaclientecoreservice.api.TransactionsApi;
import com.pichincha.spfmsaclientecoreservice.domain.Transaction;
import com.pichincha.spfmsaclientecoreservice.exception.ResourceNotFoundException;
import com.pichincha.spfmsaclientecoreservice.model.TransactionDTO;
import com.pichincha.spfmsaclientecoreservice.service.TransactionService;
import com.pichincha.spfmsaclientecoreservice.service.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController implements TransactionsApi {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @Override
    public ResponseEntity<TransactionDTO> createTransaction(TransactionDTO transactionDTO) {
        log.info("Creating transaction of type: {} with amount: {}",
                transactionDTO.getTransactionType(), transactionDTO.getAmount());
        Transaction transaction = transactionMapper.toEntity(transactionDTO);
        Transaction savedTransaction = transactionService.createTransaction(transaction);
        log.info("Transaction created successfully with ID: {}", savedTransaction.getTransactionId());
        return ResponseEntity.ok(transactionMapper.toDto(savedTransaction));
    }

    @Override
    public ResponseEntity<Void> deleteTransaction(Long transactionId) {
        log.info("Deleting transaction with ID: {}", transactionId);
        transactionService.deleteTransaction(transactionId);
        log.info("Transaction deleted successfully with ID: {}", transactionId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        log.info("Fetching all transactions");
        List<Transaction> transactions = transactionService.getAllTransactions();
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(transactionMapper::toDto)
                .toList();
        log.info("Total transactions found: {}", transactionDTOs.size());
        return ResponseEntity.ok(transactionDTOs);
    }

    @Override
    public ResponseEntity<TransactionDTO> findTransactionById(Long transactionId) {
        log.info("Fetching transaction with ID: {}", transactionId);
        Transaction transaction = transactionService.findTransactionById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));
        log.info("Transaction found: Type={}, Amount={}",
                transaction.getTransactionType(), transaction.getAmount());
        return ResponseEntity.ok(transactionMapper.toDto(transaction));
    }

    @Override
    public ResponseEntity<TransactionDTO> updateTransaction(
            Long transactionId,
            TransactionDTO transactionDTO
    ) {
        log.info("Updating transaction with ID: {}", transactionId);
        Transaction transaction = transactionMapper.toEntity(transactionDTO);
        Transaction updatedTransaction = transactionService.updateTransaction(transactionId, transaction);
        log.info("Transaction updated successfully with ID: {}", updatedTransaction.getTransactionId());
        return ResponseEntity.ok(transactionMapper.toDto(updatedTransaction));
    }
}

