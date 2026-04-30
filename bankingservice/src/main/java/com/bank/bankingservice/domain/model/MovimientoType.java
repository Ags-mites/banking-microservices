package com.bank.bankingservice.domain.model;

public enum MovimientoType {
    DEPOSITO("Depósito"),
    RETIRO("Retiro");

    private final String label;

    MovimientoType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static MovimientoType fromLabel(String value) {
        for (MovimientoType type : values()) {
            if (type.label.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de movimiento inválido. Solo 'Depósito' o 'Retiro' están permitidos.");
    }
}