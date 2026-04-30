package com.bank.bankingservice.infrastructure.output.persistence;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataMovimientoRepository extends JpaRepository<MovimientoEntity, Long> {
    List<MovimientoEntity> findByCuenta_IdOrderByFechaDesc(Long cuentaId);

    @Query("""
        select new com.bank.bankingservice.infrastructure.output.persistence.MovimientoReporteProjection(
            m.fecha,
            cr.nombre,
            c.numeroCuenta,
            c.tipoCuenta,
            (m.saldo - m.valor),
            c.estado,
            m.valor,
            m.saldo
        )
        from MovimientoEntity m
        join m.cuenta c
        join ClienteRefEntity cr on cr.clienteId = c.clienteId
        where c.clienteId = :clienteId
          and m.fecha between :start and :end
        order by m.fecha desc
    """)
    List<MovimientoReporteProjection> findReporteByClienteIdAndFechaBetween(
            @Param("clienteId") Long clienteId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}