package com.bank.bankingservice.application.dto;

import java.math.BigDecimal;

public record MovimientoListResponse(
    String fecha,
    String cliente,
    String numeroCuenta,
    String tipo,
    BigDecimal saldoInicial,
    Boolean estado,
    BigDecimal movimiento,
    BigDecimal saldoDisponible
) {}