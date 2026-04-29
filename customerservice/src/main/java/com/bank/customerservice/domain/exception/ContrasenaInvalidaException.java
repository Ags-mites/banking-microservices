package com.bank.customerservice.domain.exception;

/**
 * Excepción cuando la contraseña no cumple con los requisitos
 */
public class ContrasenaInvalidaException extends DomainException {
    
    public ContrasenaInvalidaException(String message) {
        super(message);
    }
}
