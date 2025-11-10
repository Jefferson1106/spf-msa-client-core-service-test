package com.pichincha.spfmsaclientecoreservice.service;

import com.pichincha.spfmsaclientecoreservice.model.ReportDTO;

import java.util.List;

public interface PdfReportService {

    byte[] generateAccountStatementPdf(List<ReportDTO> reportData);

}
