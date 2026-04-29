package com.bank.bankingservice.application.dto;

public record CuentaUpdateRequest(
    String tipoCuenta,
    Boolean estado
) {}
