package com.pichincha.spfmsaclientecoreservice.service.impl;

import com.pichincha.spfmsaclientecoreservice.domain.Transaction;
import com.pichincha.spfmsaclientecoreservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeEach
    void setUp() {
        transaction1 = new Transaction();
        transaction1.setTransactionId(1L);
        transaction1.setAmount(100.0);
        transaction1.setBalance(1100.0);
        transaction1.setDate(LocalDateTime.now().minusDays(1));

        transaction2 = new Transaction();
        transaction2.setTransactionId(2L);
        transaction2.setAmount(-50.0);
        transaction2.setBalance(1050.0);
        transaction2.setDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("generateAccountStatement - returns transactions for client in date range")
    void generateAccountStatement_returnsTransactionsForClientInDateRange() {
        // Given
        Long clientId = 1L;
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<Transaction> expectedTransactions = Arrays.asList(transaction1, transaction2);

        when(transactionRepository.findByClientAndDateRange(
                anyLong(), 
                any(LocalDateTime.class), 
                any(LocalDateTime.class)
        )).thenReturn(expectedTransactions);

        // When
        List<Transaction> result = reportService.generateAccountStatement(clientId, startDate, endDate);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(transaction1, transaction2);
        
        verify(transactionRepository, times(1)).findByClientAndDateRange(
                eq(clientId),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("generateAccountStatement - returns empty list when no transactions found")
    void generateAccountStatement_returnsEmptyListWhenNoTransactionsFound() {
        // Given
        Long clientId = 1L;
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(transactionRepository.findByClientAndDateRange(
                anyLong(), 
                any(LocalDateTime.class), 
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        // When
        List<Transaction> result = reportService.generateAccountStatement(clientId, startDate, endDate);

        // Then
        assertThat(result).isEmpty();
        verify(transactionRepository, times(1)).findByClientAndDateRange(
                eq(clientId),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("generateAccountStatement - calls repository with correct date time ranges")
    void generateAccountStatement_callsRepositoryWithCorrectDateTimeRanges() {
        // Given
        Long clientId = 1L;
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);

        when(transactionRepository.findByClientAndDateRange(
                anyLong(), 
                any(LocalDateTime.class), 
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        // When
        reportService.generateAccountStatement(clientId, startDate, endDate);

        // Then
        verify(transactionRepository, times(1)).findByClientAndDateRange(
                eq(clientId),
                eq(LocalDateTime.of(2023, 1, 1, 0, 0, 0)),
                eq(LocalDateTime.of(2023, 1, 31, 23, 59, 59, 999999999))
        );
    }
}
