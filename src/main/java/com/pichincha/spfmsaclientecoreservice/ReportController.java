package com.pichincha.spfmsaclientecoreservice;

import com.pichincha.spfmsaclientecoreservice.api.ReportsApi;
import com.pichincha.spfmsaclientecoreservice.model.ReportDTO;
import com.pichincha.spfmsaclientecoreservice.model.ReportResponseDTO;
import com.pichincha.spfmsaclientecoreservice.service.PdfReportService;
import com.pichincha.spfmsaclientecoreservice.service.ReportService;
import com.pichincha.spfmsaclientecoreservice.service.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReportController implements ReportsApi {

    private final ReportService reportService;
    private final ReportMapper reportMapper;
    private final PdfReportService pdfReportService;

    @Override
    public ResponseEntity<List<ReportDTO>> generateAccountStatement(
            Long clientId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info("Generating account statement for client ID: {} from {} to {}",
                clientId, startDate, endDate);

        List<com.pichincha.spfmsaclientecoreservice.domain.Transaction> transactions =
                reportService.generateAccountStatement(clientId, startDate, endDate);

        BigDecimal totalDebits = transactions.stream()
                .filter(transaction -> transaction.getAmount() < 0)
                .map(transaction -> BigDecimal.valueOf(Math.abs(transaction.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = transactions.stream()
                .filter(transaction -> transaction.getAmount() > 0)
                .map(transaction -> BigDecimal.valueOf(transaction.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ReportDTO> reports = transactions.stream()
                .map(transaction -> reportMapper.toDto(transaction, totalDebits, totalCredits))
                .toList();

        log.info("Account statement generated: {} transactions, Total Debits: {}, Total Credits: {}",
                reports.size(), totalDebits, totalCredits);

        return ResponseEntity.ok(reports);
    }

    @Override
    public ResponseEntity<ReportResponseDTO> generateAccountStatementWithPdf(
            Long clientId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info("Generating account statement with PDF for client ID: {} from {} to {}",
                clientId, startDate, endDate);

        List<com.pichincha.spfmsaclientecoreservice.domain.Transaction> transactions =
                reportService.generateAccountStatement(clientId, startDate, endDate);

        BigDecimal totalDebits = transactions.stream()
                .filter(transaction -> transaction.getAmount() < 0)
                .map(transaction -> BigDecimal.valueOf(Math.abs(transaction.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = transactions.stream()
                .filter(transaction -> transaction.getAmount() > 0)
                .map(transaction -> BigDecimal.valueOf(transaction.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ReportDTO> reports = transactions.stream()
                .map(transaction -> reportMapper.toDto(transaction, totalDebits, totalCredits))
                .toList();

        log.info("Generating PDF report with {} transactions", reports.size());
        byte[] pdfBytes = pdfReportService.generateAccountStatementPdf(reports);
        String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

        ReportResponseDTO response = new ReportResponseDTO();
        response.setReportJson(reports);
        response.setPdfBase64(pdfBase64);

        log.info("PDF report generated successfully, size: {} bytes", pdfBytes.length);

        return ResponseEntity.ok(response);
    }
}

