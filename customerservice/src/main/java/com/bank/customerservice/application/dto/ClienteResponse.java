package com.bank.customerservice.application.dto;

import java.time.LocalDateTime;

public record ClienteResponse(
    Long id,
    String nombre,
    String genero,
    Integer edad,
    String identificacion,
    String direccion,
    String telefono,
    Boolean estado,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
