package com.bank.bankingservice.infrastructure.output.persistence;

import com.bank.bankingservice.domain.model.ClienteRef;
import com.bank.bankingservice.domain.ports.out.ClienteRefRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Repository
public class ClienteRefRepositoryAdapter implements ClienteRefRepository {

    private final SpringDataClienteRefRepository springDataRepository;

    public ClienteRefRepositoryAdapter(SpringDataClienteRefRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<ClienteRef> findByClienteId(Long clienteId) {
        return springDataRepository.findByClienteId(clienteId).map(this::toDomain);
    }

    @Override
    public Optional<ClienteRef> findByIdentificacion(String identificacion) {
        return springDataRepository.findByIdentificacion(identificacion).map(this::toDomain);
    }

    @Override
    @Transactional
    public ClienteRef upsert(Long clienteId, String nombre, String identificacion) {
        ClienteRefEntity entity = springDataRepository.findByClienteId(clienteId)
                .orElseGet(() -> {
                    ClienteRefEntity newEntity = new ClienteRefEntity();
                    newEntity.setClienteId(clienteId);
                    return newEntity;
                });

        entity.setNombre(nombre);
        entity.setIdentificacion(identificacion);

        ClienteRefEntity persisted = springDataRepository.save(entity);
        return toDomain(persisted);
    }

    private ClienteRef toDomain(ClienteRefEntity entity) {
        Instant createdAt = entity.getCreatedAt() != null ? entity.getCreatedAt().toInstant(ZoneOffset.UTC) : null;
        Instant updatedAt = entity.getUpdatedAt() != null ? entity.getUpdatedAt().toInstant(ZoneOffset.UTC) : null;

        return ClienteRef.reconstituir(
                entity.getId(),
                entity.getClienteId(),
                entity.getNombre(),
                entity.getIdentificacion(),
                entity.getVersion(),
                createdAt,
                updatedAt
        );
    }
}