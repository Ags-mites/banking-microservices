package com.bank.customerservice.infrastructure.output.persistence;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.bank.customerservice.domain.model.Persona;
import com.bank.customerservice.domain.ports.out.PersonaRepository;

@Repository
public class PersonaRepositoryAdapter implements PersonaRepository {
    
    private final PersonaJpaRepository jpaRepository;
    
    public PersonaRepositoryAdapter(PersonaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Persona guardar(Persona persona) {
        PersonaEntity entity = toEntity(persona);
        PersonaEntity guardada = jpaRepository.save(entity);
        return toDomain(guardada);
    }
    
    @Override
    public Persona buscarPorId(Long id) {
        return jpaRepository.findById(id)
            .map(this::toDomain)
            .orElse(null);
    }
    
    @Override
    public Persona buscarPorIdentificacion(String identificacion) {
        PersonaEntity entity = jpaRepository.findByIdentificacion(identificacion);
        return entity != null ? toDomain(entity) : null;
    }
    
    @Override
    public boolean existePorIdentificacion(String identificacion) {
        return jpaRepository.existsByIdentificacion(identificacion);
    }
    
    @Override
    public Persona actualizar(Persona persona) {
        PersonaEntity existente = jpaRepository.findById(persona.id())
            .orElseThrow(() -> new RuntimeException("Persona no encontrada con id: " + persona.id()));
        
        existente.setNombre(persona.nombre());
        existente.setGenero(persona.genero());
        existente.setEdad(persona.edad());
        existente.setIdentificacion(persona.identificacion());
        existente.setDireccion(persona.direccion());
        existente.setTelefono(persona.telefono());
        
        PersonaEntity actualizada = jpaRepository.save(existente);
        return toDomain(actualizada);
    }
    
    @Override
    public List<Persona> obtenerTodas() {
        return jpaRepository.findAll()
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void eliminarPorId(Long id) {
        jpaRepository.deleteById(id);
    }
    
    // ─── Conversiones ──────────────────────────────────────────────────
    
    private Persona toDomain(PersonaEntity entity) {
        Persona persona = Persona.crear(
            entity.getNombre(),
            entity.getGenero(),
            entity.getEdad(),
            entity.getIdentificacion(),
            entity.getDireccion(),
            entity.getTelefono()
        );
        persona.setId(entity.getId());
        persona.setCreatedAt(entity.getCreatedAt().toInstant(ZoneId.of("UTC").getRules().getOffset(java.time.LocalDateTime.now())));
        persona.setUpdatedAt(entity.getUpdatedAt().toInstant(ZoneId.of("UTC").getRules().getOffset(java.time.LocalDateTime.now())));
        return persona;
    }
    
    private PersonaEntity toEntity(Persona persona) {
        PersonaEntity entity = new PersonaEntity();
        entity.setId(persona.id());
        entity.setNombre(persona.nombre());
        entity.setGenero(persona.genero());
        entity.setEdad(persona.edad());
        entity.setIdentificacion(persona.identificacion());
        entity.setDireccion(persona.direccion());
        entity.setTelefono(persona.telefono());
        if (persona.createdAt() != null) {
            entity.setCreatedAt(java.time.LocalDateTime.ofInstant(
                persona.createdAt(),
                ZoneId.of("UTC")
            ));
        }
        if (persona.updatedAt() != null) {
            entity.setUpdatedAt(java.time.LocalDateTime.ofInstant(
                persona.updatedAt(),
                ZoneId.of("UTC")
            ));
        }
        return entity;
    }
}
