package com.bank.customerservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bank.customerservice.domain.model.Cliente.PasswordHasher;
import com.bank.customerservice.domain.ports.in.ClienteUseCase;
import com.bank.customerservice.domain.ports.out.ClienteRepository;
import com.bank.customerservice.domain.ports.out.PersonaRepository;
import com.bank.customerservice.infrastructure.messaging.ClienteEventPublisher;
import com.bank.customerservice.infrastructure.output.persistence.ClienteJpaRepository;
import com.bank.customerservice.infrastructure.output.persistence.ClienteRepositoryAdapter;
import com.bank.customerservice.infrastructure.output.persistence.PersonaJpaRepository;
import com.bank.customerservice.infrastructure.output.persistence.PersonaRepositoryAdapter;
import com.bank.customerservice.infrastructure.output.security.BCryptPasswordEncoder;
import com.bank.customerservice.application.service.ClienteService;

@Configuration
public class ApplicationConfig {
    
    @Bean
    public PasswordHasher passwordHasher() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public PersonaRepository personaRepository(PersonaJpaRepository jpaRepository) {
        return new PersonaRepositoryAdapter(jpaRepository);
    }
    
    @Bean
    public ClienteRepository clienteRepository(ClienteJpaRepository jpaRepository) {
        return new ClienteRepositoryAdapter(jpaRepository);
    }
    
    @Bean
    public ClienteUseCase clienteUseCase(
            ClienteRepository clienteRepository,
            PersonaRepository personaRepository,
            ClienteEventPublisher eventPublisher,
            PasswordHasher passwordHasher) {
        return new ClienteService(
            clienteRepository,
            personaRepository,
            eventPublisher,
            passwordHasher
        );
    }
}
