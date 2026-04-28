---
applyTo: "backend/**/*.java"
---

> **Scope**: Se aplica a proyectos con capa backend en Spring Boot 3.x / Java 17+. Si el proyecto usa un lenguaje o estructura diferente, adaptar la sección de convenciones y wiring al stack real definido en esta misma instrucción.

# Instrucciones para Backend (Spring Boot 3.x / Java 17+)

## Arquitectura Obligatoria — Hexagonal (Ports and Adapters)

TODO proyecto backend DEBE seguir la arquitectura hexagonal con las siguientes capas:

```
src/main/java/com/example/<service>/
├── domain/           ← Entidades de dominio, Value Objects, Reglas de negocio
│   ├── model/       ← Entidades JPA puras
│   ├── ports/       ← Interfaces (Input/Ports y Output/Ports)
│   └── service/     ← Lógica de negocio (Casos de Uso)
├── application/      ← DTOs, Mappers (MapStruct), Validaciones
│   ├── dto/         ← Request/Response DTOs
│   └── mapper/      ← MapStruct mappers
└── infrastructure/  ← Adaptadores de entrada y salida
    ├── input/       ← Controladores REST (Spring MVC)
    ├── output/      ← Repositorios (Spring Data JPA), Clientes externos
    └── config/      ← Configuración Spring (Beans, Security, etc.)
```

### Responsabilidades por Capa

- **Domain (.java)**
  - Entidades puras (JPA entities con lógica de dominio)
  - Value Objects inmutables
  - Interfaces de puerto (Input Ports y Output Ports)
  - Casos de uso (lógica de negocio pura)
  - Business Exceptions
  - **PROHIBIDO**: Spring annotations en entidades de dominio, dependencias de infraestructura

- **Application (.java)**
  - DTOs normalizados (Create*, Update*, Response*)
  - MapStruct mappers (@Mapper)
  - Validaciones de entrada (@Valid, Bean Validation)
  - **PROHIBIDO**: Controllers, acceso directo a BD, lógica de infraestructura

- **Infrastructure (.java)**
  - Controladores REST (@RestController)
  - Repositorios Spring Data JPA (JpaRepository)
  - Implementación de Output Ports
  - Configuración de Beans, Security, Mensajería
  - **PROHIBIDO**: Lógica de negocio en controllers

### Flujo de Datos (Inmutable)
```
HTTP → Controller (Input Adapter) → Use Case (Domain Service) → Output Port 
                                              ↓
                                    Repository (JPA) → Entity → BD
                                              ↓
                                    Response DTO → HTTP 200/201
```

## Entidad JPA — Ejemplo Obligatorio

Toda entidad DEBE tener:

```java
package com.example.banking.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
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
    
    // Constructor privado para JPA
    protected Account() {}
    
    // Constructor de dominio
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Integer getVersion() { return version; }
}
```

**Regla Bloqueante**: Sin `@Version`, `createdAt` + `updatedAt` con `@PrePersist`/`@PreUpdate` = RECHAZADO en Code Review.

## Inyección de Dependencias — Spring Boot

```java
// ✅ CORRECTO — Constructor Injection (Spring)
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    
    private final AccountUseCase accountUseCase;
    
    public AccountController(AccountUseCase accountUseCase) {
        this.accountUseCase = accountUseCase;
    }
    
    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(@Valid @RequestBody CreateAccountDto dto) {
        AccountResponseDto response = accountUseCase.createAccount(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDto> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountUseCase.getAccountById(id));
    }
}

// En la capa de dominio - Use Case
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
        Account account = Account.create(dto.getAccountNumber(), dto.getClientId(), dto.getAccountType());
        Account saved = accountRepository.save(account);
        return mapper.toResponseDto(saved);
    }
    
    @Override
    public AccountResponseDto getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + id));
        return mapper.toResponseDto(account);
    }
}
```

❌ PROHIBIDO: Instanciar servicios/repos dentro del controller.  
❌ PROHIBIDO: Lógica de negocio en controllers.  
❌ PROHIBIDO: `@Autowired` field injection — usar constructor injection.

## Database-First Mapping — BaseDatos.sql como Fuente de Verdad

**REGLA ORO**: El esquema de base de datos se define EXCLUSIVAMENTE en `BaseDatos.sql`. Los cambios en la BD requieren modificar el script SQL centralizado.

✅ **PROCESO OBLIGATORIO**:

1. **Modificar BaseDatos.sql** (fuente de verdad)
   ```sql
   -- BaseDatos.sql
   CREATE SCHEMA IF NOT EXISTS customer_service;
   CREATE SCHEMA IF NOT EXISTS banking_service;
   
   -- Customer Service Schema
   CREATE TABLE customer_service.clients (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       person_id BIGINT NOT NULL,
       client_id VARCHAR(50) UNIQUE NOT NULL,
       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       version INT DEFAULT 1
   );
   
   -- Banking Service Schema
   CREATE TABLE banking_service.accounts (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       account_number VARCHAR(50) UNIQUE NOT NULL,
       client_id BIGINT NOT NULL,  -- Referencia local (no FK, comunicación asíncrona)
       balance DECIMAL(18,2) DEFAULT 0.00,
       account_type VARCHAR(20) NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
       version INT DEFAULT 1
   );
   
   CREATE TABLE banking_service.movements (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       account_id BIGINT NOT NULL,
       movement_type VARCHAR(20) NOT NULL,
       amount DECIMAL(18,2) NOT NULL,
       balance_after DECIMAL(18,2) NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   ```

2. **Mapear Entidades JPA a las tablas existentes**
   ```java
   @Entity
   @Table(name = "accounts", schema = "banking_service")
   public class Account {
       // ... campos mapeados a las columnas del script SQL
   }
   ```

3. **Configurar application.yml para el esquema correcto**
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/
       username: root
       password: password
     jpa:
       properties:
         hibernate:
           default_schema: banking_service
       database-platform: org.hibernate.dialect.MySQLDialect
       hibernate:
         ddl-auto: validate  # NO usar update/create, el esquema viene de BaseDatos.sql
   ```

❌ **PROHIBIDO ABSOLUTAMENTE**:
- Usar `spring.jpa.hibernate.ddl-auto=update` o `create`
- Modificar BD directamente sin actualizar BaseDatos.sql
- Generar migraciones automáticas (Flyway/Liquibase opcional, pero BaseDatos.sql es la fuente)

## MapStruct para Mapeo DTO ↔ Entity

```java
// Application Layer - Mapper
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

// DTOs
public record CreateAccountDto(
    @NotBlank String accountNumber,
    @NotNull Long clientId,
    @NotNull AccountType accountType
) {}

public record AccountResponseDto(
    Long id,
    String accountNumber,
    Long clientId,
    Double balance,
    AccountType accountType,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

## Comunicación Asíncrona entre Microservicios

Para el Banking Service reaccionar a eventos del Customer Service:

```java
// Domain - Event
public record ClienteCreadoEvent(
    Long clientId,
    String clientIdString,
    LocalDateTime occurredAt
) {}

// Infrastructure - Event Listener (Spring Events, Kafka, RabbitMQ, etc.)
@Component
public class ClienteEventListener {
    
    private final ClienteLocalRepository clienteLocalRepo;
    
    public ClienteEventListener(ClienteLocalRepository clienteLocalRepo) {
        this.clienteLocalRepo = clienteLocalRepo;
    }
    
    @EventListener
    public void handleClienteCreado(ClienteCreadoEvent event) {
        // Mantener referencia local del cliente
        ClienteLocal cliente = new ClienteLocal();
        cliente.setClientId(event.clientId());
        clienteLocalRepo.save(cliente);
    }
}
```

## Convenciones de Código — Java 17+ (Obligatorio)

- **Records**: para DTOs inmutables `record CreateAccountDto(...) {}`
- **Optional**: para retornos que pueden ser nulos `Optional<Account> findById()`
- **Streams**: para procesamiento de colecciones
- **Naming**: PascalCase (clases, métodos, propiedades), camelCase (variables locales), snake_case (columnas BD: created_at, updated_at)
- **Controllers**: retornan `ResponseEntity<T>` (200/201/400/404/409)
- **Anotaciones JPA**: en entidades para mapeo a BaseDatos.sql
- **Validaciones**: Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`)

## Agregar Nuevos Endpoints

Para agregar un nuevo endpoint:
1. Crear Entity en `domain/model/` (mapeada a BaseDatos.sql)
2. Crear Puerto de salida `domain/ports/out/RepositoryPort.java`
3. Crear DTOs en `application/dto/`
4. Crear Use Case en `domain/service/` (implementa puerto de entrada)
5. Crear Controller en `infrastructure/input/`
6. Implementar repositorio en `infrastructure/output/`
7. Registrar beans en configuración Spring

> Toda implementación DEBE cumplir con la arquitectura hexagonal. Ver `[[specs/<feature>.spec.md]]` para requerimientos específicos de cada microservicio.

## Estándar HTTP API — RFC 9457 + Envelope JSON

### Éxito (200/201)
```json
{
  "data": {
    "id": 123,
    "accountNumber": "ACC-456",
    "balance": 5000.00
  },
  "timestamp": "2025-01-15T10:30:00Z"
}
```

### Error (400/404/409)
```json
{
  "type": "https://api.example.com/errors/insufficient-funds",
  "title": "Conflict — Insufficient funds",
  "status": 409,
  "detail": "Account balance is 1000.00, cannot withdraw 5000.00",
  "instance": "/api/accounts/123/withdraw"
}
```

**Implementación**:
```java
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    
    @PostMapping("/{id}/deposit")
    public ResponseEntity<?> deposit(@PathVariable Long id, @Valid @RequestBody DepositDto dto) {
        try {
            AccountResponseDto result = accountUseCase.deposit(id, dto.getAmount());
            return ResponseEntity.ok(Map.of(
                "data", result,
                "timestamp", LocalDateTime.now()
            ));
        } catch (AccountNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "type", "https://api.example.com/errors/not-found",
                    "title", "Account not found",
                    "status", 404,
                    "detail", ex.getMessage()
                ));
        }
    }
}
```

## Prohibiciones Arquitectónicas — Innegociables

❌ Exponer entidades JPA directamente en API (siempre usar DTOs).  
❌ Lógica de negocio en controllers.  
❌ Métodos síncronos que acceden a BD (siempre `@Async` o `CompletableFuture` si es necesario).  
❌ Mezclar anotaciones Spring en entidades de dominio puro.  
❌ Operaciones de datos fuera del patrón Repository.  
❌ Usar `ddl-auto=update` — el esquema viene de BaseDatos.sql.  
❌ Timestamps que NO sean snake_case en BD (created_at, updated_at, deleted_at).  
❌ Entidad sin `@Version` para control de concurrencia optimista.  
❌ Field injection (`@Autowired`) — USAR constructor injection.  

**Consecuencia**: PR rechazado automáticamente en Code Review.

---

## Validación en Code Review

**Un PR se rechaza si:**
- ❌ No sigue la arquitectura hexagonal (capas domain/application/infrastructure)
- ❌ Domain tiene dependencias de Spring o JPA
- ❌ Entidad sin `@Version` o sin `createdAt`/`updatedAt` con `@PrePersist`/`@PreUpdate`
- ❌ Controller con lógica de negocio
- ❌ Respuestas que no siguen RFC 9457 + envelope
- ❌ Tests con cobertura < 80%
- ❌ Métodos que acceden a BD sin ser asíncronos (si aplica)
- ❌ Usa `ddl-auto=update` en lugar de BaseDatos.sql

> Para código limpio, SOLID, seguridad y observabilidad, ver `[[docs/lineamientos/dev-guidelines.md]]`.
