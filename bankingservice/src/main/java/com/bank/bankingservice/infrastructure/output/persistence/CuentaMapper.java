package com.bank.bankingservice.infrastructure.output.persistence;

import com.bank.bankingservice.domain.model.Cuenta;
import org.springframework.stereotype.Component;

@Component
public class CuentaMapper {
    public CuentaEntity toEntity(Cuenta cuenta) {
        if (cuenta == null) {
            return null;
        }
        CuentaEntity entity = new CuentaEntity();
        entity.setId(cuenta.getId());
        entity.setNumeroCuenta(cuenta.getNumeroCuenta());
        entity.setTipoCuenta(cuenta.getTipoCuenta());
        entity.setSaldoInicial(cuenta.getSaldoInicial());
        entity.setSaldoDisponible(cuenta.getSaldoDisponible());
        entity.setEstado(cuenta.getEstado());
        entity.setClienteId(cuenta.getClienteId());
        return entity;
    }

    public Cuenta toDomain(CuentaEntity entity) {
        if (entity == null) {
            return null;
        }
        Cuenta cuenta = new Cuenta();
        cuenta.setId(entity.getId());
        cuenta.setNumeroCuenta(entity.getNumeroCuenta());
        cuenta.setTipoCuenta(entity.getTipoCuenta());
        cuenta.setSaldoInicial(entity.getSaldoInicial());
        cuenta.setSaldoDisponible(entity.getSaldoDisponible());
        cuenta.setEstado(entity.getEstado());
        cuenta.setClienteId(entity.getClienteId());
        return cuenta;
    }
}
