package com.pichincha.spfmsaclientecoreservice.service.impl;

import com.pichincha.spfmsaclientecoreservice.domain.Transaction;
import com.pichincha.spfmsaclientecoreservice.repository.TransactionRepository;
import com.pichincha.spfmsaclientecoreservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> generateAccountStatement(
            Long clientId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info("Generating report for client ID: {} from {} to {}", clientId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Transaction> transactions = transactionRepository.findByClientAndDateRange(
                clientId,
                startDateTime,
                endDateTime
        );

        log.info("Report generated: {} transactions found", transactions.size());
        return transactions;
    }
}

