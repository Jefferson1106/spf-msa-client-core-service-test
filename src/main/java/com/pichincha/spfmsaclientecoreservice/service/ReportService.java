package com.pichincha.spfmsaclientecoreservice.service;

import com.pichincha.spfmsaclientecoreservice.domain.Transaction;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    List<Transaction> generateAccountStatement(Long clientId, LocalDate startDate, LocalDate endDate);
}
