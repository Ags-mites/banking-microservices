package com.bank.bankingservice.application.dto;

import java.io.Serializable;

public record ClienteActualizadoEvent(
    Long clienteId,
    String identificacion,
    String nombre
) implements Serializable {}
