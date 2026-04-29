package com.bank.bankingservice.application.service;

import com.bank.bankingservice.domain.model.Cuenta;
import com.bank.bankingservice.domain.ports.in.CuentaUseCase;
import com.bank.bankingservice.domain.ports.out.CuentaRepository;
import com.bank.bankingservice.domain.exception.AccountValidationException;
import com.bank.bankingservice.domain.exception.AccountConflictException;
import com.bank.bankingservice.domain.exception.AccountNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CuentaService implements CuentaUseCase {
    private final CuentaRepository repository;

    public CuentaService(CuentaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Cuenta createAccount(Long clienteId, String numeroCuenta, String tipoCuenta, BigDecimal saldoInicial) {
        if (repository.existsByNumeroCuenta(numeroCuenta)) {
            throw new AccountConflictException("El número de cuenta ya existe.");
        }

        Cuenta cuenta = Cuenta.abrir(clienteId, numeroCuenta, tipoCuenta, saldoInicial);
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
    public Cuenta updateAccount(Long id, String tipoCuenta, Boolean estado) {
        Cuenta cuenta = getAccountById(id);
        cuenta.actualizar(tipoCuenta, estado);
        return repository.save(cuenta);
    }

    @Override
    public void deleteAccount(Long id) {
        Cuenta cuenta = getAccountById(id);
        cuenta.inactivar();
        repository.save(cuenta);
    }
}
