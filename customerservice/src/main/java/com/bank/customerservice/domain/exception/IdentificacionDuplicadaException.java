package com.bank.customerservice.domain.exception;

public class IdentificacionDuplicadaException extends DomainException {
    
    public IdentificacionDuplicadaException(String identificacion) {
        super("La identificación " + identificacion + " ya está en uso");
    }
}
