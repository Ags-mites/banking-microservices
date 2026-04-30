package com.bank.bankingservice.domain.exception;

public class InsufficientFundsException extends AccountConflictException {

    public InsufficientFundsException() {
        super("Saldo no disponible");
    }

    public InsufficientFundsException(String message) {
        super(message);
    }
}