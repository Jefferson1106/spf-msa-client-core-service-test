package com.pichincha.spfmsaclientecoreservice.repository;

import com.pichincha.spfmsaclientecoreservice.domain.Client;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
    public interface ClientRepository extends JpaRepository<Client, Long> {

    @EntityGraph(attributePaths = {"accounts"})
    @NonNull
    @Override
    List<Client> findAll();

    @EntityGraph(attributePaths = {"accounts"})
    @NonNull
    @Override
    Optional<Client> findById(@NonNull Long id);
}
