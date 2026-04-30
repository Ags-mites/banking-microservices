---
id: SPEC-002
status: APPROVED
feature: account-management
created: 2026-04-29
updated: 2026-04-29
author: spec-generator
version: "1.0"
related-specs: []
---

# Spec: Administración de Cuentas Bancarias

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción
Funcionalidad de administración de cuentas bancarias para gestionar productos financieros de los clientes. Permite crear, actualizar, consultar y eliminar cuentas con validaciones de negocio e integración con el microservicio de clientes.

### Requerimiento de Negocio
Como administrador del sistema, quiero gestionar las cuentas de los clientes, para habilitar sus productos financieros.

### Historias de Usuario

#### HU-001: Crear Cuenta Bancaria

```
Como:        Administrador del Sistema
Quiero:      Crear una nueva cuenta bancaria con todos sus datos
Para:        Habilitar un producto financiero para un cliente

Prioridad:   Alta
Estimación:  M
Dependencias: Cliente debe existir en el sistema (customerservice)
Capa:        Backend
```

#### Criterios de Aceptación — HU-001

**CRITERIO-1.1: Crear cuenta con todos los campos válidos (Happy Path)**
```gherkin
Dado que:  Un cliente con `clienteId = 1` existe en el sistema
Cuando:    Envío POST /api/cuentas con:
           {
             "clienteId": 1,
             "numeroCuenta": "123456789",
             "tipoCuenta": "Ahorros",
             "saldoInicial": 1000.00,
             "estado": true
           }
Entonces:  El sistema retorna HTTP 201
           La respuesta contiene:
           - "id": generado automáticamente (ej. 1)
           - "numeroCuenta": "123456789"
           - "tipoCuenta": "Ahorros"
           - "saldoInicial": 1000.00
           - "estado": true
           - "clienteId": 1
           - "timestamp": ISO 8601 en UTC
```

**CRITERIO-1.2: Validar número de cuenta único**
```gherkin
Dado que:  Una cuenta con número "123456789" ya existe
Cuando:    Intento crear otra cuenta con el mismo número
Entonces:  El sistema retorna HTTP 409 Conflict
           La respuesta contiene (RFC 9457):
           {
             "type": "https://api.example.com/errors/account-conflict",
             "title": "Conflict",
             "status": 409,
             "detail": "El número de cuenta ya existe.",
             "instance": "/api/cuentas"
           }
           Manejado por GlobalExceptionHandler cuando AccountConflictException es lanzada
```

**CRITERIO-1.3: Validar tipo de cuenta**
```gherkin
Dado que:  Los tipos válidos son "Ahorros" y "Corriente"
Cuando:    Intento crear una cuenta con tipo "Invalido"
Entonces:  El sistema retorna HTTP 400 Bad Request
           La respuesta contiene (RFC 9457):
           {
             "type": "https://api.example.com/errors/account-validation",
             "title": "Bad Request",
             "status": 400,
             "detail": "Tipo de cuenta inválido. Solo 'Ahorros' o 'Corriente' están permitidos.",
             "instance": "/api/cuentas"
           }
           Lanzado en constructor Cuenta.abrir() como AccountValidationException
```

**CRITERIO-1.4: Validar saldo no negativo (Edge Case)**
```gherkin
Dado que:  Se requiere un saldo válido
Cuando:    Intento crear una cuenta con saldoInicial = -500.00
Entonces:  El sistema retorna HTTP 400 Bad Request
           La respuesta contiene (RFC 9457):
           {
             "type": "https://api.example.com/errors/account-validation",
             "title": "Bad Request",
             "status": 400,
             "detail": "El saldo no puede ser negativo.",
             "instance": "/api/cuentas"
           }
           Lanzado en constructor Cuenta.abrir() como AccountValidationException
```

**CRITERIO-1.5: Validar existencia de cliente al crear cuenta**
```gherkin
Dado que:  No existe un cliente con `clienteId = 9999` en customerservice
Cuando:    Envío POST /api/cuentas con `clienteId = 9999`
Entonces:  El sistema retorna HTTP 400 Bad Request
           La respuesta contiene (RFC 9457):
           {
             "type": "https://api.example.com/errors/account-validation",
             "title": "Bad Request",
             "status": 400,
             "detail": "El cliente con ID 9999 no existe.",
             "instance": "/api/cuentas"
           }
           La validación se ejecuta vía puerto ClientVerifier antes de persistir
```

---

#### HU-002: Consultar Todas las Cuentas

```
Como:        Administrador del Sistema
Quiero:      Obtener una lista de todas las cuentas
Para:        Tener visibilidad sobre todos los productos financieros activos

Prioridad:   Media
Estimación:  XS
Dependencias: Ninguna
Capa:        Backend
```

#### Criterios de Aceptación — HU-002

**CRITERIO-2.1: Listar todas las cuentas (Happy Path)**
```gherkin
Dado que:  Existen cuentas en el sistema
Cuando:    Envío GET /api/cuentas
Entonces:  El sistema retorna HTTP 200 OK
           La respuesta contiene un array con todas las cuentas
           Cada cuenta incluye: id, numeroCuenta, tipoCuenta, saldoInicial, estado, clienteId
```

**CRITERIO-2.2: Manejar lista vacía**
```gherkin
Dado que:  No existen cuentas en el sistema
Cuando:    Envío GET /api/cuentas
Entonces:  El sistema retorna HTTP 200 OK
           La respuesta contiene un array vacío: []
```

---

#### HU-003: Consultar Cuenta por ID

```
Como:        Administrador del Sistema
Quiero:      Obtener los detalles de una cuenta específica
Para:        Ver la información completa de un producto financiero

Prioridad:   Media
Estimación:  XS
Dependencias: Ninguna
Capa:        Backend
```

#### Criterios de Aceptación — HU-003

**CRITERIO-3.1: Obtener cuenta existente (Happy Path)**
```gherkin
Dado que:  Una cuenta con `id = 1` existe
Cuando:    Envío GET /api/cuentas/1
Entonces:  El sistema retorna HTTP 200 OK
           La respuesta contiene todos los campos de la cuenta
```

**CRITERIO-3.2: Manejar cuenta no encontrada**
```gherkin
Dado que:  No existe una cuenta con `id = 999`
Cuando:    Envío GET /api/cuentas/999
Entonces:  El sistema retorna HTTP 404 Not Found
           La respuesta contiene (RFC 9457):
           {
             "type": "https://api.example.com/errors/account-not-found",
             "title": "Not Found",
             "status": 404,
             "detail": "Cuenta no encontrada con id: 999",
             "instance": "/api/cuentas/999"
           }
```

---

#### HU-004: Actualizar Cuenta Existente

```
Como:        Administrador del Sistema
Quiero:      Actualizar los datos de una cuenta existente
Para:        Mantener la información del producto financiero sincronizada

Prioridad:   Media
Estimación:  S
Dependencias: Ninguna
Capa:        Backend
```

#### Criterios de Aceptación — HU-004

**CRITERIO-4.1: Actualizar cuenta con PUT (Happy Path)**
```gherkin
Dado que:  Una cuenta con `id = 1` existe
Cuando:    Envío PUT /api/cuentas/1 con:
           {
             "tipoCuenta": "Corriente",
             "estado": true
           }
Entonces:  El sistema retorna HTTP 200 OK
           La cuenta se actualiza correctamente
           El campo numeroCuenta permanece sin cambios
           updated_at se actualiza automáticamente
```

**CRITERIO-4.2: Actualizar parcialmente con PATCH**
```gherkin
Dado que:  Una cuenta con `id = 1` existe
Cuando:    Envío PATCH /api/cuentas/1 con:
           {
             "estado": false
           }
Entonces:  El sistema retorna HTTP 200 OK
           Solo el campo `estado` se actualiza
           Los demás campos permanecen sin cambios
```

**CRITERIO-4.3: El número de cuenta no es editable**
```gherkin
Dado que:  Una cuenta existe con numeroCuenta = "123456789"
Cuando:    Intento actualizar el número de cuenta a "987654321"
Entonces:  El sistema ignora el cambio
           El numeroCuenta permanece como "123456789"
           No se retorna error
```

**CRITERIO-4.4: Validar tipo de cuenta en actualización**
```gherkin
Dado que:  Una cuenta existe
Cuando:    Intento actualizar con tipoCuenta = "Invalido"
Entonces:  El sistema retorna HTTP 400 Bad Request
           La respuesta contiene (RFC 9457):
           {
             "type": "https://api.example.com/errors/account-validation",
             "title": "Bad Request",
             "status": 400,
             "detail": "Tipo de cuenta inválido. Solo 'Ahorros' o 'Corriente' están permitidos.",
             "instance": "/api/cuentas/1"
           }
```

---

#### HU-005: Eliminar Cuenta

```
Como:        Administrador del Sistema
Quiero:      Eliminar una cuenta del sistema
Para:        Desactivar un producto financiero cuando ya no sea necesario

Prioridad:   Media
Estimación:  XS
Dependencias: Ninguna
Capa:        Backend
```

#### Criterios de Aceptación — HU-005

**CRITERIO-5.1: Eliminar cuenta existente (Happy Path)**
```gherkin
Dado que:  Una cuenta con `id = 1` existe
Cuando:    Envío DELETE /api/cuentas/1
Entonces:  El sistema retorna HTTP 204 No Content
           La cuenta se elimina del sistema
           Intentos posteriores de GET /api/cuentas/1 retornan 404
```

**CRITERIO-5.2: Manejar eliminación de cuenta no existente**
```gherkin
Dado que:  No existe una cuenta con `id = 999`
Cuando:    Envío DELETE /api/cuentas/999
Entonces:  El sistema retorna HTTP 404 Not Found
           La respuesta contiene (RFC 9457):
           {
             "type": "https://api.example.com/errors/account-not-found",
             "title": "Not Found",
             "status": 404,
             "detail": "Cuenta no encontrada con id: 999",
             "instance": "/api/cuentas/999"
           }
```

---

#### HU-006: Validaciones de Número de Cuenta

```
Como:        Sistema de Validación
Quiero:      Garantizar la unicidad del número de cuenta
Para:        Evitar duplicados y asegurar la integridad de datos

Prioridad:   Alta
Estimación:  XS
Dependencias: HU-001
Capa:        Backend
```

#### Criterios de Aceptación — HU-006

**CRITERIO-6.1: Rechazar número de cuenta vacío**
```gherkin
Dado que:  Un número de cuenta es obligatorio
Cuando:    Intento crear una cuenta con numeroCuenta = "" o null
Entonces:  El sistema retorna HTTP 400 Bad Request
           La respuesta contiene (RFC 9457):
           {
             "type": "https://api.example.com/errors/account-validation",
             "title": "Bad Request",
             "status": 400,
             "detail": "El número de cuenta es obligatorio.",
             "instance": "/api/cuentas"
           }
```

**CRITERIO-6.2: Validar número de cuenta único en base de datos**
```gherkin
Dado que:  Dos cuentas no pueden tener el mismo número
Cuando:    Intento crear cuentas con el mismo numero_cuenta
Entonces:  El sistema retorna HTTP 409 Conflict en la segunda creación
           La respuesta contiene (RFC 9457):
           {
             "type": "https://api.example.com/errors/account-conflict",
             "title": "Conflict",
             "status": 409,
             "detail": "El número de cuenta ya existe.",
             "instance": "/api/cuentas"
           }
           Constraint UNIQUE en BaseDatos.sql lo previene a nivel BD
           GlobalExceptionHandler maneja AccountConflictException → 409
```

---

### Reglas de Negocio

1. **Número de Cuenta Único**: El campo `numero_cuenta` es UNIQUE en la base de datos. No pueden coexistir dos cuentas con el mismo número. Validación en:
   - Nivel BD: Constraint UNIQUE en BaseDatos.sql
   - Nivel Aplicación: `CuentaService.createAccount()` consulta `repository.existsByNumeroCuenta()`
   - Manejo de Error: `AccountConflictException` → GlobalExceptionHandler → HTTP 409

2. **Tipos de Cuenta Válidos**: Solo se permiten dos tipos — validación en constructor de dominio:
   - `"Ahorros"`
   - `"Corriente"`
   - Cualquier otro valor es rechazado con `AccountValidationException` en `Cuenta.abrir()`
   - Ubicación: `domain/model/Cuenta.java` - método factory `abrir()`

3. **Saldo No Negativo**: El `saldo_inicial` nunca puede ser negativo. Validación en constructor del dominio (`Cuenta.abrir()`) antes de crear la entidad. Rechaza con `AccountValidationException`.

4. **Número de Cuenta Inmutable**: Una vez creada la cuenta, el `numero_cuenta` NO puede ser modificado en operaciones PUT/PATCH. El método `actualizar()` no incluye este campo.

5. **Referencia a Cliente Obligatoria y Validada**: Toda cuenta DEBE estar vinculada a un `cliente_id` existente en customerservice.
   - **Validación**: Síncrona vía puerto `ClientVerifier` al crear la cuenta
   - **Implementación**: `CuentaService.createAccount()` valida antes de persistir
   - **Error si no existe**: `AccountValidationException` → HTTP 400 → "El cliente con ID X no existe."
   - **Regla de Integridad**: Previene crear cuentas para clientes fantasma (fuga de integridad)

6. **Estado Booleano**: El campo `estado` es un booleano que indica si la cuenta está activa (true) o inactiva (false).

7. **Timestamps Automáticos**: Los campos `created_at` y `updated_at` son generados/actualizados automáticamente por la aplicación. Formato UTC en ISO 8601.

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades Afectadas
| Entidad | Almacén | Cambios | Descripción |
|---------|---------|---------|-------------|
| `Cuenta` | tabla `cuenta` (banking_db) | Sin cambios | Modelo de dominio para gestionar cuentas bancarias |

#### Esquema de Base de Datos (BaseDatos.sql)
```sql
CREATE TABLE cuenta (
    id BIGSERIAL PRIMARY KEY,
    numero_cuenta VARCHAR(20) NOT NULL UNIQUE,
    tipo_cuenta VARCHAR(20) NOT NULL,
    saldo_inicial DECIMAL(15,2) NOT NULL DEFAULT 0,
    estado BOOLEAN DEFAULT true,
    cliente_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Campos del Modelo
| Campo | Tipo | Obligatorio | Validación | Descripción |
|-------|------|-------------|------------|-------------|
| `id` | BIGSERIAL | sí | auto-generado (PK) | Identificador único de la cuenta |
| `numero_cuenta` | VARCHAR(20) | sí | UNIQUE, no null | Número de cuenta único en el sistema |
| `tipo_cuenta` | VARCHAR(20) | sí | "Ahorros" \| "Corriente" | Clasificación de la cuenta |
| `saldo_inicial` | DECIMAL(15,2) | sí | >= 0 | Saldo inicial de la cuenta |
| `estado` | BOOLEAN | sí | default true | Indica si la cuenta está activa |
| `cliente_id` | BIGINT | sí | NOT NULL (validación por ClientVerifier) | Referencia al cliente propietario |
| `created_at` | TIMESTAMP | sí | auto (UTC) | Timestamp de creación |
| `updated_at` | TIMESTAMP | sí | auto (UTC) | Timestamp de última actualización |

#### Índices y Constraints
- **PRIMARY KEY**: `id` — búsqueda rápida por identificador
- **UNIQUE**: `numero_cuenta` — garantiza unicidad de números de cuenta
- **NOTA DE INTEGRIDAD**: En arquitectura de microservicios no se asume FK física cross-service; la existencia de `cliente_id` se garantiza en capa de aplicación vía `ClientVerifier`.

---

### API Endpoints

#### POST /api/cuentas
- **Descripción**: Crea una nueva cuenta bancaria
- **Auth requerida**: No (por ahora)
- **Request Body**:
  ```json
  {
    "clienteId": 1,
    "numeroCuenta": "123456789",
    "tipoCuenta": "Ahorros",
    "saldoInicial": 1000.00,
    "estado": true
  }
  ```
- **Response 201 Created**:
  ```json
  {
    "data": {
      "id": 1,
      "clienteId": 1,
      "numeroCuenta": "123456789",
      "tipoCuenta": "Ahorros",
      "saldoInicial": 1000.00,
      "estado": true
    },
    "timestamp": "2026-04-29T10:30:00Z"
  }
  ```
- **Response 400 Bad Request**: Campo faltante, tipo inválido, saldo negativo, cliente inexistente
- **Response 400 Bad Request**:
  ```json
  {
    "type": "https://api.example.com/errors/account-validation",
    "title": "Bad Request",
    "status": 400,
    "detail": "Error de validación en la solicitud.",
    "instance": "/api/cuentas"
  }
  ```
- **Response 409 Conflict**: Número de cuenta ya existe
  ```json
  {
    "type": "https://api.example.com/errors/account-conflict",
    "title": "Conflict",
    "status": 409,
    "detail": "El número de cuenta ya existe.",
    "instance": "/api/cuentas"
  }
  ```
  Manejado por `GlobalExceptionHandler` cuando `AccountConflictException` es lanzada por `CuentaService.createAccount()`.
- **Errores específicos**:
  - `"El número de cuenta es obligatorio."`
  - `"Tipo de cuenta inválido. Solo 'Ahorros' o 'Corriente' están permitidos."`
  - `"El saldo no puede ser negativo."`
- `"El cliente con ID X no existe."`

#### GET /api/cuentas
- **Descripción**: Lista todas las cuentas
- **Auth requerida**: No (por ahora)
- **Query Parameters**: Ninguno
- **Response 200 OK**:
  ```json
  {
    "data": [
      {
        "id": 1,
        "clienteId": 1,
        "numeroCuenta": "123456789",
        "tipoCuenta": "Ahorros",
        "saldoInicial": 1000.00,
        "estado": true
      },
      {
        "id": 2,
        "clienteId": 2,
        "numeroCuenta": "987654321",
        "tipoCuenta": "Corriente",
        "saldoInicial": 5000.00,
        "estado": true
      }
    ],
    "timestamp": "2026-04-29T10:30:00Z"
  }
  ```
- **Response 200 (lista vacía)**:
  ```json
  {
    "data": [],
    "timestamp": "2026-04-29T10:30:00Z"
  }
  ```

#### GET /api/cuentas/{id}
- **Descripción**: Obtiene una cuenta específica por ID
- **Auth requerida**: No (por ahora)
- **Path Parameters**: `id` (BIGINT) — identificador de la cuenta
- **Response 200 OK**:
  ```json
  {
    "data": {
      "id": 1,
      "clienteId": 1,
      "numeroCuenta": "123456789",
      "tipoCuenta": "Ahorros",
      "saldoInicial": 1000.00,
      "estado": true
    },
    "timestamp": "2026-04-29T10:30:00Z"
  }
  ```
- **Response 404 Not Found**: La cuenta no existe
  ```json
  {
    "type": "https://api.example.com/errors/account-not-found",
    "title": "Not Found",
    "status": 404,
    "detail": "Cuenta no encontrada con id: 999",
    "instance": "/api/cuentas/999"
  }
  ```

#### PUT /api/cuentas/{id}
- **Descripción**: Actualiza una cuenta existente (reemplazo completo de campos permitidos)
- **Auth requerida**: No (por ahora)
- **Path Parameters**: `id` (BIGINT)
- **Request Body**:
  ```json
  {
    "tipoCuenta": "Corriente",
    "estado": true
  }
  ```
- **Response 200 OK**:
  ```json
  {
    "data": {
      "id": 1,
      "clienteId": 1,
      "numeroCuenta": "123456789",
      "tipoCuenta": "Corriente",
      "saldoInicial": 1000.00,
      "estado": true
    },
    "timestamp": "2026-04-29T10:30:00Z"
  }
  ```
- **Response 400 Bad Request**:
  ```json
  {
    "type": "https://api.example.com/errors/account-validation",
    "title": "Bad Request",
    "status": 400,
    "detail": "Tipo de cuenta inválido. Solo 'Ahorros' o 'Corriente' están permitidos.",
    "instance": "/api/cuentas/1"
  }
  ```
- **Response 404 Not Found**:
  ```json
  {
    "type": "https://api.example.com/errors/account-not-found",
    "title": "Not Found",
    "status": 404,
    "detail": "Cuenta no encontrada con id: 999",
    "instance": "/api/cuentas/999"
  }
  ```
- **Notas**:
  - `numeroCuenta` se ignora si se envía (campo inmutable)
  - `saldoInicial` se ignora (no editable mediante PUT)
  - `updated_at` se actualiza automáticamente

#### PATCH /api/cuentas/{id}
- **Descripción**: Actualiza parcialmente una cuenta (actualización selectiva)
- **Auth requerida**: No (por ahora)
- **Path Parameters**: `id` (BIGINT)
- **Request Body** (campos opcionales):
  ```json
  {
    "estado": false
  }
  ```
- **Response 200 OK**: Mismo formato que PUT
- **Response 400 Bad Request**:
  ```json
  {
    "type": "https://api.example.com/errors/account-validation",
    "title": "Bad Request",
    "status": 400,
    "detail": "Tipo de cuenta inválido. Solo 'Ahorros' o 'Corriente' están permitidos.",
    "instance": "/api/cuentas/1"
  }
  ```
- **Response 404 Not Found**:
  ```json
  {
    "type": "https://api.example.com/errors/account-not-found",
    "title": "Not Found",
    "status": 404,
    "detail": "Cuenta no encontrada con id: 999",
    "instance": "/api/cuentas/999"
  }
  ```
- **Behavior**: En la implementación actual, PATCH se comporta igual que PUT (acepta los mismos campos opcionales)

#### DELETE /api/cuentas/{id}
- **Descripción**: Elimina una cuenta
- **Auth requerida**: No (por ahora)
- **Path Parameters**: `id` (BIGINT)
- **Response 204 No Content**: Eliminación exitosa (sin body)
- **Response 404 Not Found**:
  ```json
  {
    "type": "https://api.example.com/errors/account-not-found",
    "title": "Not Found",
    "status": 404,
    "detail": "Cuenta no encontrada con id: 999",
    "instance": "/api/cuentas/999"
  }
  ```
- **Notas**:
  - Después de eliminar, intentos posteriores de acceso retornan 404

---

### Puerto de Validación de Cliente (ClientVerifier)

**Interfaz**: `domain/ports/out/ClientVerifier.java`

```java
public interface ClientVerifier {
    boolean existsById(Long clienteId);
}
```

**Comportamiento**:
- **Happy Path**: Llamada REST GET `/api/clientes/{clienteId}` a customerservice
  - Respuesta 200 → Cliente existe → retorna `true`
- **Error Path**: 
  - Respuesta 404 → Cliente no existe → retorna `false` (service lanza `AccountValidationException` HTTP 400)
  - Respuesta 5xx (timeout, unavailable) → excepción técnica de infraestructura (retry policy opcional)

**Implementación**: `infrastructure/output/ClientVerifierAdapter.java`
- Usa `RestTemplate` o `WebClient` (preferible) para llamada síncrona
- Configurar timeout: 5 segundos máximo
- Punto de llamada: `CuentaService.createAccount()` antes de crear la entidad

**Ejemplo de Uso en Service**:
```java
@Override
public Cuenta createAccount(Long clienteId, String numeroCuenta, String tipoCuenta, BigDecimal saldoInicial) {
    // 1. Validar existencia del cliente (integridad referencial)
  if (!clientVerifier.existsById(clienteId)) {
        throw new AccountValidationException("El cliente con ID " + clienteId + " no existe.");
    }
    
    // 2. Validar unicidad del número de cuenta
    if (repository.existsByNumeroCuenta(numeroCuenta)) {
        throw new AccountConflictException("El número de cuenta ya existe.");
    }
    
    // 3. Crear la cuenta
    Cuenta cuenta = Cuenta.abrir(clienteId, numeroCuenta, tipoCuenta, saldoInicial);
    return repository.save(cuenta);
}
```

---

### Arquitectura y Dependencias

#### Configuración de Infraestructura

**application.yaml** — Virtual Threads (Java 21)
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

**Justificación**: Habilita virtual threads para mejorar throughput en operaciones I/O concurrentes (especialmente en las llamadas REST a clientVerifier). Recomendado para microservicios con alto volumen de transacciones.

---

#### Paquetes Java Requeridos
- `com.bank.bankingservice.domain.model` — Modelo `Cuenta` (dominio puro, Java v21)
- `com.bank.bankingservice.domain.ports.in` — Interfaz `CuentaUseCase`
- `com.bank.bankingservice.domain.ports.out` — Interfaces de salida:
  - `CuentaRepository` — operaciones CRUD en BD
  - `ClientVerifier` — validación de existencia de cliente en customerservice (REST)
- `com.bank.bankingservice.domain.exception` — Excepciones:
  - `AccountValidationException` (extends `DomainException`) → HTTP 400
  - `AccountConflictException` (extends `DomainException`) → HTTP 409
  - `AccountNotFoundException` (extends `DomainException`) → HTTP 404
- `com.bank.bankingservice.application.service` — Implementación `CuentaService`
- `com.bank.bankingservice.application.dto` — DTOs (`CuentaCreateRequest`, `CuentaUpdateRequest`, `CuentaResponse`)
- `com.bank.bankingservice.infrastructure.input.rest` — Controller `CuentaController`
- `com.bank.bankingservice.infrastructure.config` — `GlobalExceptionHandler` (@RestControllerAdvice)
- `com.bank.bankingservice.infrastructure.output` — Adaptadores:
  - `CuentaRepository` (JPA)
  - `ClientVerifierAdapter` (REST client para customerservice)

#### Servicios Externos
- **customerservice**: Microservicio de clientes.
  - **Validación de Existencia del Cliente**: OBLIGATORIA en tiempo de creación de cuenta
  - Mecanismo: Puerto de Salida `ClientVerifier` → implementación REST (llamada síncrona a `/api/clientes/{id}`)
  - **Regla de Integridad**: No se permite crear una cuenta para un `cliente_id` que NO existe en customerservice
  - Comportamiento: Si cliente no existe → `AccountValidationException` HTTP 400
  - Futuro: Posible evolución a event-driven (RabbitMQ `cliente.creado`) en fase posterior

#### Dependencias Internas
- Spring Boot 4
- Spring Data JPA
- Spring Web
- Lombok (opcional, para getters/setters)
- JUnit 5 + Mockito (para tests)

---

### Notas de Implementación

> **Arquitectura Hexagonal Completa**: La implementación sigue estrictamente el patrón hexagonal con capas separadas (Domain → Application → Infrastructure). El modelo de dominio es Java PURO (v21) sin anotaciones de framework (sin Spring, sin JPA).

> **Validación en Constructor (Java Puro v21)**: Las reglas de negocio residen en el constructor o factory methods del modelo de dominio:
>   - **Tipo de Cuenta**: Validación en el constructor `Cuenta.abrir()` y `Cuenta.reconstituir()`. Solo acepta "Ahorros" o "Corriente".
>   - **Número de Cuenta**: Validado como no null/vacío en constructor. Unicidad verificada en `CuentaService.createAccount()` via `CuentaRepository.existsByNumeroCuenta()`.
>   - **Saldo No Negativo**: Validado en constructor, rechaza valores negativos con excepción de dominio.
>   - Método factory `Cuenta.abrir(clienteId, numeroCuenta, tipoCuenta, saldoInicial)` encapsula validación.

> **Puerto de Validación de Cliente (ClientVerifier)**: Nuevo puerto de salida en `domain/ports/out/` para validar existencia de cliente en customerservice de manera síncrona (integridad referencial).
>   - Ubicación de llamada: `CuentaService.createAccount()` **antes** de crear la entidad
>   - Implementación: `ClientVerifierAdapter` con `RestTemplate` o `WebClient`
>   - Contrato: `existsById(Long clienteId): boolean`
>   - Comportamiento: Si retorna `false` → `CuentaService` lanza `AccountValidationException` → HTTP 400
>   - Timeout: 5 segundos máximo para no bloquear transacciones

> **Manejo de Errores con GlobalExceptionHandler**: Integración con `GlobalExceptionHandler` en `infrastructure/config/`:
>   - `AccountValidationException` → HTTP 400 Bad Request (errores de validación incluyendo cliente no encontrado)
>   - `AccountNotFoundException` → HTTP 404 Not Found (cuenta no existe)
>   - `AccountConflictException` → HTTP 409 Conflict (número de cuenta duplicado)
>   - Patrón similar a customerservice: handler centralizado retorna ErrorResponse con RFC 9457.

> **Virtual Threads (Java 21)**: Habilitados en `application.yaml` con `spring.threads.virtual.enabled: true`. Mejora throughput en llamadas I/O concurrentes, especialmente en validaciones REST a clientVerifier.

> **DTOs Inmutables**: Se usan `record` (Java 16+) para DTOs, garantizando inmutabilidad en la capa de presentación.

> **Base de Datos como Fuente de Verdad**: El esquema está definido en `BaseDatos.sql`. NO se usa `ddl-auto=update`. JPA mapea entidades a tablas existentes.

> **RFC 9457 + Envelope JSON**: Las respuestas siguen el estándar de errores RFC 9457 con estructura:
>   ```json
>   {
>     "type": "https://api.example.com/errors/account-conflict",
>     "title": "Conflict",
>     "status": 409,
>     "detail": "El número de cuenta ya existe.",
>     "instance": "/api/cuentas"
>   }
>   ```

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada ítem (`[x]`) al completarlo.

### Backend

#### Implementación
- [x] Crear modelo de dominio `Cuenta` — Java PURO con métodos factory (abrir, reconstituir)
- [x] Implementar `CuentaUseCase` (interfaz) — puertos de entrada
- [x] Implementar `CuentaRepository` (interfaz) — puertos de salida (persistencia)
- [ ] Implementar `ClientVerifier` (interfaz) — puerto de salida (validación de cliente)
- [x] Implementar `CuentaService` — lógica de negocio (crearCuenta, obtenerCuenta, actualizarCuenta, eliminarCuenta)
- [ ] Actualizar `CuentaService.createAccount()` — integrar validación de cliente via ClientVerifier
- [x] Implementar `CuentaController` (/api/cuentas) — endpoints CRUD con ResponseEntity y envelope JSON
- [x] Crear DTOs: `CuentaCreateRequest`, `CuentaUpdateRequest`, `CuentaResponse`
- [x] Implementar excepciones de dominio: `AccountValidationException`, `AccountConflictException`, `AccountNotFoundException`
- [x] Mapear JPA `CuentaDocument` (entidad) a tabla `cuenta`
- [x] Implementar adaptador JPA `CuentaRepositoryAdapter`
- [ ] Implementar adaptador REST `ClientVerifierAdapter` — llamada a customerservice
- [ ] Configurar `application.yaml` — Virtual Threads: `spring.threads.virtual.enabled: true`

#### Tests Backend (Matriz 3-2-1)

**Regla: POR CADA MÉTODO DE SERVICIO = 3 tests | POR CADA ENDPOINT = 3 tests | POR CADA ADAPTER = 2 tests**

##### Service Tests: `CuentaServiceTests.java`
- [ ] `createAccount_success` — Happy path con todos los campos válidos
- [ ] `createAccount_throws_conflict_when_account_number_exists` — Número de cuenta duplicado
- [ ] `createAccount_throws_validation_when_invalid_type` — Tipo de cuenta inválido
- [ ] `createAccount_throws_validation_when_client_not_exists` — Cliente inexistente
- [ ] `getAllAccounts_returns_list_success` — Lista todas las cuentas
- [ ] `getAllAccounts_returns_empty_list_when_no_accounts` — Lista vacía
- [ ] `getAllAccounts_edge_case` — (Edge case: ej. con saldo muy grande)
- [ ] `getAccountById_returns_account_success` — Obtener cuenta por ID
- [ ] `getAccountById_throws_not_found` — Cuenta inexistente
- [ ] `getAccountById_edge_case` — (Edge case: ej. ID = 0 o negativo)
- [ ] `updateAccount_success` — Actualizar tipo y estado
- [ ] `updateAccount_throws_not_found` — Cuenta inexistente
- [ ] `updateAccount_ignores_account_number` — Número de cuenta no cambia
- [ ] `deleteAccount_success` — Eliminación exitosa
- [ ] `deleteAccount_throws_not_found` — Cuenta inexistente
- [ ] `deleteAccount_edge_case` — (Edge case: eliminar dos veces)

##### Controller Tests: `CuentaControllerTests.java`
- [ ] `post_api_cuentas_returns_201_created` — Crear cuenta exitosa
- [ ] `post_api_cuentas_returns_400_bad_request` — Campo faltante o inválido
- [ ] `post_api_cuentas_returns_409_conflict` — Número de cuenta duplicado
- [ ] `get_api_cuentas_returns_200_ok` — Listar cuentas
- [ ] `get_api_cuentas_returns_200_empty_list` — Lista vacía
- [ ] `get_api_cuentas_edge_case` — (Edge case: performance con muchas cuentas)
- [ ] `get_api_cuentas_id_returns_200_ok` — Obtener cuenta por ID
- [ ] `get_api_cuentas_id_returns_404_not_found` — Cuenta no existe
- [ ] `get_api_cuentas_id_edge_case` — (Edge case: ID inválido)
- [ ] `put_api_cuentas_id_returns_200_ok` — Actualizar cuenta
- [ ] `put_api_cuentas_id_returns_400_bad_request` — Tipo inválido
- [ ] `put_api_cuentas_id_returns_404_not_found` — Cuenta no existe
- [ ] `delete_api_cuentas_id_returns_204_no_content` — Eliminar cuenta
- [ ] `delete_api_cuentas_id_returns_404_not_found` — Cuenta no existe
- [ ] `delete_api_cuentas_id_edge_case` — (Edge case: eliminar dos veces)

##### Adapter Tests: `CuentaRepositoryAdapterTests.java`
- [ ] `save_returns_entity_with_id` — Guardar y obtener ID generado
- [ ] `save_updates_existing_entity` — Actualizar entidad existente
- [ ] `findById_returns_optional_with_entity` — Buscar por ID
- [ ] `findById_returns_empty_optional` — ID no existe
- [ ] `findAll_returns_list_of_entities` — Listar todas
- [ ] `findAll_returns_empty_list_when_no_entities` — Lista vacía
- [ ] `existsByNumeroCuenta_returns_true_when_exists` — Verificar existencia
- [ ] `existsByNumeroCuenta_returns_false_when_not_exists` — No existe
- [ ] `deleteById_removes_entity` — Eliminar por ID
- [ ] `deleteById_idempotent` — Eliminar dos veces (no error)

---

### QA

#### Test Strategy
- [ ] Ejecutar skill `/gherkin-case-generator` → Mapear criterios CRITERIO-1.1 a 6.2 a escenarios BDD
- [ ] Ejecutar skill `/risk-identifier` → Clasificación ASD de riesgos (Alto/Medio/Bajo)
- [ ] Ejecutar skill `/performance-analyzer` → Plan de pruebas de performance (Load, Stress, Spike)
- [ ] Crear matriz de cobertura: criterios vs tests implementados

#### Validación de Cobertura
- [ ] Verificar que CRITERIO-1.1 a 1.4 (Crear Cuenta) tienen tests en `CuentaServiceTests` y `CuentaControllerTests`
- [ ] Verificar que CRITERIO-2.1 a 2.2 (Listar Cuentas) tienen tests
- [ ] Verificar que CRITERIO-3.1 a 3.2 (Obtener Cuenta) tienen tests
- [ ] Verificar que CRITERIO-4.1 a 4.4 (Actualizar Cuenta) tienen tests
- [ ] Verificar que CRITERIO-5.1 a 5.2 (Eliminar Cuenta) tienen tests
- [ ] Verificar que CRITERIO-6.1 a 6.2 (Validaciones) tienen tests
- [ ] Cobertura mínima: 80% (líneas de código en domain/ + service/)

#### Actualización de Estado
- [ ] Backend APPROVED: Confirmar que HU-001 a HU-006 están IMPLEMENTED
- [ ] QA APPROVED: Confirmar que Gherkin, riesgos y performance están documentados
- [ ] **Cambiar estado spec de DRAFT a APPROVED** (solo después de Code Review)
- [ ] **Cambiar estado spec de APPROVED a IN_PROGRESS** (cuando comience la implementación)
- [ ] **Cambiar estado spec de IN_PROGRESS a IMPLEMENTED** (cuando QA valide todo)

---

## Observaciones Finales

Esta especificación documenta la funcionalidad de **Administración de Cuentas Bancarias** de manera completa y accionable. El código ya existe en el repositorio, por lo que esta spec sirve como:

1. **Referencia técnica** para el equipo de QA
2. **Base para generar tests automatizados** (Gherkin con `/gherkin-case-generator`)
3. **Documento de riesgos y análisis** (con `/risk-identifier` y `/performance-analyzer`)
4. **Fuente de verdad** para cambios posteriores

Cambios futuros (Fase 2, Fase 3) deben crear nuevas specs con números incrementales (SPEC-002, SPEC-003, etc.).
