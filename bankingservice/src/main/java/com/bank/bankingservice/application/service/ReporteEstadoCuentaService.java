package com.bank.bankingservice.application.service;

import com.bank.bankingservice.domain.exception.ClienteNotFoundException;
import com.bank.bankingservice.domain.exception.InvalidDateFormatException;
import com.bank.bankingservice.domain.exception.InvalidDateRangeException;
import com.bank.bankingservice.domain.exception.MissingParameterException;
import com.bank.bankingservice.domain.model.ClienteRef;
import com.bank.bankingservice.domain.model.MovimientoReporte;
import com.bank.bankingservice.domain.ports.in.ReporteEstadoCuentaUseCase;
import com.bank.bankingservice.domain.ports.out.ClienteRefRepository;
import com.bank.bankingservice.domain.ports.out.MovimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class ReporteEstadoCuentaService implements ReporteEstadoCuentaUseCase {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MovimientoRepository movimientoRepository;
    private final ClienteRefRepository clienteRefRepository;

    public ReporteEstadoCuentaService(MovimientoRepository movimientoRepository,
                                      ClienteRefRepository clienteRefRepository) {
        this.movimientoRepository = movimientoRepository;
        this.clienteRefRepository = clienteRefRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoReporte> generarReporte(Long clienteId, String fecha) {
        Long safeClienteId = validateClienteId(clienteId);
        DateRange dateRange = parseDateRange(fecha);

        ClienteRef clienteRef = clienteRefRepository.findByClienteId(safeClienteId)
                .orElseThrow(() -> new ClienteNotFoundException("Client with id " + safeClienteId + " not found in local registry"));

        return movimientoRepository.findReporteByClienteIdAndFechaBetween(
                clienteRef.clienteId(),
                dateRange.start(),
                dateRange.end()
        );
    }

    private Long validateClienteId(Long clienteId) {
        if (clienteId == null) {
            throw new MissingParameterException("Missing required parameter: cliente");
        }
        return clienteId;
    }

    private DateRange parseDateRange(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            throw new MissingParameterException("Missing required parameter: fecha");
        }

        String[] parts = fecha.split(",");
        if (parts.length != 2) {
            throw new InvalidDateFormatException("Invalid date format. Expected dd/MM/yyyy");
        }

        try {
            LocalDate startDate = LocalDate.parse(parts[0].trim(), DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(parts[1].trim(), DATE_FORMATTER);

            if (startDate.isAfter(endDate)) {
                throw new InvalidDateRangeException("Start date must be before or equal to end date");
            }

            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);
            return new DateRange(start, end);
        } catch (DateTimeParseException ex) {
            throw new InvalidDateFormatException("Invalid date format. Expected dd/MM/yyyy");
        }
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {
    }
}