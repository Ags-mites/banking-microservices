package com.bank.customerservice.infrastructure.input.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bank.customerservice.application.dto.ClienteCreateRequest;
import com.bank.customerservice.application.dto.ClienteUpdateRequest;
import com.bank.customerservice.application.dto.ClientePatchRequest;
import com.bank.customerservice.application.dto.ClienteResponse;
import com.bank.customerservice.domain.ports.in.ClienteUseCase;

/**
 * REST Controller para gestión de Clientes
 * Input Adapter: recibe solicitudes HTTP y delega a UseCases
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    
    private final ClienteUseCase clienteUseCase;
    
    public ClienteController(ClienteUseCase clienteUseCase) {
        this.clienteUseCase = clienteUseCase;
    }
    
    @PostMapping
    public ResponseEntity<ClienteResponse> crearCliente(@RequestBody ClienteCreateRequest request) {
        ClienteResponse response = clienteUseCase.crearCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtenerClientePorId(@PathVariable Long id) {
        ClienteResponse response = clienteUseCase.obtenerClientePorId(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> obtenerTodos() {
        List<ClienteResponse> clientes = clienteUseCase.obtenerTodos();
        return ResponseEntity.ok(clientes);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizarCliente(
        @PathVariable Long id,
        @RequestBody ClienteUpdateRequest request
    ) {
        ClienteResponse response = clienteUseCase.actualizarCliente(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizarClienteParcialmente(
        @PathVariable Long id,
        @RequestBody ClientePatchRequest request
    ) {
        ClienteResponse response = clienteUseCase.actualizarClienteParcialmente(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteUseCase.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
}
