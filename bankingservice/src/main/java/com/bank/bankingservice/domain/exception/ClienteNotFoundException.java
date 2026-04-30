package com.bank.bankingservice.domain.exception;

public class ClienteNotFoundException extends DomainException {

    public ClienteNotFoundException(String message) {
        super(message);
    }
}