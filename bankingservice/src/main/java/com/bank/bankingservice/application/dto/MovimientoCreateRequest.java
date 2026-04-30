package com.bank.bankingservice.application.dto;

import java.math.BigDecimal;

public record MovimientoCreateRequest(
    Long cuentaId,
    String tipo,
    BigDecimal valor
) {}