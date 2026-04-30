package com.bank.bankingservice.domain.ports.in;

import com.bank.bankingservice.domain.model.Movimiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MovimientoUseCase {
    Movimiento registrarDeposito(Long cuentaId, BigDecimal valor);

    Movimiento registrarRetiro(Long cuentaId, BigDecimal valor);

    List<Movimiento> listarMovimientosPorCuenta(Long cuentaId, int offset, int limit);

    List<Movimiento> listarMovimientosPorCuentaEnRango(Long cuentaId, LocalDate from, LocalDate to, int offset, int limit);
}