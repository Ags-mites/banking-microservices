package com.bank.bankingservice.domain.exception;

public class AccountConflictException extends DomainException {
    
    public AccountConflictException(String message) {
        super(message);
    }
}
