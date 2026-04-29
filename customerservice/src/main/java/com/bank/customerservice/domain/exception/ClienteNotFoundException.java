package com.bank.customerservice.domain.exception;

public class ClienteNotFoundException extends DomainException {
    
    public ClienteNotFoundException(Long clienteId) {
        super("Cliente con id " + clienteId + " no encontrado");
    }
    
    public ClienteNotFoundException(String message) {
        super(message);
    }
}
