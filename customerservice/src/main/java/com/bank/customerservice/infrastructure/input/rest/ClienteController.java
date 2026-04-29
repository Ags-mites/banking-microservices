package com.bank.customerservice.infrastructure.input.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bank.customerservice.application.dto.ClienteCreateRequest;
import com.bank.customerservice.application.dto.ClienteUpdateRequest;
import com.bank.customerservice.application.dto.ClientePatchRequest;
import com.bank.customerservice.application.dto.ClienteResponse;
import com.bank.customerservice.domain.exception.ClienteNotFoundException;
import com.bank.customerservice.domain.exception.IdentificacionDuplicadaException;
import com.bank.customerservice.domain.ports.in.ClienteUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * REST Controller para gestión de Clientes
 * Input Adapter: recibe solicitudes HTTP y delega a UseCases
 */
@Tag(name = "Clientes", description = "API para gestión integral de clientes y personas")
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    
    private final ClienteUseCase clienteUseCase;
    
    public ClienteController(ClienteUseCase clienteUseCase) {
        this.clienteUseCase = clienteUseCase;
    }
    
    @Operation(
        summary = "Crear un nuevo cliente",
        description = "Crea un nuevo cliente junto con su persona asociada en una sola operación transaccional"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos (contraseña < 8 caracteres, campos obligatorios faltantes)"),
        @ApiResponse(responseCode = "409", description = "La identificación ya está en uso")
    })
    @PostMapping
    public ResponseEntity<ClienteResponse> crearCliente(@RequestBody ClienteCreateRequest request) {
        try {
            ClienteResponse response = clienteUseCase.crearCliente(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IdentificacionDuplicadaException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(
        summary = "Obtener cliente por ID",
        description = "Retorna los datos completos de un cliente específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtenerClientePorId(
        @Parameter(description = "ID único del cliente", example = "1")
        @PathVariable Long id
    ) {
        try {
            ClienteResponse response = clienteUseCase.obtenerClientePorId(id);
            return ResponseEntity.ok(response);
        } catch (ClienteNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(
        summary = "Obtener todos los clientes",
        description = "Retorna un listado de todos los clientes registrados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Listado de clientes (puede estar vacío)")
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> obtenerTodos() {
        List<ClienteResponse> clientes = clienteUseCase.obtenerTodos();
        return ResponseEntity.ok(clientes);
    }
    
    @Operation(
        summary = "Actualizar cliente (completo)",
        description = "Actualiza todos los datos de un cliente existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "409", description = "La identificación ya está en uso")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizarCliente(
        @Parameter(description = "ID único del cliente", example = "1")
        @PathVariable Long id,
        @RequestBody ClienteUpdateRequest request
    ) {
        try {
            ClienteResponse response = clienteUseCase.actualizarCliente(id, request);
            return ResponseEntity.ok(response);
        } catch (ClienteNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IdentificacionDuplicadaException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(
        summary = "Actualizar cliente (parcial)",
        description = "Actualiza solo los campos proporcionados, preservando los demás"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "409", description = "La identificación ya está en uso")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizarClienteParcialmente(
        @Parameter(description = "ID único del cliente", example = "1")
        @PathVariable Long id,
        @RequestBody ClientePatchRequest request
    ) {
        try {
            ClienteResponse response = clienteUseCase.actualizarClienteParcialmente(id, request);
            return ResponseEntity.ok(response);
        } catch (ClienteNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IdentificacionDuplicadaException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(
        summary = "Eliminar cliente",
        description = "Elimina un cliente y su persona asociada del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(
        @Parameter(description = "ID único del cliente", example = "1")
        @PathVariable Long id
    ) {
        try {
            clienteUseCase.eliminarCliente(id);
            return ResponseEntity.noContent().build();
        } catch (ClienteNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
