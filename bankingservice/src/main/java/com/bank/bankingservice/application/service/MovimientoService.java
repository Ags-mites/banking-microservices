package com.bank.bankingservice.application.service;

import com.bank.bankingservice.domain.exception.AccountNotFoundException;
import com.bank.bankingservice.domain.model.Cuenta;
import com.bank.bankingservice.domain.model.Movimiento;
import com.bank.bankingservice.domain.ports.in.MovimientoUseCase;
import com.bank.bankingservice.domain.ports.out.CuentaRepository;
import com.bank.bankingservice.domain.ports.out.MovimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovimientoService implements MovimientoUseCase {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    public MovimientoService(MovimientoRepository movimientoRepository, CuentaRepository cuentaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    @Transactional
    public Movimiento registrarDeposito(Long cuentaId, BigDecimal valor) {
        Cuenta cuenta = getCuentaOrThrow(cuentaId);
        Movimiento movimiento = Movimiento.registrarDeposito(cuentaId, valor);
        movimiento.registrar(cuenta.saldoDisponible());
        cuenta.actualizarSaldoDisponible(movimiento.saldo());
        cuentaRepository.save(cuenta);
        return movimientoRepository.save(movimiento);
    }

    @Override
    @Transactional
    public Movimiento registrarRetiro(Long cuentaId, BigDecimal valor) {
        Cuenta cuenta = getCuentaOrThrow(cuentaId);
        Movimiento movimiento = Movimiento.registrarRetiro(cuentaId, valor);
        movimiento.registrar(cuenta.saldoDisponible());
        cuenta.actualizarSaldoDisponible(movimiento.saldo());
        cuentaRepository.save(cuenta);
        return movimientoRepository.save(movimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movimiento> listarMovimientosPorCuenta(Long cuentaId, int offset, int limit) {
        getCuentaOrThrow(cuentaId);
        return applyPagination(movimientoRepository.findByCuentaId(cuentaId, 0, Integer.MAX_VALUE), offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movimiento> listarMovimientosPorCuentaEnRango(Long cuentaId, LocalDate from, LocalDate to, int offset, int limit) {
        getCuentaOrThrow(cuentaId);
        return applyPagination(movimientoRepository.findByCuentaIdAndFechaRange(cuentaId, from, to), offset, limit);
    }

    private Cuenta getCuentaOrThrow(Long cuentaId) {
        return cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new AccountNotFoundException(cuentaId));
    }

    private List<Movimiento> applyPagination(List<Movimiento> movimientos, int offset, int limit) {
        if (movimientos.isEmpty()) {
            return movimientos;
        }
        int safeOffset = Math.max(0, offset);
        int safeLimit = limit <= 0 ? movimientos.size() : limit;
        if (safeOffset >= movimientos.size()) {
            return List.of();
        }
        int endIndex = Math.min(movimientos.size(), safeOffset + safeLimit);
        return new ArrayList<>(movimientos.subList(safeOffset, endIndex));
    }
}