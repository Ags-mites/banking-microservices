package com.bank.bankingservice.domain.ports.in;

import com.bank.bankingservice.domain.model.Cuenta;
import java.util.List;
import java.util.Optional;

public interface CuentaUseCase {
    Cuenta createAccount(Cuenta cuenta);
    List<Cuenta> getAllAccounts();
    Cuenta getAccountById(Long id);
    Cuenta updateAccount(Long id, Cuenta cuenta);
    void deleteAccount(Long id);
}
