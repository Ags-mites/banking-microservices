package com.bank.bankingservice.domain.exception;

public class InvalidDateFormatException extends DomainException {

    public InvalidDateFormatException(String message) {
        super(message);
    }
}