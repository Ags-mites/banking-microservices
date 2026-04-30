package com.bank.customerservice.application.dto;

import java.io.Serializable;

public record ClienteActualizadoEvent(
    Long clienteId,
    String identificacion,
    String nombre
) implements Serializable {}
