package com.bank.bankingservice.infrastructure.output.persistence;

import org.springframework.data.repository.CrudRepository;

public interface SpringDataCuentaRepository extends CrudRepository<CuentaEntity, Long> {
    boolean existsByNumeroCuenta(String numeroCuenta);
}
