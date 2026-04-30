package com.bank.bankingservice.domain.exception;

public class AccountNotFoundException extends DomainException {
    
    public AccountNotFoundException(Long accountId) {
        super("Cuenta no encontrada con id: " + accountId);
    }
    
    public AccountNotFoundException(String message) {
        super(message);
    }
}
