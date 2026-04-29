package com.bank.bankingservice.domain.ports.in;

import com.bank.bankingservice.domain.model.Cuenta;
import java.math.BigDecimal;
import java.util.List;

public interface CuentaUseCase {
    Cuenta createAccount(Long clienteId, String numeroCuenta, String tipoCuenta, BigDecimal saldoInicial);
    List<Cuenta> getAllAccounts();
    Cuenta getAccountById(Long id);
    Cuenta updateAccount(Long id, String tipoCuenta, Boolean estado);
    void deleteAccount(Long id);
}
