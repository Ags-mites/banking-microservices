package com.bank.customerservice.application.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.customerservice.application.dto.ClienteCreateRequest;
import com.bank.customerservice.application.dto.ClienteUpdateRequest;
import com.bank.customerservice.application.dto.ClientePatchRequest;
import com.bank.customerservice.application.dto.ClienteResponse;
import com.bank.customerservice.application.dto.ClienteCreadoEvent;
import com.bank.customerservice.domain.model.Cliente;
import com.bank.customerservice.domain.model.Cliente.PasswordHasher;
import com.bank.customerservice.domain.model.Genero;
import com.bank.customerservice.domain.model.Persona;
import com.bank.customerservice.domain.exception.ClienteNotFoundException;
import com.bank.customerservice.domain.exception.IdentificacionDuplicadaException;
import com.bank.customerservice.domain.ports.in.ClienteUseCase;
import com.bank.customerservice.domain.ports.out.ClienteRepository;
import com.bank.customerservice.domain.ports.out.PersonaRepository;
import com.bank.customerservice.infrastructure.messaging.ClienteEventPublisher;

@Service
@Transactional
public class ClienteService implements ClienteUseCase {
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final ClienteEventPublisher eventPublisher;
    private final PasswordHasher passwordHasher;
    
    public ClienteService(
        ClienteRepository clienteRepository,
        PersonaRepository personaRepository,
        ClienteEventPublisher eventPublisher,
        PasswordHasher passwordHasher
    ) {
        this.clienteRepository = clienteRepository;
        this.personaRepository = personaRepository;
        this.eventPublisher = eventPublisher;
        this.passwordHasher = passwordHasher;
    }
    
    @Override
    public ClienteResponse crearCliente(ClienteCreateRequest request) {

        if (personaRepository.existePorIdentificacion(request.identificacion())) {
            throw new IdentificacionDuplicadaException(request.identificacion());
        }
        
        Genero genero = request.genero() != null ? Genero.valueOf(request.genero()) : null;
        Persona persona = Persona.crear(
            request.nombre(),
            genero,
            request.edad(),
            request.identificacion(),
            request.direccion(),
            request.telefono()
        );
        Persona personaGuardada = personaRepository.guardar(persona);
        
        Cliente cliente = Cliente.crear(
            personaGuardada.id(),
            request.contrasena(),
            request.estado(),
            passwordHasher
        );
        Cliente clienteGuardado = clienteRepository.guardar(cliente);
        
        ClienteCreadoEvent event = new ClienteCreadoEvent(
            clienteGuardado.id(),
            personaGuardada.identificacion(),
            personaGuardada.nombre()
        );
        try {
            eventPublisher.publicarClienteCreadoEvent(event);
        } catch (Exception e) {
            System.err.println("Error al publicar evento cliente.creado: " + e.getMessage());
        }
        
        return toClienteResponse(clienteGuardado, personaGuardada);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.buscarPorId(id);
        if (cliente == null) {
            throw new ClienteNotFoundException(id);
        }
        
        // Obtener los datos de persona
        Persona persona = personaRepository.buscarPorId(cliente.personaId());
        
        return toClienteResponse(cliente, persona);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> obtenerTodos() {
        List<Cliente> clientes = clienteRepository.obtenerTodos();
        
        return clientes.stream()
            .map(cliente -> {
                Persona persona = personaRepository.buscarPorId(cliente.personaId());
                return toClienteResponse(cliente, persona);
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public ClienteResponse actualizarCliente(Long id, ClienteUpdateRequest request) {
        Cliente cliente = clienteRepository.buscarPorId(id);
        if (cliente == null) {
            throw new ClienteNotFoundException(id);
        }
        
        Persona persona = personaRepository.buscarPorId(cliente.personaId());
        
        if (!persona.identificacion().equals(request.identificacion()) &&
            personaRepository.existePorIdentificacion(request.identificacion())) {
            throw new IdentificacionDuplicadaException(request.identificacion());
        }
        
        Genero genero = request.genero() != null ? Genero.valueOf(request.genero()) : persona.genero();
        persona.actualizar(
            request.nombre(),
            genero,
            request.edad(),
            request.identificacion(),
            request.direccion(),
            request.telefono()
        );
        Persona personaActualizada = personaRepository.actualizar(persona);
        
        cliente.actualizar(request.contrasena(), request.estado(), passwordHasher);
        Cliente clienteActualizado = clienteRepository.actualizar(cliente);
        
        return toClienteResponse(clienteActualizado, personaActualizada);
    }
    
    @Override
    public ClienteResponse actualizarClienteParcialmente(Long id, ClientePatchRequest request) {
        Cliente cliente = clienteRepository.buscarPorId(id);
        if (cliente == null) {
            throw new ClienteNotFoundException(id);
        }
        
        Persona persona = personaRepository.buscarPorId(cliente.personaId());
        
        if (request.identificacion() != null &&
            !persona.identificacion().equals(request.identificacion()) &&
            personaRepository.existePorIdentificacion(request.identificacion())) {
            throw new IdentificacionDuplicadaException(request.identificacion());
        }
        
        Genero genero = request.genero() != null ? Genero.valueOf(request.genero()) : null;
        persona.actualizar(
            request.nombre(),
            genero,
            request.edad(),
            request.identificacion(),
            request.direccion(),
            request.telefono()
        );
        Persona personaActualizada = personaRepository.actualizar(persona);
        
        cliente.actualizar(request.contrasena(), request.estado(), passwordHasher);
        Cliente clienteActualizado = clienteRepository.actualizar(cliente);
        
        return toClienteResponse(clienteActualizado, personaActualizada);
    }
    
    @Override
    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.buscarPorId(id);
        if (cliente == null) {
            throw new ClienteNotFoundException(id);
        }
        
        clienteRepository.eliminarPorId(id);
        personaRepository.eliminarPorId(cliente.personaId());
    }
    
    // ─── Utilidades ────────────────────────────────────────────────────────
    private ClienteResponse toClienteResponse(Cliente cliente, Persona persona) {
        String generoStr = persona.genero() != null ? persona.genero().toString() : null;
        
        LocalDateTime createdAtDateTime = LocalDateTime.ofInstant(
            cliente.createdAt(),
            ZoneId.of("UTC")
        );
        LocalDateTime updatedAtDateTime = LocalDateTime.ofInstant(
            cliente.updatedAt(),
            ZoneId.of("UTC")
        );
        
        String createdAtFormatted = createdAtDateTime.format(DATE_FORMATTER);
        String updatedAtFormatted = updatedAtDateTime.format(DATE_FORMATTER);
        
        return new ClienteResponse(
            cliente.id(),
            persona.nombre(),
            generoStr,
            persona.edad(),
            persona.identificacion(),
            persona.direccion(),
            persona.telefono(),
            cliente.estado(),
            createdAtFormatted,
            updatedAtFormatted
        );
    }
}
