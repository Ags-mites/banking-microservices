package com.bank.customerservice.application.dto;

import java.time.LocalDateTime;

public record ClienteCreadoEvent(
    Long clienteId,
    String nombre,
    String identificacion,
    Boolean estado,
    LocalDateTime createdAt
) {}
