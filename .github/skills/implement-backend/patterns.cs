/*
 * patterns.java — Patrones de referencia para el agente Backend Developer en Spring Boot 3.x / Java 17+
 * Este archivo NO se ejecuta directamente. Es una referencia para que el agente
 * genere código consistente con la arquitectura Hexagonal del proyecto.
 * 
 * Uso: Cuando generes una feature nueva, sigue estos patrones para cada capa.
 */

// ─── DOMAIN LAYER: VALUE OBJECT ──────────────────────────────────────────────
package com.example.banking.domain.model;

/**
 * Value Object inmutable — encapsula lógica de un concepto de dominio.
 */
public class Email {
    private final String value;
    
    private Email(String value) {
        this.value = value;
    }
    
    public static Email create(String value) {
        if (value == null || value.isBlank() || !value.contains("@")) {
            throw new InvalidEmailException("Email format is invalid");
        }
        return new Email(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}

// ─── DOMAIN LAYER: ENTITY (JPA) ───────────────────────────────────────────
package com.example.banking.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidad JPA — contiene toda la lógica de cambios y validaciones de negocio.
 * - Constructor protegido para JPA
 * - Propiedades mutables solo via métodos de dominio
 * - Version para concurrencia optimista
 */
@Entity
@Table(name = "features", schema = "banking_service")
public class Feature {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "uid", nullable = false, unique = true)
    private String uid;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "is_archived")
    private boolean archived;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Integer version;
    
    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    protected Feature() {
        // Para JPA
    }
    
    public Feature(String uid, String name, String description) {
        this.uid = uid;
        this.name = name;
        this.description = description;
        this.archived = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.version = 0;
        raiseDomainEvent(new FeatureCreatedEvent(uid, name));
    }
    
    // Getters
    public Long getId() { return id; }
    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isArchived() { return archived; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Integer getVersion() { return version; }
    
    // Métodos de negocio
    public void updateDetails(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new InvalidFeatureDetailsException("Name cannot be empty");
        }
        this.name = name;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
        this.version++;
        raiseDomainEvent(new FeatureUpdatedEvent(uid, name));
    }
    
    public void archive() {
        if (archived) {
            throw new FeatureAlreadyArchivedException("Feature is already archived");
        }
        this.archived = true;
        this.updatedAt = LocalDateTime.now();
        this.version++;
        raiseDomainEvent(new FeatureArchivedEvent(uid));
    }
    
    // Métodos privados de dominio
    private void raiseDomainEvent(DomainEvent event) {
        domainEvents.add(event);
    }
    
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}

// ─── DOMAIN LAYER: EXCEPTIONS ───────────────────────────────────────────────
package com.example.banking.domain.exception;

public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}

public class InvalidFeatureDetailsException extends DomainException {
    public InvalidFeatureDetailsException(String message) {
        super(message);
    }
}

public class FeatureAlreadyArchivedException extends DomainException {
    public FeatureAlreadyArchivedException(String message) {
        super(message);
    }
}

public class InvalidEmailException extends DomainException {
    public InvalidEmailException(String message) {
        super(message);
    }
}

// ─── DOMAIN LAYER: DOMAIN EVENTS ────────────────────────────────────────────
package com.example.banking.domain.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredAt();
    String aggregateId();
}

public record FeatureCreatedEvent(String featureId, String name) implements DomainEvent {
    @Override
    public LocalDateTime occurredAt() {
        return LocalDateTime.now();
    }
    
    @Override
    public String aggregateId() {
        return featureId;
    }
}

public record FeatureUpdatedEvent(String featureId, String newName) implements DomainEvent {
    @Override
    public LocalDateTime occurredAt() {
        return LocalDateTime.now();
    }
    
    @Override
    public String aggregateId() {
        return featureId;
    }
}

public record FeatureArchivedEvent(String featureId) implements DomainEvent {
    @Override
    public LocalDateTime occurredAt() {
        return LocalDateTime.now();
    }
    
    @Override
    public String aggregateId() {
        return featureId;
    }
}

// ─── DOMAIN LAYER: REPOSITORY PORT (OUTPUT) ────────────────────────────────
package com.example.banking.domain.ports.out;

import com.example.banking.domain.model.Feature;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida — Interfaz de persistencia sin JPA.
 * Es la especificación que la capa de infraestructura debe implementar.
 */
public interface FeatureRepositoryPort {
    Optional<Feature> findByUid(String uid);
    List<Feature> findAll();
    Feature save(Feature feature);
    void delete(Feature feature);
}

// ─── DOMAIN LAYER: USE CASE PORT (INPUT) ──────────────────────────────────
package com.example.banking.domain.ports.in;

import com.example.banking.application.dto.CreateFeatureDto;
import com.example.banking.application.dto.FeatureResponseDto;
import com.example.banking.application.dto.UpdateFeatureDto;
import java.util.List;

/**
 * Puerto de entrada — Casos de uso del dominio.
 */
public interface FeatureUseCase {
    FeatureResponseDto createFeature(CreateFeatureDto dto);
    FeatureResponseDto getFeatureByUid(String uid);
    List<FeatureResponseDto> getAllFeatures();
    FeatureResponseDto updateFeature(String uid, UpdateFeatureDto dto);
    void archiveFeature(String uid);
}

// ─── APPLICATION LAYER: DTOs ────────────────────────────────────────────────
package com.example.banking.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTOs como records para inmutabilidad.
 */
public record CreateFeatureDto(
    @NotBlank String uid,
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String description
) {}

public record UpdateFeatureDto(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String description
) {}

public record FeatureResponseDto(
    Long id,
    String uid,
    String name,
    String description,
    boolean isArchived,
    LocalDateTime createdAt,
    Integer version
) {}

// ─── APPLICATION LAYER: MAPPER (MapStruct) ─────────────────────────────────
package com.example.banking.application.mapper;

import com.example.banking.domain.model.Feature;
import com.example.banking.application.dto.CreateFeatureDto;
import com.example.banking.application.dto.FeatureResponseDto;
import com.example.banking.application.dto.UpdateFeatureDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeatureMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Feature toEntity(CreateFeatureDto dto);
    
    FeatureResponseDto toResponseDto(Feature entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(UpdateFeatureDto dto, Feature entity);
}

// ─── DOMAIN LAYER: SERVICE (USE CASE IMPLEMENTATION) ───────────────────────
package com.example.banking.domain.service;

import com.example.banking.domain.model.Feature;
import com.example.banking.domain.ports.in.FeatureUseCase;
import com.example.banking.domain.ports.out.FeatureRepositoryPort;
import com.example.banking.application.mapper.FeatureMapper;
import com.example.banking.application.dto.CreateFeatureDto;
import com.example.banking.application.dto.FeatureResponseDto;
import com.example.banking.application.dto.UpdateFeatureDto;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de dominio — lógica de orquestación para casos de uso.
 * - Instancia el agregado
 * - Persiste cambios
 * - Publica eventos
 * - Convierte a DTO
 */
@Service
public class FeatureService implements FeatureUseCase {
    
    private final FeatureRepositoryPort repository;
    private final FeatureMapper mapper;
    
    public FeatureService(FeatureRepositoryPort repository, FeatureMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    @Override
    public FeatureResponseDto createFeature(CreateFeatureDto dto) {
        Feature feature = mapper.toEntity(dto);
        Feature saved = repository.save(feature);
        publishDomainEvents(saved);
        return mapper.toResponseDto(saved);
    }
    
    @Override
    public FeatureResponseDto getFeatureByUid(String uid) {
        Feature feature = repository.findByUid(uid)
            .orElseThrow(() -> new IllegalArgumentException("Feature not found: " + uid));
        return mapper.toResponseDto(feature);
    }
    
    @Override
    public List<FeatureResponseDto> getAllFeatures() {
        return repository.findAll().stream()
            .map(mapper::toResponseDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public FeatureResponseDto updateFeature(String uid, UpdateFeatureDto dto) {
        Feature feature = repository.findByUid(uid)
            .orElseThrow(() -> new IllegalArgumentException("Feature not found: " + uid));
        
        mapper.updateEntityFromDto(dto, feature);
        feature.updateDetails(dto.name(), dto.description());
        
        Feature saved = repository.save(feature);
        publishDomainEvents(saved);
        return mapper.toResponseDto(saved);
    }
    
    @Override
    public void archiveFeature(String uid) {
        Feature feature = repository.findByUid(uid)
            .orElseThrow(() -> new IllegalArgumentException("Feature not found: " + uid));
        
        feature.archive();
        repository.save(feature);
        publishDomainEvents(feature);
    }
    
    private void publishDomainEvents(Feature feature) {
        // Aquí se publicarían los eventos via ApplicationEventPublisher o mensajería
        feature.clearDomainEvents();
    }
}

// ─── INFRASTRUCTURE LAYER: JPA REPOSITORY ──────────────────────────────────
package com.example.banking.infrastructure.output;

import com.example.banking.domain.model.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FeatureJpaRepository extends JpaRepository<Feature, Long> {
    Optional<Feature> findByUid(String uid);
}

// ─── INFRASTRUCTURE LAYER: REPOSITORY ADAPTER ──────────────────────────────
package com.example.banking.infrastructure.output;

import com.example.banking.domain.model.Feature;
import com.example.banking.domain.ports.out.FeatureRepositoryPort;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class FeatureRepositoryAdapter implements FeatureRepositoryPort {
    
    private final FeatureJpaRepository jpaRepository;
    
    public FeatureRepositoryAdapter(FeatureJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Optional<Feature> findByUid(String uid) {
        return jpaRepository.findByUid(uid);
    }
    
    @Override
    public List<Feature> findAll() {
        return jpaRepository.findAll().stream()
            .collect(Collectors.toList());
    }
    
    @Override
    public Feature save(Feature feature) {
        return jpaRepository.save(feature);
    }
    
    @Override
    public void delete(Feature feature) {
        jpaRepository.delete(feature);
    }
}

// ─── INFRASTRUCTURE LAYER: REST CONTROLLER ────────────────────────────────
package com.example.banking.infrastructure.input;

import com.example.banking.domain.ports.in.FeatureUseCase;
import com.example.banking.application.dto.CreateFeatureDto;
import com.example.banking.application.dto.FeatureResponseDto;
import com.example.banking.application.dto.UpdateFeatureDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/features")
public class FeatureController {
    
    private final FeatureUseCase featureUseCase;
    
    public FeatureController(FeatureUseCase featureUseCase) {
        this.featureUseCase = featureUseCase;
    }
    
    @PostMapping
    public ResponseEntity<FeatureResponseDto> createFeature(@Valid @RequestBody CreateFeatureDto dto) {
        FeatureResponseDto response = featureUseCase.createFeature(dto);
        return ResponseEntity.status(201).body(response);
    }
    
    @GetMapping("/{uid}")
    public ResponseEntity<FeatureResponseDto> getFeatureByUid(@PathVariable String uid) {
        return ResponseEntity.ok(featureUseCase.getFeatureByUid(uid));
    }
    
    @GetMapping
    public ResponseEntity<List<FeatureResponseDto>> getAllFeatures() {
        return ResponseEntity.ok(featureUseCase.getAllFeatures());
    }
    
    @PutMapping("/{uid}")
    public ResponseEntity<FeatureResponseDto> updateFeature(
        @PathVariable String uid,
        @Valid @RequestBody UpdateFeatureDto dto
    ) {
        return ResponseEntity.ok(featureUseCase.updateFeature(uid, dto));
    }
    
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> archiveFeature(@PathVariable String uid) {
        featureUseCase.archiveFeature(uid);
        return ResponseEntity.noContent().build();
    }
}

// ─── INFRASTRUCTURE LAYER: CONFIGURATION ─────────────────────────────────
package com.example.banking.infrastructure.config;

import com.example.banking.domain.ports.out.FeatureRepositoryPort;
import com.example.banking.infrastructure.output.FeatureJpaRepository;
import com.example.banking.infrastructure.output.FeatureRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    
    @Bean
    public FeatureRepositoryPort featureRepositoryPort(FeatureJpaRepository jpaRepository) {
        return new FeatureRepositoryAdapter(jpaRepository);
    }
}
