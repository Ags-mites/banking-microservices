---
applyTo: "backend/**/*.java"
---

> **Scope**: Se aplica a proyectos con capa backend en Spring Boot 4 / Java 21. Si el proyecto usa un lenguaje o estructura diferente, adaptar la sección de convenciones y wiring al stack real definido en esta misma instrucción.

# Instrucciones para Backend (Spring Boot 4 / Java 21)

## Reglas de Oro

> 🛡️ **REGLA ORO**: 
> - **Domain** = Java PURO (sin anotaciones de framework)
> - **Application** = `@Service` para wiring
> - **Infrastructure** = JPA (`@Entity`), Spring (`@RestController`, `@Repository`, `@Configuration`)

> 🛡️ **BaseDatos.sql COMO FUENTE DE VERDAD**: El esquema de BD se define EXCLUSIVAMENAME en `BaseDatos.sql`. No usar `ddl-auto=update/create`.

## Arquitectura Hexagonal

```
src/main/java/com/example/<service>/
├── domain/           ← Java PURO - SIN anotaciones
│   ├── model/       ← Entities + Value Objects
│   ├── ports/       ← Interfaces (Input/Output Ports)
│   └── exception/   ← Excepciones del dominio
│
├── application/      ← @Service (wiring) + DTOs
│   ├── dto/       ← Request/Response (Records)
│   └── usecase/    ← Casos de uso (@Service aquí)
│
└── infrastructure/ ← Anotaciones de framework
    ├── input/      ← REST Controllers (@RestController)
    ├── output/     ← JPA (@Entity, @Repository)
    ├── messaging/  ← RabbitMQ
    └── config/     ← @Configuration
```

> 📌 **@Service** va en `application/usecase/`, NO en `domain/`

**Pattern de implementación**: Ver [[skills/implement-backend/SKILL.md]] para el algoritmo completo paso a paso.

---

## BaseDatos.sql como Fuente de Verdad

**REGLA ORO**: El esquema de base de datos se define EXCLUSIVAMENTE en `BaseDatos.sql`. Los cambios en la BD requieren modificar el script SQL centralizado.

**PROCESO OBLIGATORIO**:

1. **Modificar BaseDatos.sql** (fuente de verdad)
2. **Mapear Entidades JPA** a las tablas existentes (usar `@Table(schema = "...")`)
3. **Configurar application.yml** con el esquema correcto

**PROHIBIDO ABSOLUTAMENTE**:
- Usar `spring.jpa.hibernate.ddl-auto=update` o `create`
- Modificar BD directamente sin actualizar BaseDatos.sql
- Generar migraciones automáticas (Flyway/Liquibase opcional, pero BaseDatos.sql es la fuente)

---

## RabbitMQ — Configuración de Mensajería Asíncrona

### Habilitar RabbitMQ

```yaml
# application.yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```

### Reglas Técnicas:

1. **Records para DTOs y Mensajes**
2. **Sealed Interfaces para Errores de Dominio** (si aplica)
3. **Virtual Threads (Java 21)**:
   ```yaml
   spring:
     threads:
       virtual:
         enabled: true
   ```
4. **Serialización JSON** para RabbitMQ (Jackson con JavaTimeModule)
5. **Productor y Consumidor** usando `@Component` + `@RabbitListener`
6. **Regla de Solo Lectura**: Microservicio de Cuentas NO puede modificar datos del Cliente. Solo leer referencia sincronizada.

> Ver implementación completa en [[skills/implement-backend/SKILL.md]]

---

## Estándar HTTP API — RFC 9457 + Envelope JSON

### Éxito (200/201)
```json
{
  "data": { "id": 123, "accountNumber": "ACC-456", "balance": 5000.00 },
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

---

## Prohibiciones Arquitectónicas — Innegociables

- ❌ Exponer entidades JPA directamente en API (siempre usar DTOs)
- ❌ Lógica de negocio en controllers
- ❌ Métodos síncronos que acceden a BD (usar `@Async` si es necesario)
- ❌ Mezclar anotaciones Spring en entidades de dominio puro
- ❌ Operaciones de datos fuera del patrón Repository
- ❌ Usar `ddl-auto=update` — el esquema viene de BaseDatos.sql
- ❌ Timestamps que NO sean snake_case en BD (created_at, updated_at)
- ❌ Entidad sin `@Version` para control de concurrencia optimista
- ❌ Field injection (`@Autowired`) — USAR constructor injection

**Consecuencia**: PR rechazado automáticamente en Code Review.

---

## Validación en Code Review

| Criterio | Validación |
|----------|-----------|
| Arquitectura hexagonal | ✅ Capas domain/application/infrastructure separadas |
| Domain puro | ✅ Sin dependencias Spring/JPA en domain/ |
| Entidad JPA | ✅ @Version + createdAt/updatedAt con @PrePersist/@PreUpdate |
| Controller | ✅ Solo routing, sin lógica de negocio |
| HTTP Response | ✅ Sigue RFC 9457 + envelope |
| Tests | ✅ Cobertura >= 80% |
| BaseDatos.sql | ✅ Fuente de verdad, no ddl-auto=update |

> Para código limpio, SOLID, seguridad y observabilidad, ver `[[docs/lineamientos/dev-guidelines.md]]`