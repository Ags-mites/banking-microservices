---
name: implement-backend
description: Implementa un feature completo en el backend con Spring Boot 4 / Java 21 y Arquitectura Hexagonal PURA. Requiere spec con status APPROVED en .github/specs/.
argument-hint: "<nombre-feature>"
---

# Implement Backend — Spring Boot 4 / Java 21 Hexagonal Architecture

## Prerequisitos
1. **Leer spec**: [[specs/<feature>.spec.md]] — sección 2 (entidades, casos de uso, puertos)
2. **Stack & Arquitectura**: [[instructions/backend.instructions.md]]
3. **Patrones de código**: [[skills/implement-backend/patterns.java]]
4. **Directrices generales**: [[docs/lineamientos/dev-guidelines.md]]
5. **BaseDatos.sql**: Ver esquema definido en el script SQL centralizado

---

## Arquitectura Hexagonal PURA

| Capa | Paquete | Anotaciones | 
|------|--------|-------------|
| **Domain** | `domain/model/` | ❌ Ninguna (100% POJO) |
| **Domain** | `domain/ports/` | ❌ Ninguna (interfaces) |
| **Application** | `application/usecase/` | ✅ `@Service` (wiring) |
| **Application** | `application/dto/` | ❌ Ninguna (Records) |
| **Infrastructure** | `infrastructure/output/` | ✅ `@Entity`, `@Repository` |
| **Infrastructure** | `infrastructure/input/` | ✅ `@RestController` |

> 📌 **REGLA**: Domain = Java PURO. Las anotaciones de framework van **exclusivamente** en Infrastructure y Application (para wiring).

> Ver patrones completos en [[skills/implement-backend/patterns.java]]

---

## Algoritmo de Implementación

### Paso 1: Análisis de Dependencias
- Identificar entidades raíz del spec
- Listar Value Objects y sus propiedades
- Mapear dependencias entre agregados
- Identificar comunicación asíncrona
- **Salida**: lista de archivos `.java` a crear por capa

### Paso 2: Domain Layer — Java PURO
> Ver patrón en [[skills/implement-backend/patterns.java]] sección "Domain Layer"

```
src/main/java/com/example/<service>/domain/
├── model/
│   ├── Account.java           # Entity: lógica de negocio (POJO)
│   ├── Money.java           # Value Object inmutable
│   └── AccountType.java     # Enum
├── ports/in/
│   └── AccountUseCase.java  # Input Port (interfaz)
├── ports/out/
│   └── AccountRepository.java # Output Port (interfaz)
└── exception/
    └── DomainException.java
```

**Regla**: NO usar ninguna anotación (@Entity, @Table, @Version, etc.)

### Paso 3: Application Layer — DTOs + UseCases
> Ver patrón en [[skills/implement-backend/patterns.java]] sección "Application Layer"

```
src/main/java/com/example/<service>/application/
├── dto/
│   ├── CreateAccountRequest.java
│   └── AccountResponse.java
└── usecase/
    └── AccountService.java   # @Service (aquí sí)
```

```java
// DTOs como Records
public record CreateAccountRequest(
    String accountNumber,
    Long clientId,
    String accountType
) {}

// UseCase con @Service (aquí sí corresponde)
@Service
public class AccountService implements AccountUseCase {
    private final AccountRepository repository;
    
    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }
}
```

### Paso 4: Infrastructure Layer — JPA + Controllers
> Ver patrón en [[skills/implement-backend/patterns.java]] sección "Infrastructure Layer"

```
src/main/java/com/example/<service>/infrastructure/
├── output/
│   ├── AccountEntity.java      # @Entity (@Table)
│   ├── AccountRepositoryAdapter.java  # @Repository
│   └── AccountJpaRepository.java    # Spring Data
├── input/
│   └── AccountController.java     # @RestController
└── config/
    └── ApplicationConfig.java   # @Configuration
```

### Paso 5: Wiring (conectar capas)
```java
// infrastructure/config/ApplicationConfig.java
@Configuration
public class ApplicationConfig {
    
    @Bean
    public AccountRepository accountRepository(AccountJpaRepository jpaRepository) {
        return new AccountRepositoryAdapter(jpaRepository);
    }
    
    @Bean
    public AccountUseCase accountUseCase(AccountRepository repository) {
        return new AccountService(repository);
    }
}
```

---

## Estructura de Carpetas Final

```
src/main/java/com/example/<service>/
├── domain/
│   ├── model/           # Entity + Value Objects + Enums (POJO puro)
│   ├── ports/in/       # Input Ports (Use Cases interfaces)
│   ├── ports/out/      # Output Ports (Repository interfaces)
│   └── exception/     # Excepciones del dominio
├── application/
│   ├── dto/          # Records DTOs
│   └── usecase/      # Implementación de UseCases (@Service aquí)
└── infrastructure/
    ├── input/        # REST Controllers
    ├── output/      # JPA Entities + Repositories + Adapters
    └── config/      # Spring config (wiring)
```

---

## Comandos Maven

| Acción | Comando |
|--------|---------|
| Construir | `./mvnw clean install` |
| Ejecutar | `./mvnw spring-boot:run` |
| Tests | `./mvnw test` |
| Verificar | `./mvnw clean verify` |
| Empaquetar | `./mvnw package -DskipTests` |

---

## Responsabilidades por Capa

| Capa | Componente | Responsabilidad |
|------|-----------|----------------|
| **Domain** | Entity, Value Object | Lógica de negocio pura |
| **Domain** | Input/Output Port | Interfaces (contratos) |
| **Application** | UseCase (@Service) | Orquestación, usa ports |
| **Application** | DTO | Contratos HTTP |
| **Infrastructure** | JPA Entity | Mapeo a BD |
| **Infrastructure** | Repository | Persistencia |
| **Infrastructure** | Controller | Routing HTTP |

---

## Estándares de Código

| Estándar | ✅ Correcto | ❌ Evitar |
|----------|-----------|---------|
| **Domain** | 0% anotaciones | @Entity, @Table, @Service |
| **Domain** | Solo java.* imports | org.springframework, jakarta.persistence |
| **Application** | @Service (wiring) | Lógica de negocio aquí |
| **Value Object** | inmutable (final + no setters) | setters públicos |
| **Records** | `record Dto(...)` | `class Dto { set; get; }` |
| **Inyección** | Constructor | @Autowired campo |

---

## Validación de Completitud

| Criterio | ✅ Validar |
|----------|----------|
| **Domain Puro** | Sin anotaciones de framework |
| **BaseDatos.sql** | Entidad mapea tabla definida |
| **Timestamps** | @PrePersist + @PreUpdate en Entity |
| **Concurrencia** | @Version en Entity |
| **Desacoplamiento** | Repository implementa puerto del domain |
| **Inyección** | Constructor injection |
| **Wiring** | ApplicationConfig conecta capas |

---

## Restricciones

- Domain = Java PURO (sin import de framework)
- @Service va en Application, no en Domain
- No generar tests (responsabilidad de test-engineer)
- BaseDatos.sql = fuente de verdad del esquema
- Usar `./mvnw` para build