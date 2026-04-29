package com.bank.bankingservice.application.service;

import com.bank.bankingservice.domain.model.Cuenta;
import com.bank.bankingservice.domain.ports.in.CuentaUseCase;
import com.bank.bankingservice.domain.ports.out.CuentaRepository;
import com.bank.bankingservice.domain.exception.AccountValidationException;
import com.bank.bankingservice.domain.exception.AccountConflictException;
import com.bank.bankingservice.domain.exception.AccountNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CuentaService implements CuentaUseCase {
    private final CuentaRepository repository;

    public CuentaService(CuentaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Cuenta createAccount(Cuenta cuenta) {
        if (!"Ahorros".equals(cuenta.getTipoCuenta()) && !"Corriente".equals(cuenta.getTipoCuenta())) {
            throw new AccountValidationException("Tipo de cuenta inválido. Solo 'Ahorros' o 'Corriente' están permitidos.");
        }
        if (cuenta.getSaldoInicial() != null && cuenta.getSaldoInicial().signum() < 0) {
            throw new AccountValidationException("El saldo inicial no puede ser negativo.");
        }
        if (repository.existsByNumeroCuenta(cuenta.getNumeroCuenta())) {
            throw new AccountConflictException("El número de cuenta ya existe.");
        }

        if (cuenta.getEstado() == null) {
            cuenta.setEstado(true);
        }
        cuenta.setSaldoDisponible(cuenta.getSaldoInicial());

        return repository.save(cuenta);
    }

    @Override
    public List<Cuenta> getAllAccounts() {
        return repository.findAll();
    }

    @Override
    public Cuenta getAccountById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Cuenta no encontrada con id: " + id));
    }

    @Override
    public Cuenta updateAccount(Long id, Cuenta cuentaActualizada) {
        Cuenta cuenta = getAccountById(id);

        if (cuentaActualizada.getTipoCuenta() != null) {
            if (!"Ahorros".equals(cuentaActualizada.getTipoCuenta()) && !"Corriente".equals(cuentaActualizada.getTipoCuenta())) {
                throw new AccountValidationException("Tipo de cuenta inválido.");
            }
            cuenta.setTipoCuenta(cuentaActualizada.getTipoCuenta());
        }

        if (cuentaActualizada.getEstado() != null) {
            cuenta.setEstado(cuentaActualizada.getEstado());
        }

        return repository.save(cuenta);
    }

    @Override
    public void deleteAccount(Long id) {
        Cuenta cuenta = getAccountById(id);
        cuenta.setEstado(false);
        repository.save(cuenta);
    }
}
