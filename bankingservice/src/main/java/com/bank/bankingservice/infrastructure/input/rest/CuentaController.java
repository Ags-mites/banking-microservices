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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaUseCase cuentaUseCase;

    public CuentaController(CuentaUseCase cuentaUseCase) {
        this.cuentaUseCase = cuentaUseCase;
    }

    @PostMapping
    public ResponseEntity<CuentaResponse> createAccount(@RequestBody CuentaCreateRequest request) {
        Cuenta createdCuenta = cuentaUseCase.createAccount(
                request.clienteId(),
                request.numeroCuenta(),
                request.tipoCuenta(),
                request.saldoInicial()
        );

        CuentaResponse response = new CuentaResponse(
                createdCuenta.id(),
                createdCuenta.numeroCuenta(),
                createdCuenta.tipoCuenta(),
                createdCuenta.saldoInicial(),
                createdCuenta.saldoDisponible(),
                createdCuenta.estado(),
                createdCuenta.clienteId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CuentaResponse>> getAllAccounts() {
        List<CuentaResponse> responses = cuentaUseCase.getAllAccounts().stream()
                .map(c -> new CuentaResponse(c.id(), c.numeroCuenta(), c.tipoCuenta(), c.saldoInicial(), c.saldoDisponible(), c.estado(), c.clienteId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponse> getAccountById(@PathVariable Long id) {
        Cuenta c = cuentaUseCase.getAccountById(id);
        CuentaResponse response = new CuentaResponse(c.id(), c.numeroCuenta(), c.tipoCuenta(), c.saldoInicial(), c.saldoDisponible(), c.estado(), c.clienteId());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaResponse> updateAccount(@PathVariable Long id, @RequestBody CuentaUpdateRequest request) {
        Cuenta c = cuentaUseCase.updateAccount(id, request.tipoCuenta(), request.estado());
        CuentaResponse response = new CuentaResponse(c.id(), c.numeroCuenta(), c.tipoCuenta(), c.saldoInicial(), c.saldoDisponible(), c.estado(), c.clienteId());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CuentaResponse> patchAccount(@PathVariable Long id, @RequestBody CuentaUpdateRequest request) {
        return updateAccount(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        cuentaUseCase.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
