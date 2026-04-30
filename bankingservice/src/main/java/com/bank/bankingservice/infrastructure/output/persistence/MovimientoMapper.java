package com.bank.bankingservice.infrastructure.output.persistence;

import com.bank.bankingservice.application.dto.MovimientoListResponse;
import com.bank.bankingservice.domain.model.Cuenta;
import com.bank.bankingservice.domain.model.Movimiento;
import com.bank.bankingservice.domain.model.MovimientoType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import jakarta.persistence.EntityManager;

@Component
public class MovimientoMapper {

    private final EntityManager entityManager;
    private static final DateTimeFormatter LIST_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MovimientoMapper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public MovimientoEntity toEntity(Movimiento movimiento) {
        if (movimiento == null) {
            return null;
        }

        MovimientoEntity entity = new MovimientoEntity();
        entity.setId(movimiento.id());
        if (movimiento.fecha() != null) {
            entity.setFecha(LocalDateTime.ofInstant(movimiento.fecha(), ZoneOffset.UTC));
        }
        entity.setTipoMovimiento(movimiento.tipoMovimiento().label());
        entity.setValor(movimiento.valor());
        entity.setSaldo(movimiento.saldo());

        CuentaEntity cuenta = entityManager.getReference(CuentaEntity.class, movimiento.cuentaId());
        entity.setCuenta(cuenta);
        return entity;
    }

    public Movimiento toDomain(MovimientoEntity entity) {
        if (entity == null) {
            return null;
        }
        return Movimiento.reconstituir(
                entity.getId(),
                entity.getFecha() != null ? entity.getFecha().toInstant(ZoneOffset.UTC) : null,
                MovimientoType.fromLabel(entity.getTipoMovimiento()),
                entity.getValor(),
                entity.getSaldo(),
                entity.getCuenta() != null ? entity.getCuenta().getId() : null
        );
    }

    public MovimientoListResponse toListResponse(Movimiento movimiento, Cuenta cuenta, String clienteNombre) {
        String fecha = movimiento.fecha() != null
                ? LIST_DATE_FORMATTER.format(movimiento.fecha().atZone(ZoneOffset.UTC).toLocalDate())
                : null;

        return new MovimientoListResponse(
                fecha,
                clienteNombre,
                cuenta.numeroCuenta(),
                cuenta.tipoCuenta(),
                movimiento.saldo().subtract(movimiento.valor()),
                cuenta.estado(),
                movimiento.valor().abs(),
                movimiento.saldo()
        );
    }
}