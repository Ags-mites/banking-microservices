package com.bank.bankingservice.domain.ports.out;

import com.bank.bankingservice.domain.model.Cuenta;
import java.util.List;
import java.util.Optional;

public interface CuentaRepository {
    Cuenta save(Cuenta cuenta);
    List<Cuenta> findAll();
    Optional<Cuenta> findById(Long id);
    boolean existsByNumeroCuenta(String numeroCuenta);
    void deleteById(Long id);
}
