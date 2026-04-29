package com.bank.customerservice.application.dto;

public record ClienteUpdateRequest(
    String nombre,
    String genero,
    Integer edad,
    String identificacion,
    String direccion,
    String telefono,
    String contrasena,
    Boolean estado
) {}
