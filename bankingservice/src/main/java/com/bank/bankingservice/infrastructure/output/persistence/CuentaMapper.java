package com.bank.bankingservice.infrastructure.output.persistence;

import com.bank.bankingservice.domain.model.Cuenta;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class CuentaMapper {
    public CuentaEntity toEntity(Cuenta cuenta) {
        if (cuenta == null) {
            return null;
        }
        CuentaEntity entity = new CuentaEntity();
        entity.setId(cuenta.id());
        entity.setNumeroCuenta(cuenta.numeroCuenta());
        entity.setTipoCuenta(cuenta.tipoCuenta());
        entity.setSaldoInicial(cuenta.saldoInicial());
        entity.setSaldoDisponible(cuenta.saldoDisponible());
        entity.setEstado(cuenta.estado());
        entity.setClienteId(cuenta.clienteId());
        entity.setVersion(cuenta.version());
        if (cuenta.createdAt() != null) {
            entity.setCreatedAt(LocalDateTime.ofInstant(cuenta.createdAt(), ZoneOffset.UTC));
        }
        if (cuenta.updatedAt() != null) {
            entity.setUpdatedAt(LocalDateTime.ofInstant(cuenta.updatedAt(), ZoneOffset.UTC));
        }
        return entity;
    }

    public Cuenta toDomain(CuentaEntity entity) {
        if (entity == null) {
            return null;
        }
        Instant createdAt = entity.getCreatedAt() != null ? entity.getCreatedAt().toInstant(ZoneOffset.UTC) : null;
        Instant updatedAt = entity.getUpdatedAt() != null ? entity.getUpdatedAt().toInstant(ZoneOffset.UTC) : null;

        return Cuenta.reconstituir(
                entity.getId(),
                entity.getNumeroCuenta(),
                entity.getTipoCuenta(),
                entity.getSaldoInicial(),
                entity.getSaldoDisponible(),
                entity.getEstado(),
                entity.getClienteId(),
                entity.getVersion(),
                createdAt,
                updatedAt
        );
    }
}
