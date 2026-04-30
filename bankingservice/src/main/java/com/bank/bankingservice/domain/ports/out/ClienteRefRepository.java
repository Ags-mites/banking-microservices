package com.bank.bankingservice.domain.ports.out;

import com.bank.bankingservice.domain.model.ClienteRef;

import java.util.Optional;

public interface ClienteRefRepository {
    Optional<ClienteRef> findByClienteId(Long clienteId);

    Optional<ClienteRef> findByIdentificacion(String identificacion);

    ClienteRef upsert(Long clienteId, String nombre, String identificacion);
}