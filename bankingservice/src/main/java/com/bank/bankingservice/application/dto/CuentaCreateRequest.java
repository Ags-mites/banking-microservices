package com.bank.bankingservice.application.dto;

import java.math.BigDecimal;

public record CuentaCreateRequest(
    String numeroCuenta,
    String tipoCuenta,
    BigDecimal saldoInicial,
    Boolean estado,
    Long clienteId
) {}
