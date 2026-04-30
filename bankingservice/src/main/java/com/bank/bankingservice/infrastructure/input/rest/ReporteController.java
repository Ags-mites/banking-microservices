package com.bank.bankingservice.infrastructure.input.rest;

import com.bank.bankingservice.application.dto.MovimientoReporteDto;
import com.bank.bankingservice.domain.model.MovimientoReporte;
import com.bank.bankingservice.domain.ports.in.ReporteEstadoCuentaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final DateTimeFormatter REPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReporteEstadoCuentaUseCase reporteEstadoCuentaUseCase;

    public ReporteController(ReporteEstadoCuentaUseCase reporteEstadoCuentaUseCase) {
        this.reporteEstadoCuentaUseCase = reporteEstadoCuentaUseCase;
    }

    @GetMapping
    public ResponseEntity<List<MovimientoReporteDto>> getReporte(@RequestParam(required = false) Long cliente,
                                                                 @RequestParam(required = false) String fecha) {
        List<MovimientoReporteDto> response = reporteEstadoCuentaUseCase.generarReporte(cliente, fecha)
                .stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(response);
    }

    private MovimientoReporteDto toDto(MovimientoReporte reporte) {
        String fecha = reporte.fecha() != null
                ? REPORT_DATE_FORMATTER.format(reporte.fecha().atZone(ZoneOffset.UTC).toLocalDate())
                : null;

        return new MovimientoReporteDto(
                fecha,
                reporte.cliente(),
                reporte.numeroCuenta(),
                reporte.tipo(),
                reporte.saldoInicial(),
                reporte.estado(),
                reporte.movimiento(),
                reporte.saldoDisponible()
        );
    }
}