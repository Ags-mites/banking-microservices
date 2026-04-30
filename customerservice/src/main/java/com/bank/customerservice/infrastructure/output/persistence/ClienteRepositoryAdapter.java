package com.bank.customerservice.infrastructure.output.persistence;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.bank.customerservice.domain.model.Cliente;
import com.bank.customerservice.domain.ports.out.ClienteRepository;

@Repository
public class ClienteRepositoryAdapter implements ClienteRepository {
    
    private final ClienteJpaRepository jpaRepository;
    
    public ClienteRepositoryAdapter(ClienteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Cliente guardar(Cliente cliente) {
        ClienteEntity entity = toEntity(cliente);
        ClienteEntity guardado = jpaRepository.save(entity);
        return toDomain(guardado);
    }
    
    @Override
    public Cliente buscarPorId(Long id) {
        return jpaRepository.findById(id)
            .map(this::toDomain)
            .orElse(null);
    }
    
    @Override
    public Cliente buscarPorPersonaId(Long personaId) {
        ClienteEntity entity = jpaRepository.findByPersonaId(personaId);
        return entity != null ? toDomain(entity) : null;
    }
    
    @Override
    public List<Cliente> obtenerTodos() {
        return jpaRepository.findAll()
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public Cliente actualizar(Cliente cliente) {
        ClienteEntity existente = jpaRepository.findById(cliente.id())
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + cliente.id()));
        
        existente.setContrasena(cliente.contrasena());
        existente.setEstado(cliente.estado());
        
        ClienteEntity actualizado = jpaRepository.save(existente);
        return toDomain(actualizado);
    }
    
    @Override
    public void eliminarPorId(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existePorId(Long id) {
        return jpaRepository.existsById(id);
    }
    
    // ─── Conversiones ──────────────────────────────────────────────────
    
    private Cliente toDomain(ClienteEntity entity) {
        return Cliente.crearDesdeRepository(
            entity.getId(),
            entity.getPersonaId(),
            entity.getContrasena(),
            entity.getEstado(),
            entity.getCreatedAt().toInstant(ZoneId.of("UTC").getRules().getOffset(java.time.LocalDateTime.now())),
            entity.getUpdatedAt().toInstant(ZoneId.of("UTC").getRules().getOffset(java.time.LocalDateTime.now()))
        );
    }
    
    private ClienteEntity toEntity(Cliente cliente) {
        ClienteEntity entity = new ClienteEntity();
        entity.setId(cliente.id());
        entity.setPersonaId(cliente.personaId());
        entity.setContrasena(cliente.contrasena());
        entity.setEstado(cliente.estado());
        if (cliente.createdAt() != null) {
            entity.setCreatedAt(java.time.LocalDateTime.ofInstant(
                cliente.createdAt(),
                ZoneId.of("UTC")
            ));
        }
        if (cliente.updatedAt() != null) {
            entity.setUpdatedAt(java.time.LocalDateTime.ofInstant(
                cliente.updatedAt(),
                ZoneId.of("UTC")
            ));
        }
        return entity;
    }
}
