package com.pichincha.spfmsaclientecoreservice.service.mapper;

import com.pichincha.spfmsaclientecoreservice.domain.Transaction;
import com.pichincha.spfmsaclientecoreservice.model.ReportDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(target = "date", source = "transaction.date")
    @Mapping(target = "client", expression = "java(getClientName(transaction))")
    @Mapping(target = "accountNumber", source = "transaction.account.accountNumber")
    @Mapping(target = "type", source = "transaction.account.accountType")
    @Mapping(target = "initialBalance", expression = "java(transaction.getAccount().getInitialBalance())")
    @Mapping(target = "status", expression = "java(transaction.getAccount().getStatus())")
    @Mapping(target = "movement", source = "transaction.amount")
    @Mapping(target = "availableBalance", source = "transaction.balance")
    @Mapping(target = "totalDebits", ignore = true)
    @Mapping(target = "totalCredits", ignore = true)
    ReportDTO toDto(Transaction transaction);

    default String getClientName(Transaction transaction) {
        if (transaction.getAccount() != null && transaction.getAccount().getClient() != null) {
            return transaction.getAccount().getClient().getName();
        }
        return null;
    }

    default ReportDTO toDto(
            Transaction transaction,
            BigDecimal totalDebits,
            BigDecimal totalCredits
    ) {
        ReportDTO report = toDto(transaction);
        report.setTotalDebits(totalDebits != null ? totalDebits.doubleValue() : null);
        report.setTotalCredits(totalCredits != null ? totalCredits.doubleValue() : null);
        return report;
    }
}

