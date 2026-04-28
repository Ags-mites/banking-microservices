---
name: implement-backend
description: Implementa un feature completo en el backend con Spring Boot 3.x / Java 17+ y Arquitectura Hexagonal. Requiere spec con status APPROVED en .github/specs/.
argument-hint: "<nombre-feature>"
---

# Implement Backend — Spring Boot 3.x / Java 17+ Hexagonal Architecture

## Prerequisitos
1. **Leer spec**: [[specs/<feature>.spec.md]] — sección 2 (entidades, casos de uso, puertos)
2. **Stack & Arquitectura**: [[instructions/backend.instructions.md]] (Spring Boot 3.x, Hexagonal)
3. **Directrices generales**: [[docs/lineamientos/dev-guidelines.md]]
4. **BaseDatos.sql**: Ver esquema definido en el script SQL centralizado

---

## Algoritmo de Implementación

### Paso 1: Análisis de Dependencias
- Identificar entidades raíz del spec
- Listar Value Objects y sus propiedades
- Mapear dependencias entre agregados
- Identificar comunicación asíncrona (eventos de dominio)
- **Salida**: lista de archivos `.java` a crear por capa

### Paso 2: Modelado de Dominio — Entidades JPA con Lógica de Negocio

Crear carpeta `src/main/java/com/example/<service>/domain/` con:

- **model/**: Entities JPA puras con lógica de dominio
- **ports/out/**: Interfaces de salida (repositorios, clientes externos)
- **ports/in/**: Interfaces de entrada (casos de uso)
- **service/**: Implementación de casos de uso (lógica de negocio)

**Validador de Entidad — Obligatorio**:
```java
// ✅ CORRECTO: Entity con @Version y createdAt/updatedAt
package com.example.banking.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts", schema = "banking_service")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;
    
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    
    @Column(name = "balance", nullable = false)
    private Double balance;
    
    @Column(name = "account_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Integer version;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    protected Account() {}
    
    public static Account create(String accountNumber, Long clientId, AccountType type) {
        Account account = new Account();
        account.accountNumber = accountNumber;
        account.clientId = clientId;
        account.balance = 0.0;
        account.accountType = type;
        return account;
    }
    
    // Lógica de negocio
    public void deposit(Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.balance += amount;
    }
    
    public void withdraw(Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.balance < amount) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        this.balance -= amount;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public Long getClientId() { return clientId; }
    public Double getBalance() { return balance; }
    public AccountType getAccountType() { return accountType; }
}

// ❌ RECHAZADO EN CODE REVIEW: Sin @Version o createdAt/updatedAt
@Entity
public class Account {
    @Id
    private Long id;
    private Double balance;
    // Falta: @Version, createdAt, updatedAt → No pasa validación
}
```

**Regla Bloqueante**: Si una entidad no gestiona su propia consistencia (no tiene @Version, no audita createdAt/updatedAt), la implementación se rechaza.

### Paso 3: Puertos e Interfaces — Desacoplamiento

```java
// domain/ports/in/AccountUseCase.java - Input Port
public interface AccountUseCase {
    AccountResponseDto createAccount(CreateAccountDto dto);
    AccountResponseDto getAccountById(Long id);
    AccountResponseDto deposit(Long id, Double amount);
    AccountResponseDto withdraw(Long id, Double amount);
}

// domain/ports/out/AccountRepositoryPort.java - Output Port
public interface AccountRepositoryPort {
    Account save(Account account);
    Optional<Account> findById(Long id);
    List<Account> findByClientId(Long clientId);
    void delete(Account account);
}
```

### Paso 4: Casos de Uso — Lógica de Negocio

```java
// domain/service/AccountService.java
package com.example.banking.domain.service;

import com.example.banking.domain.model.Account;
import com.example.banking.domain.ports.in.AccountUseCase;
import com.example.banking.domain.ports.out.AccountRepositoryPort;
import com.example.banking.application.mapper.AccountMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService implements AccountUseCase {
    
    private final AccountRepositoryPort accountRepository;
    private final AccountMapper mapper;
    
    public AccountService(AccountRepositoryPort accountRepository, AccountMapper mapper) {
        this.accountRepository = accountRepository;
        this.mapper = mapper;
    }
    
    @Override
    public AccountResponseDto createAccount(CreateAccountDto dto) {
        // Verificar idempotencia
        List<Account> existing = accountRepository.findByClientId(dto.getClientId());
        if (!existing.isEmpty()) {
            return mapper.toResponseDto(existing.get(0));
        }
        
        Account account = Account.create(
            dto.getAccountNumber(), 
            dto.getClientId(), 
            dto.getAccountType()
        );
        Account saved = accountRepository.save(account);
        return mapper.toResponseDto(saved);
    }
    
    @Override
    public AccountResponseDto getAccountById(Long id) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
        return mapper.toResponseDto(account);
    }
    
    @Override
    public AccountResponseDto deposit(Long id, Double amount) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
        account.deposit(amount);
        Account saved = accountRepository.save(account);
        return mapper.toResponseDto(saved);
    }
    
    @Override
    public AccountResponseDto withdraw(Long id, Double amount) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
        account.withdraw(amount);
        Account saved = accountRepository.save(account);
        return mapper.toResponseDto(saved);
    }
}
```

### Paso 5: Application Layer — DTOs y Mappers

```java
// application/dto/CreateAccountDto.java
package com.example.banking.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountDto(
    @NotBlank String accountNumber,
    @NotNull Long clientId,
    @NotNull AccountType accountType
) {}

// application/dto/AccountResponseDto.java
public record AccountResponseDto(
    Long id,
    String accountNumber,
    Long clientId,
    Double balance,
    AccountType accountType,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

// application/mapper/AccountMapper.java
package com.example.banking.application.mapper;

import com.example.banking.domain.model.Account;
import com.example.banking.application.dto.CreateAccountDto;
import com.example.banking.application.dto.AccountResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Account toEntity(CreateAccountDto dto);
    
    AccountResponseDto toResponseDto(Account entity);
    
    List<AccountResponseDto> toResponseDtoList(List<Account> entities);
}
```

### Paso 6: Infraestructura — Repositorios e Input Adapters

```java
// infrastructure/output/AccountRepositoryAdapter.java
package com.example.banking.infrastructure.output;

import com.example.banking.domain.model.Account;
import com.example.banking.domain.ports.out.AccountRepositoryPort;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public class AccountRepositoryAdapter implements AccountRepositoryPort {
    
    private final AccountJpaRepository jpaRepository;
    
    public AccountRepositoryAdapter(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Account save(Account account) {
        return jpaRepository.save(account);
    }
    
    @Override
    public Optional<Account> findById(Long id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public List<Account> findByClientId(Long clientId) {
        return jpaRepository.findByClientId(clientId);
    }
    
    @Override
    public void delete(Account account) {
        jpaRepository.delete(account);
    }
}

// infrastructure/input/AccountController.java
package com.example.banking.infrastructure.input;

import com.example.banking.domain.ports.in.AccountUseCase;
import com.example.banking.application.dto.CreateAccountDto;
import com.example.banking.application.dto.AccountResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    
    private final AccountUseCase accountUseCase;
    
    public AccountController(AccountUseCase accountUseCase) {
        this.accountUseCase = accountUseCase;
    }
    
    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountDto dto) {
        AccountResponseDto response = accountUseCase.createAccount(dto);
        return ResponseEntity.status(201)
            .body(Map.of("data", response, "timestamp", LocalDateTime.now()));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDto> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountUseCase.getAccountById(id));
    }
    
    @PostMapping("/{id}/deposit")
    public ResponseEntity<?> deposit(
        @PathVariable Long id, 
        @RequestParam Double amount
    ) {
        AccountResponseDto response = accountUseCase.deposit(id, amount);
        return ResponseEntity.ok(Map.of("data", response, "timestamp", LocalDateTime.now()));
    }
}
```

### Paso 7: Repositorio JPA

```java
// infrastructure/output/AccountJpaRepository.java
package com.example.banking.infrastructure.output;

import com.example.banking.domain.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccountJpaRepository extends JpaRepository<Account, Long> {
    List<Account> findByClientId(Long clientId);
}
```

---

## Estructura de Carpetas (Hexagonal Standard)

```
src/main/java/com/example/<service>/
├── domain/
│   ├── model/                    # Entities JPA con lógica de negocio
│   │   ├── Account.java
│   │   └── Movement.java
│   ├── ports/
│   │   ├── in/                   # Input Ports (Use Cases)
│   │   │   └── AccountUseCase.java
│   │   └── out/                  # Output Ports (Repositories)
│   │       └── AccountRepositoryPort.java
│   ├── service/                  # Implementación de casos de uso
│   │   └── AccountService.java
│   └── exception/                # Excepciones de dominio
│       └── InsufficientFundsException.java
├── application/
│   ├── dto/                      # DTOs (requests, responses)
│   │   ├── CreateAccountDto.java
│   │   └── AccountResponseDto.java
│   └── mapper/                   # MapStruct mappers
│       └── AccountMapper.java
└── infrastructure/
    ├── input/                    # REST Controllers
    │   └── AccountController.java
    ├── output/                   # Repositorios JPA, clientes externos
    │   ├── AccountJpaRepository.java
    │   └── AccountRepositoryAdapter.java
    └── config/                   # Configuración Spring
        └── AppConfig.java
```

---

## Comandos Maven (./mvnw)

### Construir el proyecto
```bash
./mvnw clean install
```

### Ejecutar la aplicación
```bash
./mvnw spring-boot:run
```

### Ejecutar tests
```bash
./mvnw test
```

### Limpiar y recompilar
```bash
./mvnw clean verify
```

### Empaquetar para despliegue
```bash
./mvnw package -DskipTests
```

---

## Responsabilidades por Capa

| Capa | Archivo | Responsabilidad |
|------|---------|-----------------|
| **Domain** | `Account.java` | Lógica de negocio pura, reglas invariantes, validaciones |
| **Domain** | `AccountRepositoryPort.java` | Interfaz de persistencia (sin JPA) |
| **Domain** | `AccountUseCase.java` | Interfaz de caso de uso (puerto de entrada) |
| **Domain** | `AccountService.java` | Lógica de negocio, orquestación |
| **Application** | `CreateAccountDto.java` | Contrato de entrada HTTP |
| **Application** | `AccountResponseDto.java` | Contrato de salida HTTP |
| **Application** | `AccountMapper.java` | Mapeo DTO ↔ Entity (MapStruct) |
| **Infrastructure** | `AccountJpaRepository.java` | Spring Data JPA repository |
| **Infrastructure** | `AccountRepositoryAdapter.java` | Implementación de puerto de salida |
| **Infrastructure** | `AccountController.java` | Routing HTTP, validación entrada |

---

## Patrones de Código Java: Hexagonal Architecture

### Entidad con Lógica de Negocio
```java
@Entity
@Table(name = "accounts", schema = "banking_service")
public class Account {
    // ... campos, constructores, getters
    
    public void withdraw(Double amount) {
        if (amount <= 0) throw new IllegalArgumentException("...");
        if (this.balance < amount) throw new InsufficientFundsException("...");
        this.balance -= amount;
    }
}
```

### Inyección de Dependencias (Constructor)
```java
@Service
public class AccountService implements AccountUseCase {
    private final AccountRepositoryPort repository;
    private final AccountMapper mapper;
    
    public AccountService(AccountRepositoryPort repository, AccountMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
}
```

### Mapeo con MapStruct
```java
@Mapper(componentModel = "spring")
public interface AccountMapper {
    Account toEntity(CreateAccountDto dto);
    AccountResponseDto toResponseDto(Account entity);
}
```

### Control de Concurrencia Optimista
```java
@Version
@Column(name = "version")
private Integer version;
```

---

## Estándares de Código Java (Resumen Rápido)

| Estándar | ✅ Correcto | ❌ Evitar |
|----------|-----------|---------|
| **Records** | `record Dto(String name)` | `class Dto { set; get; }` (mutable) |
| **Optional** | `Optional<Account> findById()` | Retornar `null` |
| **Inyección** | Constructor injection | `@Autowired` field injection |
| **Async** | `@Async` o `CompletableFuture` | `.get()` bloqueante |
| **Excepciones** | `throw new BusinessException()` | `throw new RuntimeException()` |
| **Persistencia** | `@Repository` + JPA | Acceso directo a `EntityManager` |
| **Versión** | `@Version Integer version` | Sin control de concurrencia |
| **Timestamps** | `@PrePersist` / `@PreUpdate` | Sin auditoría automática |

---

## Validación de Completitud de la Skill

| Criterio | ✅ Validar |
|----------|----------|
| **Database-First** | Entidad mapea tabla definida en BaseDatos.sql |
| **Timestamps Obligatorios** | Entidad tiene `@PrePersist` y `@PreUpdate` para created_at / updated_at |
| **Concurrencia Optimista** | Entidad tiene `@Version` con `Integer version` |
| **Desacoplamiento** | Repositorio implementa interfaz del dominio (sin JPA en Domain) |
| **Inyección de Dependencias** | Todas las dependencias vía constructor (no `@Autowired`) |
| **Mapeo DTO → Entity** | MapStruct mapper registrado como Spring bean |
| **Validación de Entrada** | Bean Validation (`@Valid`, `@NotNull`, etc.) |
| **Transacciones** | `@Transactional` en casos de uso |
| **Eventos de Dominio** | Publicados vía ApplicationEvent o mensajería |
| **Naming Convenciones** | PascalCase (tipos), camelCase (variables), snake_case (BD) |
| **Testing Ready** | Interfaces en dominio, mocks inyectables |

---

## Orden de Ejecución

1. **Domain**: Entidades, Value Objects, Puertos (Interfaces)
2. **Domain**: Casos de Uso (Servicios con lógica de negocio)
3. **Application**: DTOs, Mappers (MapStruct)
4. **Infrastructure**: Repositorios JPA, Adaptadores de salida
5. **Infrastructure**: Controladores REST (Adaptadores de entrada)
6. **Infrastructure**: Configuración Spring (`@Configuration` beans)

---

## Restricciones

- Todos los archivos `.java` en `src/main/java/` (siguiendo estructura hexagonal)
- Usar constructor injection (no `@Autowired` en campos)
- Registrar servicios via `@Service`, `@Repository`, `@RestController`
- No generar tests (responsabilidad de [[agents/test-engineer-backend.agent.md]])
- No modificar frontend
- BaseDatos.sql es la fuente de verdad del esquema
- Usar `./mvnw` para todos los comandos de build
