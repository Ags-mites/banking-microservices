package com.bank.customerservice.application.dto;

public record ClientePatchRequest(
    String nombre,
    String genero,
    Integer edad,
    String identificacion,
    String direccion,
    String telefono,
    String contrasena,
    Boolean estado
) {}
