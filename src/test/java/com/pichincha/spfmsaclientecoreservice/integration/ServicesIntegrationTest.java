package com.pichincha.spfmsaclientecoreservice.integration;

import com.pichincha.spfmsaclientecoreservice.service.AccountService;
import com.pichincha.spfmsaclientecoreservice.service.ClientService;
import com.pichincha.spfmsaclientecoreservice.service.TransactionService;
import com.pichincha.spfmsaclientecoreservice.service.ReportService;
import com.pichincha.spfmsaclientecoreservice.service.PdfReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ServicesIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private PdfReportService pdfReportService;

    @Test
    @DisplayName("Integration test - All services are properly injected")
    void allServicesAreProperlyInjected() {
        // This test ensures that all service beans are correctly configured and injected
        assertThat(accountService).isNotNull();
        assertThat(clientService).isNotNull();
        assertThat(transactionService).isNotNull();
        assertThat(reportService).isNotNull();
        assertThat(pdfReportService).isNotNull();
    }
}
