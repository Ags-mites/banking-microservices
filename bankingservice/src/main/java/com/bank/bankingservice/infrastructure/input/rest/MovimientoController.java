package com.bank.bankingservice.infrastructure.input.rest;

import com.bank.bankingservice.application.dto.MovimientoCreateRequest;
import com.bank.bankingservice.application.dto.MovimientoListResponse;
import com.bank.bankingservice.application.dto.MovimientoResponse;
import com.bank.bankingservice.domain.exception.AccountNotFoundException;
import com.bank.bankingservice.domain.model.Cuenta;
import com.bank.bankingservice.domain.model.Movimiento;
import com.bank.bankingservice.domain.model.MovimientoType;
import com.bank.bankingservice.domain.ports.in.MovimientoUseCase;
import com.bank.bankingservice.domain.ports.out.ClienteLookup;
import com.bank.bankingservice.domain.ports.out.CuentaRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    private final MovimientoUseCase movimientoUseCase;
    private final CuentaRepository cuentaRepository;
    private final ClienteLookup clienteLookup;

    public MovimientoController(MovimientoUseCase movimientoUseCase,
                                CuentaRepository cuentaRepository,
                                ClienteLookup clienteLookup) {
        this.movimientoUseCase = movimientoUseCase;
        this.cuentaRepository = cuentaRepository;
        this.clienteLookup = clienteLookup;
    }

    @PostMapping
    public ResponseEntity<MovimientoResponse> createMovimiento(@RequestBody MovimientoCreateRequest request) {
        Movimiento movimiento = toMovimiento(request);
        MovimientoResponse response = new MovimientoResponse(
                movimiento.id(),
                movimiento.fecha() != null ? ISO_INSTANT.format(movimiento.fecha()) : null,
                movimiento.tipoMovimiento().label(),
                movimiento.valor().abs(),
                movimiento.saldo(),
                movimiento.cuentaId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MovimientoListResponse>> listMovimientos(
            @RequestParam Long cuentaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {

        List<Movimiento> movimientos = (from != null || to != null)
                ? movimientoUseCase.listarMovimientosPorCuentaEnRango(cuentaId, from, to, offset, limit)
                : movimientoUseCase.listarMovimientosPorCuenta(cuentaId, offset, limit);

        Cuenta cuenta = getCuenta(cuentaId);
        String clienteNombre = clienteLookup.findNombreById(cuenta.clienteId());

        List<MovimientoListResponse> responses = movimientos.stream()
                .map(movimiento -> toListResponse(movimiento, cuenta, clienteNombre))
                .toList();

        return ResponseEntity.ok(responses);
    }

    private Movimiento toMovimiento(MovimientoCreateRequest request) {
        MovimientoType movimientoType = MovimientoType.fromLabel(request.tipo());

        return switch (movimientoType) {
            case DEPOSITO -> movimientoUseCase.registrarDeposito(request.cuentaId(), request.valor());
            case RETIRO -> movimientoUseCase.registrarRetiro(request.cuentaId(), request.valor());
        };
    }

    private MovimientoListResponse toListResponse(Movimiento movimiento, Cuenta cuenta, String clienteNombre) {
        String fecha = movimiento.fecha() != null
                ? movimiento.fecha().atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
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

    private Cuenta getCuenta(Long cuentaId) {
        return cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new AccountNotFoundException(cuentaId));
    }
}