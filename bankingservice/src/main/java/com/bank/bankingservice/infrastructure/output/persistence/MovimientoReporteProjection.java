package com.bank.bankingservice.infrastructure.output.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoReporteProjection {

    private final LocalDateTime fecha;
    private final String cliente;
    private final String numeroCuenta;
    private final String tipo;
    private final BigDecimal saldoInicial;
    private final Boolean estado;
    private final BigDecimal movimiento;
    private final BigDecimal saldoDisponible;

    public MovimientoReporteProjection(LocalDateTime fecha,
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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public String getTipo() {
        return tipo;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public Boolean getEstado() {
        return estado;
    }

    public BigDecimal getMovimiento() {
        return movimiento;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible;
    }
}