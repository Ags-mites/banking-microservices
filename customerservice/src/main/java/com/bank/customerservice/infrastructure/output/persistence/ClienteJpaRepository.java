package com.bank.customerservice.infrastructure.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {
    
    ClienteEntity findByPersonaId(Long personaId);
}
