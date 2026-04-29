package com.bank.customerservice.infrastructure.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaJpaRepository extends JpaRepository<PersonaEntity, Long> {
    
    PersonaEntity findByIdentificacion(String identificacion);
    
    boolean existsByIdentificacion(String identificacion);
}
