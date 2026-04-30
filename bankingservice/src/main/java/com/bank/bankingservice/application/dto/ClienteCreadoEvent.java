package com.bank.bankingservice.application.dto;

public record ClienteCreadoEvent(
        Long clienteId,
        String identificacion,
        String nombre
) {
}
