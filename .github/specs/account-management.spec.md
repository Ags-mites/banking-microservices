---
id: SPEC-002
status: DRAFT
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
           - "saldoDisponible": 1000.00
           - "estado": true
           - "clienteId": 1
           - "timestamp": ISO 8601 en UTC
```

**CRITERIO-1.2: Validar número de cuenta único**
```gherkin
Dado que:  Una cuenta con número "123456789" ya existe
Cuando:    Intento crear otra cuenta con el mismo número
Entonces:  El sistema retorna HTTP 409 Conflict
           El mensaje de error indica:
           "El número de cuenta ya existe."
```

**CRITERIO-1.3: Validar tipo de cuenta**
```gherkin
Dado que:  Los tipos válidos son "Ahorros" y "Corriente"
Cuando:    Intento crear una cuenta con tipo "Invalido"
Entonces:  El sistema retorna HTTP 400 Bad Request
           El mensaje indica:
           "Tipo de cuenta inválido. Solo 'Ahorros' o 'Corriente' están permitidos."
```

**CRITERIO-1.4: Validar saldo no negativo (Edge Case)**
```gherkin
Dado que:  Se requiere un saldo válido
Cuando:    Intento crear una cuenta con saldoInicial = -500.00
Entonces:  El sistema retorna HTTP 400 Bad Request
           El mensaje indica:
           "El saldo no puede ser negativo."
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
           Cada cuenta incluye: id, numeroCuenta, tipoCuenta, saldoInicial, saldoDisponible, estado, clienteId
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
           El mensaje indica:
           "Cuenta no encontrada con id: 999"
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
           El mensaje indica el error de validación
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
           El mensaje indica que la cuenta no existe
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
           El mensaje indica:
           "El número de cuenta es obligatorio."
```

**CRITERIO-6.2: Validar número de cuenta único en base de datos**
```gherkin
Dado que:  Dos cuentas no pueden tener el mismo número
Cuando:    Intento crear cuentas con el mismo numero_cuenta
Entonces:  El sistema retorna HTTP 409 Conflict en la segunda creación
           El constraint UNIQUE en BaseDatos.sql lo previene
```

---

### Reglas de Negocio

1. **Número de Cuenta Único**: El campo `numero_cuenta` es UNIQUE en la base de datos. No pueden coexistir dos cuentas con el mismo número.

2. **Tipos de Cuenta Válidos**: Solo se permiten dos tipos:
   - `"Ahorros"`
   - `"Corriente"`
   
   Cualquier otro valor es rechazado con validación de dominio.

3. **Saldo No Negativo**: El `saldo_inicial` y `saldo_disponible` nunca pueden ser negativos. Validación aplicada en el modelo de dominio.

4. **Número de Cuenta Inmutable**: Una vez creada la cuenta, el `numero_cuenta` NO puede ser modificado en operaciones PUT/PATCH.

5. **Referencia a Cliente Obligatoria**: Toda cuenta debe estar vinculada a un `cliente_id` existente en el microservicio de clientes (customerservice).

6. **Estado Booleano**: El campo `estado` es un booleano que indica si la cuenta está activa (true) o inactiva (false).

7. **Timestamps Automáticos**: Los campos `created_at` y `updated_at` son generados/actualizados automáticamente por la aplicación. Formato UTC en ISO 8601.

8. **Version para Concurrencia Optimista**: El campo `version` (INTEGER) implementa optimistic locking para evitar conflictos en actualizaciones concurrentes.

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
    saldo_disponible DECIMAL(15,2) NOT NULL DEFAULT 0,
    estado BOOLEAN DEFAULT true,
    cliente_id BIGINT NOT NULL,
    version INTEGER DEFAULT 0,
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
| `saldo_disponible` | DECIMAL(15,2) | sí | >= 0 | Saldo disponible actual |
| `estado` | BOOLEAN | sí | default true | Indica si la cuenta está activa |
| `cliente_id` | BIGINT | sí | NOT NULL (FK → cliente) | Referencia al cliente propietario |
| `version` | INTEGER | sí | default 0 | Optimistic locking para concurrencia |
| `created_at` | TIMESTAMP | sí | auto (UTC) | Timestamp de creación |
| `updated_at` | TIMESTAMP | sí | auto (UTC) | Timestamp de última actualización |

#### Índices y Constraints
- **PRIMARY KEY**: `id` — búsqueda rápida por identificador
- **UNIQUE**: `numero_cuenta` — garantiza unicidad de números de cuenta
- **FOREIGN KEY**: `cliente_id` → tabla `cliente` (customer_db) — integridad referencial

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
      "saldoDisponible": 1000.00,
      "estado": true
    },
    "timestamp": "2026-04-29T10:30:00Z"
  }
  ```
- **Response 400 Bad Request**: Campo faltante, tipo inválido, saldo negativo
- **Response 409 Conflict**: Número de cuenta ya existe
- **Errores específicos**:
  - `"El número de cuenta es obligatorio."`
  - `"Tipo de cuenta inválido. Solo 'Ahorros' o 'Corriente' están permitidos."`
  - `"El saldo no puede ser negativo."`
  - `"El número de cuenta ya existe."`

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
        "saldoDisponible": 1000.00,
        "estado": true
      },
      {
        "id": 2,
        "clienteId": 2,
        "numeroCuenta": "987654321",
        "tipoCuenta": "Corriente",
        "saldoInicial": 5000.00,
        "saldoDisponible": 5000.00,
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
      "saldoDisponible": 1000.00,
      "estado": true
    },
    "timestamp": "2026-04-29T10:30:00Z"
  }
  ```
- **Response 404 Not Found**: La cuenta no existe
  ```json
  {
    "title": "Not Found — Account not found",
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
      "saldoDisponible": 1000.00,
      "estado": true
    },
    "timestamp": "2026-04-29T10:30:00Z"
  }
  ```
- **Response 400 Bad Request**: Tipo de cuenta inválido
- **Response 404 Not Found**: La cuenta no existe
- **Notas**:
  - `numeroCuenta` se ignora si se envía (campo inmutable)
  - `saldoInicial` y `saldoDisponible` se ignoran (no editables mediante PUT)
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
- **Behavior**: En la implementación actual, PATCH se comporta igual que PUT (acepta los mismos campos opcionales)

#### DELETE /api/cuentas/{id}
- **Descripción**: Elimina una cuenta
- **Auth requerida**: No (por ahora)
- **Path Parameters**: `id` (BIGINT)
- **Response 204 No Content**: Eliminación exitosa (sin body)
- **Response 404 Not Found**: La cuenta no existe
- **Notas**:
  - Después de eliminar, intentos posteriores de acceso retornan 404

---

### Arquitectura y Dependencias

#### Paquetes Java Requeridos
- `com.bank.bankingservice.domain.model` — Modelo `Cuenta` (dominio puro)
- `com.bank.bankingservice.domain.ports.in` — Interfaz `CuentaUseCase`
- `com.bank.bankingservice.domain.ports.out` — Interfaz `CuentaRepository`
- `com.bank.bankingservice.domain.exception` — Excepciones (`AccountValidationException`, `AccountConflictException`, `AccountNotFoundException`)
- `com.bank.bankingservice.application.service` — Implementación `CuentaService`
- `com.bank.bankingservice.application.dto` — DTOs (`CuentaCreateRequest`, `CuentaUpdateRequest`, `CuentaResponse`)
- `com.bank.bankingservice.infrastructure.input.rest` — Controller `CuentaController`
- `com.bank.bankingservice.infrastructure.output` — Adaptador JPA `CuentaRepository`

#### Servicios Externos
- **customerservice**: Microservicio de clientes. La tabla `cuenta` tiene FK → `cliente.id` pero NO hay validación en tiempo de ejecución (posible integración vía eventos con RabbitMQ en futuras fases).

#### Dependencias Internas
- Spring Boot 4
- Spring Data JPA
- Spring Web
- Lombok (opcional, para getters/setters)
- JUnit 5 + Mockito (para tests)

---

### Notas de Implementación

> **Arquitectura Hexagonal Completa**: La implementación sigue estrictamente el patrón hexagonal con capas separadas (Domain → Application → Infrastructure). El modelo de dominio es Java PURO sin anotaciones de framework.

> **Validación en Dominio**: Las reglas de negocio (número de cuenta único, tipos válidos, saldo no negativo) se validan en el modelo de dominio (`Cuenta`) antes de pasar por el repositorio.

> **DTOs Inmutables**: Se usan `record` (Java 16+) para DTOs, garantizando inmutabilidad en la capa de presentación.

> **Optimistic Locking**: El campo `version` implementa control de concurrencia optimista. Útil para detectar conflictos en actualizaciones simultáneas.

> **Basque de datos como Fuente de Verdad**: El esquema está definido en `BaseDatos.sql`. NO se usa `ddl-auto=update`. JPA mapea entidades a tablas existentes.

> **RFC 9457 + Envelope JSON**: Las respuestas siguen el estándar de errores RFC 9457 con envelope JSON que incluye timestamp en cada respuesta.

> **Concurrencia**: La aplicación usa Virtual Threads de Java 21 para mejorar throughput en operaciones I/O (si está habilitado en `application.yaml`).

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada ítem (`[x]`) al completarlo.

### Backend

#### Implementación
- [x] Crear modelo de dominio `Cuenta` — Java PURO con métodos factory (abrir, reconstituir)
- [x] Implementar `CuentaUseCase` (interfaz) — puertos de entrada
- [x] Implementar `CuentaRepository` (interfaz) — puertos de salida
- [x] Implementar `CuentaService` — lógica de negocio (crearCuenta, obtenerCuenta, actualizarCuenta, eliminarCuenta)
- [x] Implementar `CuentaController` (/api/cuentas) — endpoints CRUD con ResponseEntity y envelope JSON
- [x] Crear DTOs: `CuentaCreateRequest`, `CuentaUpdateRequest`, `CuentaResponse`
- [x] Implementar excepciones de dominio: `AccountValidationException`, `AccountConflictException`, `AccountNotFoundException`
- [x] Mapear JPA `CuentaDocument` (entidad) a tabla `cuenta`
- [x] Implementar adaptador JPA `CuentaRepositoryAdapter`

#### Tests Backend (Matriz 3-2-1)

**Regla: POR CADA MÉTODO DE SERVICIO = 3 tests | POR CADA ENDPOINT = 3 tests | POR CADA ADAPTER = 2 tests**

##### Service Tests: `CuentaServiceTests.java`
- [ ] `createAccount_success` — Happy path con todos los campos válidos
- [ ] `createAccount_throws_conflict_when_account_number_exists` — Número de cuenta duplicado
- [ ] `createAccount_throws_validation_when_invalid_type` — Tipo de cuenta inválido
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
