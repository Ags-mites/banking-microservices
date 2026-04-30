package com.bank.bankingservice.infrastructure.input;

public record ErrorResponse(
    String type,
    String title,
    Integer status,
    String detail,
    String instance,
    String timestamp
) {}
