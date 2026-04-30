package com.bank.customerservice.application.dto;

public record ClienteResponse(
    Long id,
    
    String nombre,
    
    String genero,
    
    Integer edad,
    
    String identificacion,
    
    String direccion,
    
    String telefono,
    
    Boolean estado,
    
    String createdAt,
    
    String updatedAt
) {}
