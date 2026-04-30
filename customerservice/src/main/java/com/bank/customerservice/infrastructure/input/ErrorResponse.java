package com.bank.customerservice.infrastructure.input;


public record ErrorResponse(
    String type,
    String title,
    Integer status,
    String detail,
    String instance,
    String timestamp
) {}
