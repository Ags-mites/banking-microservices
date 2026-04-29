package com.bank.bankingservice.domain.exception;

public class AccountValidationException extends DomainException {
    public AccountValidationException(String message) {
        super(message, "ACCOUNT_VALIDATION_ERROR", 400);
    }
}
