package com.bank.bankingservice.domain.model;

import com.bank.bankingservice.domain.exception.InsufficientFundsException;
import com.bank.bankingservice.domain.exception.MovimientoValidationException;

import java.math.BigDecimal;
import java.time.Instant;

public class Movimiento {
    private Long id;
    private Instant fecha;
    private MovimientoType tipoMovimiento;
    private BigDecimal valor;
    private BigDecimal saldo;
    private Long cuentaId;

    private Movimiento() {
    }

    public static Movimiento registrarDeposito(Long cuentaId, BigDecimal valor) {
        validarCuentaId(cuentaId);
        validarValor(valor);

        Movimiento movimiento = new Movimiento();
        movimiento.cuentaId = cuentaId;
        movimiento.tipoMovimiento = MovimientoType.DEPOSITO;
        movimiento.valor = valor;
        return movimiento;
    }

    public static Movimiento registrarRetiro(Long cuentaId, BigDecimal valor) {
        validarCuentaId(cuentaId);
        validarValor(valor);

        Movimiento movimiento = new Movimiento();
        movimiento.cuentaId = cuentaId;
        movimiento.tipoMovimiento = MovimientoType.RETIRO;
        movimiento.valor = valor.negate();
        return movimiento;
    }

    public static Movimiento reconstituir(Long id, Instant fecha, MovimientoType tipoMovimiento,
                                          BigDecimal valor, BigDecimal saldo, Long cuentaId) {
        Movimiento movimiento = new Movimiento();
        movimiento.id = id;
        movimiento.fecha = fecha;
        movimiento.tipoMovimiento = tipoMovimiento;
        movimiento.valor = valor;
        movimiento.saldo = saldo;
        movimiento.cuentaId = cuentaId;
        return movimiento;
    }

    public void registrar(BigDecimal saldoAnterior) {
        if (saldoAnterior == null) {
            throw new MovimientoValidationException("El saldo de la cuenta es obligatorio");
        }

        if (tipoMovimiento == MovimientoType.RETIRO && saldoAnterior.compareTo(valor.abs()) < 0) {
            throw new InsufficientFundsException();
        }

        this.saldo = saldoAnterior.add(valor);
        this.fecha = Instant.now();
    }

    private static void validarCuentaId(Long cuentaId) {
        if (cuentaId == null) {
            throw new MovimientoValidationException("La cuenta es obligatoria");
        }
    }

    private static void validarValor(BigDecimal valor) {
        if (valor == null) {
            throw new MovimientoValidationException("El valor es obligatorio");
        }
        if (valor.signum() == 0) {
            throw new MovimientoValidationException("El valor debe ser mayor a cero");
        }
        if (valor.signum() < 0) {
            throw new MovimientoValidationException("El valor debe ser positivo");
        }
    }

    public Long id() {
        return id;
    }

    public Instant fecha() {
        return fecha;
    }

    public MovimientoType tipoMovimiento() {
        return tipoMovimiento;
    }

    public BigDecimal valor() {
        return valor;
    }

    public BigDecimal saldo() {
        return saldo;
    }

    public Long cuentaId() {
        return cuentaId;
    }
}