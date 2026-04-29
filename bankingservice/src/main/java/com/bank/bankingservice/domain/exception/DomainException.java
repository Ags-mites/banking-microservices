package com.bank.bankingservice.domain.exception;

public class DomainException extends RuntimeException {
    private final String code;
    private final int status;

    public DomainException(String message, String code, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}
