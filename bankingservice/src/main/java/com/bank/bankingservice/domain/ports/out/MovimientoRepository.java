package com.bank.bankingservice.domain.ports.out;

import com.bank.bankingservice.domain.model.Movimiento;

import java.time.LocalDate;
import java.util.List;

public interface MovimientoRepository {
    Movimiento save(Movimiento movimiento);

    List<Movimiento> findByCuentaId(Long cuentaId, int offset, int limit);

    List<Movimiento> findByCuentaIdAndFechaRange(Long cuentaId, LocalDate from, LocalDate to);
}