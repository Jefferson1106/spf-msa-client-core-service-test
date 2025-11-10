package com.pichincha.spfmsaclientecoreservice.service.impl;

import com.pichincha.spfmsaclientecoreservice.model.ReportDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PdfReportServiceImplTest {

    @InjectMocks
    private PdfReportServiceImpl pdfReportService;

    private ReportDTO reportDTO1;
    private ReportDTO reportDTO2;

    @BeforeEach
    void setUp() {
        reportDTO1 = new ReportDTO();
        reportDTO1.setClient("John Doe");
        reportDTO1.setAccountNumber("ACCT-001");
        reportDTO1.setType("SAVINGS");
        reportDTO1.setInitialBalance(1000.0);
        reportDTO1.setMovement(500.0);
        reportDTO1.setAvailableBalance(1500.0);
        reportDTO1.setDate(LocalDate.now().minusDays(1));

        reportDTO2 = new ReportDTO();
        reportDTO2.setClient("John Doe");
        reportDTO2.setAccountNumber("ACCT-001");
        reportDTO2.setType("SAVINGS");
        reportDTO2.setInitialBalance(1500.0);
        reportDTO2.setMovement(-200.0);
        reportDTO2.setAvailableBalance(1300.0);
        reportDTO2.setDate(LocalDate.now());
    }

    @Test
    @DisplayName("generateAccountStatementPdf - generates PDF with valid report data")
    void generateAccountStatementPdf_generatesPdfWithValidReportData() {
        // Given
        List<ReportDTO> reportData = Arrays.asList(reportDTO1, reportDTO2);

        // When
        byte[] pdfBytes = pdfReportService.generateAccountStatementPdf(reportData);

        // Then
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);

        // Verify PDF header (PDF files start with "%PDF-")
        String pdfHeader = new String(Arrays.copyOfRange(pdfBytes, 0, 4));
        assertThat(pdfHeader).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("generateAccountStatementPdf - generates PDF with empty report data")
    void generateAccountStatementPdf_generatesPdfWithEmptyReportData() {
        // Given
        List<ReportDTO> emptyReportData = List.of();

        // When
        byte[] pdfBytes = pdfReportService.generateAccountStatementPdf(emptyReportData);

        // Then
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);

        // Verify it's still a valid PDF
        String pdfHeader = new String(Arrays.copyOfRange(pdfBytes, 0, 4));
        assertThat(pdfHeader).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("generateAccountStatementPdf - handles single report data entry")
    void generateAccountStatementPdf_handlesSingleReportDataEntry() {
        // Given
        List<ReportDTO> singleReportData = List.of(reportDTO1);

        // When
        byte[] pdfBytes = pdfReportService.generateAccountStatementPdf(singleReportData);

        // Then
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);

        // Verify it's a valid PDF
        String pdfHeader = new String(Arrays.copyOfRange(pdfBytes, 0, 4));
        assertThat(pdfHeader).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("generateAccountStatementPdf - handles null values in report data gracefully")
    void generateAccountStatementPdf_handlesNullValuesInReportDataGracefully() {
        // Given
        ReportDTO reportWithNulls = new ReportDTO();
        reportWithNulls.setClient("Jane Doe");
        reportWithNulls.setAccountNumber("ACCT-002");
        reportWithNulls.setType("CHECKING");
        // Leave other fields as null

        List<ReportDTO> reportDataWithNulls = List.of(reportWithNulls);

        // When
        byte[] pdfBytes = pdfReportService.generateAccountStatementPdf(reportDataWithNulls);

        // Then
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);

        // Verify it's still a valid PDF despite null values
        String pdfHeader = new String(Arrays.copyOfRange(pdfBytes, 0, 4));
        assertThat(pdfHeader).isEqualTo("%PDF");
    }
}
