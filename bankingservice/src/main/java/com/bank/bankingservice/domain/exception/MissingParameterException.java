package com.bank.bankingservice.domain.exception;

public class MissingParameterException extends DomainException {

    public MissingParameterException(String message) {
        super(message);
    }
}