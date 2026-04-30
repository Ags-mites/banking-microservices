package com.bank.bankingservice.infrastructure.output.persistence;

import com.bank.bankingservice.domain.model.Movimiento;
import com.bank.bankingservice.domain.model.MovimientoReporte;
import com.bank.bankingservice.domain.ports.out.MovimientoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MovimientoRepositoryAdapter implements MovimientoRepository {

    private final SpringDataMovimientoRepository springDataRepository;
    private final MovimientoMapper mapper;

    public MovimientoRepositoryAdapter(SpringDataMovimientoRepository springDataRepository, MovimientoMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Movimiento save(Movimiento movimiento) {
        MovimientoEntity entity = mapper.toEntity(movimiento);
        MovimientoEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Movimiento> findByCuentaId(Long cuentaId, int offset, int limit) {
        List<Movimiento> movimientos = springDataRepository.findByCuenta_IdOrderByFechaDesc(cuentaId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
        return slice(movimientos, offset, limit);
    }

    @Override
    public List<Movimiento> findByCuentaIdAndFechaRange(Long cuentaId, LocalDate from, LocalDate to) {
        List<Movimiento> movimientos = springDataRepository.findByCuenta_IdOrderByFechaDesc(cuentaId)
                .stream()
                .map(mapper::toDomain)
                .filter(movimiento -> isWithinRange(movimiento, from, to))
                .collect(Collectors.toList());
        return movimientos;
    }

    @Override
    public List<MovimientoReporte> findReporteByClienteIdAndFechaBetween(Long clienteId, LocalDateTime start, LocalDateTime end) {
        return springDataRepository.findReporteByClienteIdAndFechaBetween(clienteId, start, end)
                .stream()
                .map(this::toDomainReporte)
                .collect(Collectors.toList());
    }

    private boolean isWithinRange(Movimiento movimiento, LocalDate from, LocalDate to) {
        if (movimiento.fecha() == null) {
            return false;
        }

        LocalDate fecha = movimiento.fecha().atZone(ZoneOffset.UTC).toLocalDate();
        boolean afterFrom = from == null || !fecha.isBefore(from);
        boolean beforeTo = to == null || !fecha.isAfter(to);
        return afterFrom && beforeTo;
    }

    private List<Movimiento> slice(List<Movimiento> movimientos, int offset, int limit) {
        if (movimientos.isEmpty()) {
            return movimientos;
        }
        int safeOffset = Math.max(0, offset);
        int safeLimit = limit <= 0 ? movimientos.size() : limit;
        if (safeOffset >= movimientos.size()) {
            return List.of();
        }
        int endIndex = Math.min(movimientos.size(), safeOffset + safeLimit);
        return movimientos.subList(safeOffset, endIndex);
    }

    private MovimientoReporte toDomainReporte(MovimientoReporteProjection projection) {
        return MovimientoReporte.reconstituir(
                projection.getFecha() != null ? projection.getFecha().toInstant(ZoneOffset.UTC) : null,
                projection.getCliente(),
                projection.getNumeroCuenta(),
                projection.getTipo(),
                projection.getSaldoInicial(),
                projection.getEstado(),
                projection.getMovimiento(),
                projection.getSaldoDisponible()
        );
    }
}