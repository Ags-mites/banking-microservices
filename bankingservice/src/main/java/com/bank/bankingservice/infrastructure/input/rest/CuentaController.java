package com.bank.bankingservice.infrastructure.input.rest;

import com.bank.bankingservice.application.dto.CuentaCreateRequest;
import com.bank.bankingservice.application.dto.CuentaResponse;
import com.bank.bankingservice.application.dto.CuentaUpdateRequest;
import com.bank.bankingservice.domain.model.Cuenta;
import com.bank.bankingservice.domain.ports.in.CuentaUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaUseCase cuentaUseCase;

    public CuentaController(CuentaUseCase cuentaUseCase) {
        this.cuentaUseCase = cuentaUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody CuentaCreateRequest request) {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(request.numeroCuenta());
        cuenta.setTipoCuenta(request.tipoCuenta());
        cuenta.setSaldoInicial(request.saldoInicial());
        cuenta.setEstado(request.estado() != null ? request.estado() : true);
        cuenta.setClienteId(request.clienteId());

        Cuenta createdCuenta = cuentaUseCase.createAccount(cuenta);

        CuentaResponse response = new CuentaResponse(
                createdCuenta.getId(),
                createdCuenta.getNumeroCuenta(),
                createdCuenta.getTipoCuenta(),
                createdCuenta.getSaldoInicial(),
                createdCuenta.getSaldoDisponible(),
                createdCuenta.getEstado(),
                createdCuenta.getClienteId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "data", response,
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAccounts() {
        List<CuentaResponse> responses = cuentaUseCase.getAllAccounts().stream()
                .map(c -> new CuentaResponse(c.getId(), c.getNumeroCuenta(), c.getTipoCuenta(), c.getSaldoInicial(), c.getSaldoDisponible(), c.getEstado(), c.getClienteId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "data", responses,
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAccountById(@PathVariable Long id) {
        Cuenta c = cuentaUseCase.getAccountById(id);
        CuentaResponse response = new CuentaResponse(c.getId(), c.getNumeroCuenta(), c.getTipoCuenta(), c.getSaldoInicial(), c.getSaldoDisponible(), c.getEstado(), c.getClienteId());

        return ResponseEntity.ok(Map.of(
                "data", response,
                "timestamp", Instant.now().toString()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAccount(@PathVariable Long id, @RequestBody CuentaUpdateRequest request) {
        Cuenta updateData = new Cuenta();
        updateData.setTipoCuenta(request.tipoCuenta());
        updateData.setEstado(request.estado());

        Cuenta c = cuentaUseCase.updateAccount(id, updateData);
        CuentaResponse response = new CuentaResponse(c.getId(), c.getNumeroCuenta(), c.getTipoCuenta(), c.getSaldoInicial(), c.getSaldoDisponible(), c.getEstado(), c.getClienteId());

        return ResponseEntity.ok(Map.of(
                "data", response,
                "timestamp", Instant.now().toString()
        ));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> patchAccount(@PathVariable Long id, @RequestBody CuentaUpdateRequest request) {
        return updateAccount(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        cuentaUseCase.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
