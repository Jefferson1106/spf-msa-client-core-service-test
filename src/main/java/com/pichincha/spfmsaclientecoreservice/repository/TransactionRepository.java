package com.pichincha.spfmsaclientecoreservice.repository;

import com.pichincha.spfmsaclientecoreservice.domain.Account;
import com.pichincha.spfmsaclientecoreservice.domain.Transaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN FETCH t.account a " +
            "LEFT JOIN FETCH a.client c " +
            "WHERE t.account.client.personId = :clientId " +
            "AND t.date BETWEEN :startDate AND :endDate")
    List<Transaction> findByClientAndDateRange(
            @Param("clientId") Long clientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @EntityGraph(attributePaths = {"account", "account.client"})
    @NonNull
    @Override
    List<Transaction> findAll();

    @EntityGraph(attributePaths = {"account", "account.client"})
    @NonNull
    @Override
    Optional<Transaction> findById(@NonNull Long id);

    /**
     * Encuentra todas las transacciones de una cuenta ordenadas por fecha ascendente
     */
    List<Transaction> findByAccountOrderByDateAsc(Account account);
}
