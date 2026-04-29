# Lineamientos de Desarrollo — Estándar de Ingeniería

> Estos lineamientos son obligatorios para todas las implementaciones del proyecto Banking Microservices.

---

## 1. Diseño y Arquitectura (LIN-DEV-001)

**Objetivo**: Garantizar bajo acoplamiento y alta cohesión mediante Arquitectura Hexagonal.

### Reglas

- **Independencia de Frameworks**: El núcleo del negocio (Domain) no debe depender de librerías externas o de persistencia.
- **Inversión de Dependencias (DIP)**: Se deben utilizar puertos (interfaces) para la comunicación con el exterior (Base de Datos, APIs, Mensajería).
- **Modelo de Herencia**: La jerarquía de clases debe ser clara y justificada por el dominio.

### Arquitectura Hexagonal

```
Domain (Java Puro) → Application (@Service) → Infrastructure (Frameworks)
```

| Capa | Responsabilidad | Dependencias |
|------|----------------|-------------|
| **Domain** | Entidades, reglas de negocio, lógica pura | Ninguna (java.* only) |
| **Application** | Casos de uso, orquestación | Depende de Domain |
| **Infrastructure** | Adaptadores (HTTP, BD, messaging) | Framework |

---

## 2. Codificación Limpia / Clean Code (LIN-DEV-002)

**Objetivo**: Código auto-explicativo y mantenible.

### Reglas

- **Nomenclatura**: Nombres de clases, métodos y variables deben ser descriptivos y estar en el lenguaje del negocio.
- **Responsabilidad Única (SRP)**: Cada componente debe tener una única razón para cambiar.
- **Legibilidad**: Se prohíbe el uso de "comentarios obvios". El código debe explicar el "qué" y el "por qué" por sí mismo.

### Estándares

| Estándar | ✅ Correcto | ❌ Evitar |
|----------|-----------|----------|
| **Método** | `calculateInterestEarned()` | `calc()` |
| **Variable** | `totalBalanceDue` | `x`, `temp` |
| **Clase** | `InsufficientFundsException` | `ErrorHandler` |
| **Comentarios** |// Algoritmo de Fibonacci | // Loop para iterar |

---

## 3. Gestión de Errores y Resiliencia (LIN-DEV-003)

**Objetivo**: Control total sobre el flujo de fallos y excepciones.

### Reglas

- **Excepciones Controladas**: Todo flujo de error debe ser capturado y transformado en un mensaje de negocio coherente.
- **Validación de Reglas de Negocio**: El sistema debe impedir estados inconsistentes (como saldos negativos no autorizados) mediante excepciones específicas.
- **Códigos de Estado**: Uso estricto de verbos y códigos HTTP estándar.

### Códigos HTTP

| Código | Uso | Ejemplo |
|--------|-----|---------|
| `201 Created` | Recurso creado exitosamente | `POST /accounts` |
| `200 OK` | Operación exitosa | `GET /accounts/{id}` |
| `400 Bad Request` | Datos inválidos | `POST /accounts` con datos mal |
| `404 Not Found` | Recurso no existe | `GET /accounts/999` |
| `409 Conflict` | Regla de negocio violada | `withdraw` > saldo |

### Errores (RFC 9457)

```json
{
  "type": "https://api.bank.com/errors/insufficient-funds",
  "title": "Conflict — Insufficient funds",
  "status": 409,
  "detail": "Account balance is 1000.00, cannot withdraw 5000.00"
}
```

---

## 4. Persistencia y Datos (LIN-DEV-004)

**Objetivo**: Acceso a datos consistente y reproducible.

### Reglas

- **Patrón Repository**: Centralización del acceso a datos para permitir cambios en la tecnología de persistencia sin afectar el dominio.
- **Evolución del Esquema**: Toda base de datos debe poder recrearse desde cero mediante scripts de definición de datos (DDL).
- **Trazabilidad**: Se debe garantizar el registro histórico de movimientos y cambios de estado.

### BaseDatos.sql como Fuente de Verdad

```sql
-- BaseDatos.sql
CREATE SCHEMA IF NOT EXISTS banking_service;

CREATE TABLE banking_service.accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    client_id BIGINT NOT NULL,
    balance DECIMAL(18,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 1
);
```

### Prohibiciones

- ❌ `ddl-auto=update` o `create`
- ❌ Modificar BD sin actualizar BaseDatos.sql
- ❌ Entidades sin `@Version` para concurrencia

---

## 5. Calidad y Testing (LIN-DEV-005)

**Objetivo**: Validar la funcionalidad de forma automatizada.

### Reglas

- **Pruebas de Dominio**: Implementación obligatoria de pruebas unitarias para las entidades y lógica de negocio crítica.
- **Pruebas de Contrato**: Validación de endpoints mediante pruebas de integración que aseguren el flujo completo.

### Cobertura

| Tipo | Target | Herramienta |
|------|-------|------------|
| **Unitarias** | ≥ 80% | JUnit 5 + Mockito |
| **Integración** | Endpoints key | MockMvc |
| **Calidad Gate** | Bloqueante en CI | JaCoCo |

### Estructura de Tests

```
src/test/java/com/bank/<service>/
├── application/usecase/    ← Service Tests
├── infrastructure/input/   ← Controller Tests
└── infrastructure/output/  ← Adapter Tests
```

---

## 6. Entrega y Portabilidad (LIN-DEV-006)

**Objetivo**: Asegurar que el software sea ejecutable en cualquier entorno.

### Reglas

- **Contenerización**: Uso de Docker para empaquetar la solución, garantizando que "funcione en mi máquina y en la tuya".
- **Versionamiento Semántico**: Uso de Git con mensajes descriptivos para trazar la evolución de la solución.
- **Documentación de API**: Provisión de artefactos de validación (Postman/OpenAPI) para pruebas externas.

### Commits (Conventional)

```
feat: add account withdrawal feature
fix: correct balance calculation
chore: update BaseDatos.sql schema
docs: update API documentation
```

### Docker

```dockerfile
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests
EXPOSE 8081
CMD ["java", "-jar", "target/bankingservice.jar"]
```

---

## Checklist de Cumplimiento

| Regla | Descripción | Validación |
|------|-----------|-------------|
| LIN-DEV-001 | Arquitectura Hexagonal | Domain sin imports de framework |
| LIN-DEV-001 | Puertos definidos | Interfaces en domain/ports/ |
| LIN-DEV-002 | Clean Code | Sin "comentarios obvios" |
| LIN-DEV-002 | SRP | ≤ 50 líneas por método |
| LIN-DEV-003 | Errores controlados | Códigos HTTP específicos |
| LIN-DEV-004 | BaseDatos.sql | Fuente de verdad |
| LIN-DEV-005 | Cobertura ≥ 80% | JaCoCo |
| LIN-DEV-006 | Docker | Imagen funcional |

> Toda desviación debe ser justificada y aprobada formalmente.