package com.bank.bankingservice.domain.exception;

public class AccountNotFoundException extends DomainException {
    public AccountNotFoundException(String message) {
        super(message, "ACCOUNT_NOT_FOUND", 404);
    }
}
