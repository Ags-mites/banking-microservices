package com.bank.customerservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClienteResponse(
    Long id,
    
    @JsonProperty("Nombre")
    String nombre,
    
    @JsonProperty("Genero")
    String genero,
    
    @JsonProperty("Edad")
    Integer edad,
    
    @JsonProperty("Identificación")
    String identificacion,
    
    @JsonProperty("Dirección")
    String direccion,
    
    @JsonProperty("Teléfono")
    String telefono,
    
    @JsonProperty("Estado")
    Boolean estado,
    
    @JsonProperty("Fecha Creación")
    String createdAt,
    
    @JsonProperty("Fecha Actualización")
    String updatedAt
) {}
