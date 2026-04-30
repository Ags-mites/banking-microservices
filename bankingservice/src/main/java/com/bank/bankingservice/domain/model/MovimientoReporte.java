package com.bank.bankingservice.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class MovimientoReporte {
    private final Instant fecha;
    private final String cliente;
    private final String numeroCuenta;
    private final String tipo;
    private final BigDecimal saldoInicial;
    private final Boolean estado;
    private final BigDecimal movimiento;
    private final BigDecimal saldoDisponible;

    private MovimientoReporte(Instant fecha,
                              String cliente,
                              String numeroCuenta,
                              String tipo,
                              BigDecimal saldoInicial,
                              Boolean estado,
                              BigDecimal movimiento,
                              BigDecimal saldoDisponible) {
        this.fecha = fecha;
        this.cliente = cliente;
        this.numeroCuenta = numeroCuenta;
        this.tipo = tipo;
        this.saldoInicial = saldoInicial;
        this.estado = estado;
        this.movimiento = movimiento;
        this.saldoDisponible = saldoDisponible;
    }

    public static MovimientoReporte reconstituir(Instant fecha,
                                                 String cliente,
                                                 String numeroCuenta,
                                                 String tipo,
                                                 BigDecimal saldoInicial,
                                                 Boolean estado,
                                                 BigDecimal movimiento,
                                                 BigDecimal saldoDisponible) {
        return new MovimientoReporte(fecha, cliente, numeroCuenta, tipo, saldoInicial, estado, movimiento, saldoDisponible);
    }

    public Instant fecha() {
        return fecha;
    }

    public String cliente() {
        return cliente;
    }

    public String numeroCuenta() {
        return numeroCuenta;
    }

    public String tipo() {
        return tipo;
    }

    public BigDecimal saldoInicial() {
        return saldoInicial;
    }

    public Boolean estado() {
        return estado;
    }

    public BigDecimal movimiento() {
        return movimiento;
    }

    public BigDecimal saldoDisponible() {
        return saldoDisponible;
    }
}