package com.pichincha.spfmsaclientecoreservice.service.mapper;

import com.pichincha.spfmsaclientecoreservice.domain.Transaction;
import com.pichincha.spfmsaclientecoreservice.model.TransactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", uses = {AccountMapper.class})
public interface TransactionMapper {

    @Mapping(source = "account.accountId", target = "accountId")
    TransactionDTO toDto(Transaction transaction);

    @Mapping(source = "accountId", target = "account.accountId")
    Transaction toEntity(TransactionDTO transactionDTO);

    default OffsetDateTime map(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atOffset(ZoneOffset.UTC);
    }

    default LocalDateTime map(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : offsetDateTime.toLocalDateTime();
    }
}
