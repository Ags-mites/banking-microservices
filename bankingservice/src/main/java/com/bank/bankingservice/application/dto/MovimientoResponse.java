package com.bank.bankingservice.application.dto;

import java.math.BigDecimal;

public record MovimientoResponse(
    Long id,
    String fecha,
    String tipo,
    BigDecimal valor,
    BigDecimal saldo,
    Long cuentaId
) {}