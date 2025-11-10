package com.pichincha.spfmsaclientecoreservice.service.impl;

import com.pichincha.spfmsaclientecoreservice.model.ReportDTO;
import com.pichincha.spfmsaclientecoreservice.service.PdfReportService;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Slf4j
@Service
public class PdfReportServiceImpl implements PdfReportService {

    @Override
    public byte[] generateAccountStatementPdf(List<ReportDTO> reportData) {
        try {
            // Agrupar datos por cuenta
            Map<String, List<ReportDTO>> groupedByAccount = groupByAccount(reportData);

            JasperDesign jasperDesign = createReportDesign();
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

            // Preparar parámetros
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", "ESTADO DE CUENTA BANCARIO");

            if (!reportData.isEmpty()) {
                parameters.put("ClientName", reportData.get(0).getClient());

                // Calcular totales generales
                double totalInitialBalance = groupedByAccount.values().stream()
                        .mapToDouble(list -> {
                            if (list.isEmpty()) return 0.0;
                            Double v = list.get(0).getInitialBalance();
                            return v != null ? v : 0.0;
                        })
                        .sum();

                double totalDeposits = reportData.stream()
                        .mapToDouble(r -> {
                            Double mv = r.getMovement();
                            return (mv != null && mv.doubleValue() > 0.0) ? mv.doubleValue() : 0.0;
                        })
                        .sum();

                double totalWithdrawals = Math.abs(reportData.stream()
                        .mapToDouble(r -> {
                            Double mv = r.getMovement();
                            return (mv != null && mv.doubleValue() < 0.0) ? mv.doubleValue() : 0.0;
                        })
                        .sum());

                double totalFinalBalance = reportData.stream()
                        .mapToDouble(r -> {
                            Double av = r.getAvailableBalance();
                            return av != null ? av.doubleValue() : 0.0;
                        })
                        .max()
                        .orElse(0.0);

                parameters.put("TotalAccounts", groupedByAccount.size());
                parameters.put("TotalInitialBalance", totalInitialBalance);
                parameters.put("TotalDeposits", totalDeposits);
                parameters.put("TotalWithdrawals", totalWithdrawals);
                parameters.put("TotalFinalBalance", totalFinalBalance);
            }

            // Crear datasource con datos agrupados y convertidos
            List<Map<String, Object>> convertedData = convertGroupedDataToMap(groupedByAccount);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(convertedData);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

            return outputStream.toByteArray();

        } catch (JRException exception) {
            throw new RuntimeException("Error generating PDF report: " + exception.getMessage(), exception);
        }
    }

    private Map<String, List<ReportDTO>> groupByAccount(List<ReportDTO> reportData) {
        Map<String, List<ReportDTO>> grouped = new LinkedHashMap<>();
        for (ReportDTO report : reportData) {
            String accountKey = report.getAccountNumber() + "-" + report.getType();
            grouped.computeIfAbsent(accountKey, k -> new ArrayList<>()).add(report);
        }
        return grouped;
    }

    private JasperDesign createReportDesign() throws JRException {
        JasperDesign jasperDesign = new JasperDesign();
        jasperDesign.setName("AccountStatement");
        jasperDesign.setPageWidth(595);
        jasperDesign.setPageHeight(842);
        jasperDesign.setColumnWidth(515);
        jasperDesign.setColumnSpacing(0);
        jasperDesign.setLeftMargin(40);
        jasperDesign.setRightMargin(40);
        jasperDesign.setTopMargin(50);
        jasperDesign.setBottomMargin(50);

        addParameters(jasperDesign);
        addFields(jasperDesign);
        addTitleBand(jasperDesign);
        addColumnHeaderBand(jasperDesign);
        addDetailBand(jasperDesign);
        addPageFooter(jasperDesign);

        return jasperDesign;
    }

    private void addParameters(JasperDesign jasperDesign) throws JRException {
        JRDesignParameter parameterTitle = new JRDesignParameter();
        parameterTitle.setName("ReportTitle");
        parameterTitle.setValueClass(String.class);
        jasperDesign.addParameter(parameterTitle);

        JRDesignParameter parameterClientName = new JRDesignParameter();
        parameterClientName.setName("ClientName");
        parameterClientName.setValueClass(String.class);
        jasperDesign.addParameter(parameterClientName);

        // Parámetros para resumen general
        addParameter(jasperDesign, "TotalAccounts", Integer.class);
        addParameter(jasperDesign, "TotalInitialBalance", Double.class);
        addParameter(jasperDesign, "TotalDeposits", Double.class);
        addParameter(jasperDesign, "TotalWithdrawals", Double.class);
        addParameter(jasperDesign, "TotalFinalBalance", Double.class);
    }

    private void addParameter(JasperDesign jasperDesign, String name, Class<?> valueClass) throws JRException {
        JRDesignParameter parameter = new JRDesignParameter();
        parameter.setName(name);
        parameter.setValueClass(valueClass);
        jasperDesign.addParameter(parameter);
    }

    private void addFields(JasperDesign jasperDesign) throws JRException {
        // Campos para encabezados de cuenta
        addField(jasperDesign, "isAccountHeader", Boolean.class);
        addField(jasperDesign, "accountNumber", String.class);
        addField(jasperDesign, "accountType", String.class);
        addField(jasperDesign, "client", String.class);
        addField(jasperDesign, "accountInitialBalance", Double.class);
        addField(jasperDesign, "accountDeposits", Double.class);
        addField(jasperDesign, "accountWithdrawals", Double.class);
        addField(jasperDesign, "accountFinalBalance", Double.class);

        // Campos para movimientos
        addField(jasperDesign, "date", String.class);
        addField(jasperDesign, "type", String.class);
        addField(jasperDesign, "initialBalance", Double.class);
        addField(jasperDesign, "status", String.class);
        addField(jasperDesign, "movementType", String.class); // NUEVA COLUMNA
        addField(jasperDesign, "movement", Double.class);
        addField(jasperDesign, "availableBalance", Double.class);
    }

    private void addField(JasperDesign jasperDesign, String fieldName, Class<?> fieldClass) throws JRException {
        JRDesignField field = new JRDesignField();
        field.setName(fieldName);
        field.setValueClass(fieldClass);
        jasperDesign.addField(field);
    }

    private void addTitleBand(JasperDesign jasperDesign) {
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(110); // ✅ REDUCIDO: Sin recuadro de fecha

        // Logo y nombre del banco NEXUS
        JRDesignStaticText bankName = new JRDesignStaticText();
        bankName.setX(0);
        bankName.setY(5);
        bankName.setWidth(515);
        bankName.setHeight(35);
        bankName.setText("BANCO CLIENTES");
        bankName.setFontSize(28f);
        bankName.setBold(true);
        bankName.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        bankName.setForecolor(new Color(0, 51, 102)); // Azul corporativo
        titleBand.addElement(bankName);

        // Subtítulo del banco
        JRDesignStaticText bankSlogan = new JRDesignStaticText();
        bankSlogan.setX(0);
        bankSlogan.setY(38);
        bankSlogan.setWidth(515);
        bankSlogan.setHeight(12);
        bankSlogan.setText("BANCO DE CONFIANZA Y SEGURIDAD");
        bankSlogan.setFontSize(8f);
        bankSlogan.setItalic(true);
        bankSlogan.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        bankSlogan.setForecolor(new Color(100, 100, 100));
        titleBand.addElement(bankSlogan);

        // Línea separadora
        JRDesignLine line1 = new JRDesignLine();
        line1.setX(0);
        line1.setY(55);
        line1.setWidth(515);
        line1.setHeight(0);
        line1.setForecolor(new Color(0, 51, 102));
        titleBand.addElement(line1);

        // Título del documento
        JRDesignStaticText titleText = new JRDesignStaticText();
        titleText.setX(0);
        titleText.setY(62);
        titleText.setWidth(515);
        titleText.setHeight(20);
        titleText.setText("ESTADO DE CUENTA BANCARIO");
        titleText.setFontSize(14f);
        titleText.setBold(true);
        titleText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleBand.addElement(titleText);

        // ✅ SOLO INFORMACIÓN DE CUENTA Y CLIENTE (sin recuadro de fecha)
        JRDesignTextField accountInfoField = new JRDesignTextField();
        accountInfoField.setX(0);
        accountInfoField.setY(87);
        accountInfoField.setWidth(515);
        accountInfoField.setHeight(15);
        accountInfoField.setExpression(createExpression("\"CUENTA AHORROS - 478758 - Cliente: \" + $P{ClientName} + \"    |    Total de Cuentas: \" + $P{TotalAccounts}"));
        accountInfoField.setFontSize(11f);
        accountInfoField.setBold(true);
        accountInfoField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        accountInfoField.setForecolor(new Color(0, 51, 102));
        titleBand.addElement(accountInfoField);

        jasperDesign.setTitle(titleBand);
    }

    private void addColumnHeaderBand(JasperDesign jasperDesign) {
        JRDesignBand columnHeaderBand = new JRDesignBand();
        columnHeaderBand.setHeight(30);

        // Fondo azul para el encabezado
        JRDesignRectangle headerBackground = new JRDesignRectangle();
        headerBackground.setX(0);
        headerBackground.setY(0);
        headerBackground.setWidth(515);
        headerBackground.setHeight(30);
        headerBackground.setBackcolor(new Color(0, 51, 102)); // Azul corporativo
        headerBackground.setForecolor(new Color(0, 51, 102));
        columnHeaderBand.addElement(headerBackground);

        // ACTUALIZADO: Agregada columna "Tipo Movimiento"
        String[] headers = {"Fecha", "Cliente", "Nro.Cuenta", "Tipo", "Saldo Inicial", "Estado", "Tipo Movimiento", "Movimiento", "Saldo Disponible"};
        int[] widths = {50, 70, 60, 55, 60, 35, 70, 55, 60};
        int xPosition = 0;

        for (int i = 0; i < headers.length; i++) {
            JRDesignStaticText headerText = new JRDesignStaticText();
            headerText.setX(xPosition);
            headerText.setY(5);
            headerText.setWidth(widths[i]);
            headerText.setHeight(20);
            headerText.setText(headers[i]);
            headerText.setFontSize(7f); // Reducido para que quepa todo
            headerText.setBold(true);
            headerText.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
            headerText.setForecolor(Color.WHITE); // Texto blanco
            columnHeaderBand.addElement(headerText);
            xPosition += widths[i];
        }

        jasperDesign.setColumnHeader(columnHeaderBand);
    }

    private void addDetailBand(JasperDesign jasperDesign) {
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(18); // ✅ ALTURA COMPACTA PARA REDUCIR ESPACIADO

        int[] widths = {50, 70, 60, 55, 60, 35, 70, 55, 60};

        // ============ SOLO FILAS DE TRANSACCIONES ============
        // Fondo alternado para transacciones
        JRDesignRectangle rowBackground = new JRDesignRectangle();
        rowBackground.setX(0);
        rowBackground.setY(0);
        rowBackground.setWidth(515);
        rowBackground.setHeight(17);
        rowBackground.setBackcolor(new Color(250, 250, 250));
        rowBackground.setForecolor(new Color(200, 200, 200));
        detailBand.addElement(rowBackground);

        // Línea inferior de cada fila de transacción
        JRDesignLine rowLine = new JRDesignLine();
        rowLine.setX(0);
        rowLine.setY(17);
        rowLine.setWidth(515);
        rowLine.setHeight(0);
        rowLine.setForecolor(new Color(220, 220, 220));
        detailBand.addElement(rowLine);

        int xPosition = 0;
        int transactionY = 2; // ✅ Posición Y=2 para centrar texto en fila compacta

        // Fecha
        JRDesignTextField dateField = new JRDesignTextField();
        dateField.setX(xPosition);
        dateField.setY(transactionY);
        dateField.setWidth(widths[0]);
        dateField.setHeight(13);
        dateField.setExpression(createExpression("$F{date}"));
        dateField.setFontSize(7f);
        dateField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        detailBand.addElement(dateField);
        xPosition += widths[0];

        // Cliente
        JRDesignTextField clientField = new JRDesignTextField();
        clientField.setX(xPosition);
        clientField.setY(transactionY);
        clientField.setWidth(widths[1]);
        clientField.setHeight(13);
        clientField.setExpression(createExpression("$F{client}"));
        clientField.setFontSize(7f);
        clientField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        detailBand.addElement(clientField);
        xPosition += widths[1];

        // Número de cuenta
        JRDesignTextField accountNumberField = new JRDesignTextField();
        accountNumberField.setX(xPosition);
        accountNumberField.setY(transactionY);
        accountNumberField.setWidth(widths[2]);
        accountNumberField.setHeight(13);
        accountNumberField.setExpression(createExpression("$F{accountNumber}"));
        accountNumberField.setFontSize(7f);
        accountNumberField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        detailBand.addElement(accountNumberField);
        xPosition += widths[2];

        // Tipo de cuenta
        JRDesignTextField typeField = new JRDesignTextField();
        typeField.setX(xPosition);
        typeField.setY(transactionY);
        typeField.setWidth(widths[3]);
        typeField.setHeight(13);
        typeField.setExpression(createExpression("$F{type}"));
        typeField.setFontSize(7f);
        typeField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        typeField.setBold(true);
        typeField.setForecolor(new Color(0, 51, 102));
        detailBand.addElement(typeField);
        xPosition += widths[3];

        // Saldo Inicial
        JRDesignTextField initialBalanceField = new JRDesignTextField();
        initialBalanceField.setX(xPosition);
        initialBalanceField.setY(transactionY);
        initialBalanceField.setWidth(widths[4]);
        initialBalanceField.setHeight(13);
        initialBalanceField.setExpression(createExpression("\"$\" + new java.text.DecimalFormat(\"#,##0.00\").format($F{initialBalance})"));
        initialBalanceField.setFontSize(7f);
        initialBalanceField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        detailBand.addElement(initialBalanceField);
        xPosition += widths[4];

        // Estado
        JRDesignTextField statusField = new JRDesignTextField();
        statusField.setX(xPosition);
        statusField.setY(transactionY);
        statusField.setWidth(widths[5]);
        statusField.setHeight(13);
        statusField.setExpression(createExpression("$F{status}"));
        statusField.setFontSize(7f);
        statusField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        detailBand.addElement(statusField);
        xPosition += widths[5];

        // Tipo de Movimiento (NUEVA COLUMNA CON EMOJIS)
        JRDesignTextField movementTypeField = new JRDesignTextField();
        movementTypeField.setX(xPosition);
        movementTypeField.setY(transactionY);
        movementTypeField.setWidth(widths[6]);
        movementTypeField.setHeight(13);
        movementTypeField.setExpression(createExpression("$F{movementType}"));
        movementTypeField.setFontSize(7f);
        movementTypeField.setBold(true);
        movementTypeField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        detailBand.addElement(movementTypeField);
        xPosition += widths[6];

        // Movimiento
        JRDesignTextField movementField = new JRDesignTextField();
        movementField.setX(xPosition);
        movementField.setY(transactionY);
        movementField.setWidth(widths[7]);
        movementField.setHeight(13);
        movementField.setExpression(createExpression("\"$\" + new java.text.DecimalFormat(\"#,##0.00\").format($F{movement})"));
        movementField.setFontSize(7f);
        movementField.setBold(true);
        movementField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        detailBand.addElement(movementField);
        xPosition += widths[7];

        // Saldo Disponible
        JRDesignTextField availableBalanceField = new JRDesignTextField();
        availableBalanceField.setX(xPosition);
        availableBalanceField.setY(transactionY);
        availableBalanceField.setWidth(widths[8]);
        availableBalanceField.setHeight(13);
        availableBalanceField.setExpression(createExpression("\"$\" + new java.text.DecimalFormat(\"#,##0.00\").format($F{availableBalance})"));
        availableBalanceField.setFontSize(7f);
        availableBalanceField.setBold(true);
        availableBalanceField.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        availableBalanceField.setForecolor(new Color(0, 100, 0));
        detailBand.addElement(availableBalanceField);

        ((JRDesignSection) jasperDesign.getDetailSection()).addBand(detailBand);
    }

    private void addPageFooter(JasperDesign jasperDesign) {
        JRDesignBand pageFooter = new JRDesignBand();
        pageFooter.setHeight(50);

        // Línea superior
        JRDesignLine topLine = new JRDesignLine();
        topLine.setX(0);
        topLine.setY(5);
        topLine.setWidth(515);
        topLine.setHeight(0);
        topLine.setForecolor(new Color(0, 51, 102));
        pageFooter.addElement(topLine);

        // Información del banco
        JRDesignStaticText footerInfo = new JRDesignStaticText();
        footerInfo.setX(0);
        footerInfo.setY(10);
        footerInfo.setWidth(350);
        footerInfo.setHeight(15);
        footerInfo.setText("BANK - Servicios Financieros | www.nexusbank.com | 1-800-BANK");
        footerInfo.setFontSize(7f);
        footerInfo.setForecolor(new Color(100, 100, 100));
        pageFooter.addElement(footerInfo);

        // Número de página
        JRDesignTextField pageNumber = new JRDesignTextField();
        pageNumber.setX(350);
        pageNumber.setY(10);
        pageNumber.setWidth(165);
        pageNumber.setHeight(15);
        pageNumber.setExpression(createExpression("\"Página \" + $V{PAGE_NUMBER}"));
        pageNumber.setFontSize(8f);
        pageNumber.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        pageFooter.addElement(pageNumber);

        // Advertencia legal
        JRDesignStaticText legalNote = new JRDesignStaticText();
        legalNote.setX(0);
        legalNote.setY(25);
        legalNote.setWidth(515);
        legalNote.setHeight(20);
        legalNote.setText("Este documento es confidencial. Para consultas, contacte con su asesor bancario.");
        legalNote.setFontSize(7f);
        legalNote.setItalic(true);
        legalNote.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        legalNote.setForecolor(new Color(150, 150, 150));
        pageFooter.addElement(legalNote);

        jasperDesign.setPageFooter(pageFooter);
    }

    private JRDesignExpression createExpression(String text) {
        JRDesignExpression expression = new JRDesignExpression();
        expression.setText(text);
        return expression;
    }

    private List<Map<String, Object>> convertGroupedDataToMap(Map<String, List<ReportDTO>> groupedByAccount) {
        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<Map<String, Object>> dataList = new ArrayList<>();

        for (Map.Entry<String, List<ReportDTO>> entry : groupedByAccount.entrySet()) {
            List<ReportDTO> accountReports = entry.getValue();

            if (accountReports.isEmpty()) continue;

            // ✅ SIN ENCABEZADOS DE CUENTA - Solo transacciones
            for (ReportDTO report : accountReports) {
                Map<String, Object> map = new HashMap<>();
                map.put("isAccountHeader", false);


                java.time.LocalDate localDate = report.getDate();
                if (localDate != null) {
                    String fecha = localDate.format(dateOnlyFormatter);
                    map.put("date", fecha);
                } else {
                    map.put("date", "");
                }

                map.put("client", report.getClient() != null ? report.getClient() : "");
                map.put("accountNumber", report.getAccountNumber() != null ? report.getAccountNumber() : "");
                map.put("type", translateAccountType(report.getType()));
                map.put("initialBalance", report.getInitialBalance() != null ? report.getInitialBalance() : Double.valueOf(0.0));
                map.put("status", Boolean.TRUE.equals(report.getStatus()) ? "Sí" : "No");

                // Tipo de movimiento con emojis
                String movementType = "";
                Double mvLocal = report.getMovement();
                if (mvLocal != null) {
                    movementType = mvLocal.doubleValue() >= 0.0 ? "🟢 DEPÓSITO" : "🔴 RETIRO";
                }
                map.put("movementType", movementType);

                map.put("movement", report.getMovement() != null ? report.getMovement() : Double.valueOf(0.0));
                map.put("availableBalance", report.getAvailableBalance() != null ? report.getAvailableBalance() : Double.valueOf(0.0));

                dataList.add(map);
            }
        }
        return dataList;
    }

    private String translateAccountType(String type) {
        if (type == null) return "";
        switch (type.toUpperCase()) {
            case "SAVINGS":
                return "Ahorros";
            case "CHECKING":
                return "Corriente";
            default:
                return type;
        }
    }
}
