package com.bank.bankingservice.domain.ports.out;

public interface ClienteLookup {
    String findNombreById(Long clienteId);
}