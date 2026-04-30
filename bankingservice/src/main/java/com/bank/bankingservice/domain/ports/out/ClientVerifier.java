package com.bank.bankingservice.domain.ports.out;

public interface ClientVerifier {
    boolean existsById(Long clienteId);
}
