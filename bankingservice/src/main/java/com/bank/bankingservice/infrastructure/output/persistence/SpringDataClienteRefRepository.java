package com.bank.bankingservice.infrastructure.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataClienteRefRepository extends JpaRepository<ClienteRefEntity, Long> {
    Optional<ClienteRefEntity> findByClienteId(Long clienteId);
}