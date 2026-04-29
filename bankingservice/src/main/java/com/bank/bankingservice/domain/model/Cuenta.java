package com.bank.bankingservice.domain.model;

import com.bank.bankingservice.domain.exception.AccountValidationException;
import java.math.BigDecimal;
import java.time.Instant;

public class Cuenta {
    private Long id;
    private String numeroCuenta;
    private String tipoCuenta;
    private BigDecimal saldoInicial;
    private BigDecimal saldoDisponible;
    private Boolean estado;
    private Long clienteId;
    private Integer version;
    private Instant createdAt;
    private Instant updatedAt;

    private Cuenta() {
    }

    public static Cuenta abrir(Long clienteId, String numeroCuenta, String tipoCuenta, BigDecimal saldoInicial) {
        validarNumeroCuenta(numeroCuenta);
        validarTipoCuenta(tipoCuenta);
        validarSaldo(saldoInicial);

        Cuenta cuenta = new Cuenta();
        cuenta.clienteId = clienteId;
        cuenta.numeroCuenta = numeroCuenta;
        cuenta.tipoCuenta = tipoCuenta;
        cuenta.saldoInicial = saldoInicial;
        cuenta.saldoDisponible = saldoInicial;
        cuenta.estado = true;
        cuenta.createdAt = Instant.now();
        cuenta.updatedAt = Instant.now();
        return cuenta;
    }

    public static Cuenta reconstituir(Long id, String numeroCuenta, String tipoCuenta, BigDecimal saldoInicial,
                                      BigDecimal saldoDisponible, Boolean estado, Long clienteId,
                                      Integer version, Instant createdAt, Instant updatedAt) {
        Cuenta cuenta = new Cuenta();
        cuenta.id = id;
        cuenta.numeroCuenta = numeroCuenta;
        cuenta.tipoCuenta = tipoCuenta;
        cuenta.saldoInicial = saldoInicial;
        cuenta.saldoDisponible = saldoDisponible;
        cuenta.estado = estado;
        cuenta.clienteId = clienteId;
        cuenta.version = version;
        cuenta.createdAt = createdAt;
        cuenta.updatedAt = updatedAt;
        return cuenta;
    }

    public void actualizar(String tipoCuenta, Boolean estado) {
        if (tipoCuenta != null) {
            validarTipoCuenta(tipoCuenta);
            this.tipoCuenta = tipoCuenta;
        }
        if (estado != null) {
            this.estado = estado;
        }
        this.updatedAt = Instant.now();
    }

    public void inactivar() {
        this.estado = false;
        this.updatedAt = Instant.now();
    }

    // ─── Validaciones ──────────────────────────────────────────────────

    private static void validarNumeroCuenta(String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty()) {
            throw new AccountValidationException("El número de cuenta es obligatorio.");
        }
    }

    private static void validarTipoCuenta(String tipoCuenta) {
        if (!"Ahorros".equals(tipoCuenta) && !"Corriente".equals(tipoCuenta)) {
            throw new AccountValidationException("Tipo de cuenta inválido. Solo 'Ahorros' o 'Corriente' están permitidos.");
        }
    }

    private static void validarSaldo(BigDecimal saldo) {
        if (saldo == null || saldo.signum() < 0) {
            throw new AccountValidationException("El saldo no puede ser negativo.");
        }
    }

    // ─── Getters ───────────────────────────────────────────────────────

    public Long id() { return id; }
    public String numeroCuenta() { return numeroCuenta; }
    public String tipoCuenta() { return tipoCuenta; }
    public BigDecimal saldoInicial() { return saldoInicial; }
    public BigDecimal saldoDisponible() { return saldoDisponible; }
    public Boolean estado() { return estado; }
    public Long clienteId() { return clienteId; }
    public Integer version() { return version; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    // ─── Setters (para infraestructura/persistencia) ────────────────────

    public void setId(Long id) { this.id = id; }
    public void setVersion(Integer version) { this.version = version; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
