package com.bank.customerservice.application.dto;

public record ClienteCreadoEvent(
    Long clienteId,
    String identificacion,
    String nombre
) {
}
