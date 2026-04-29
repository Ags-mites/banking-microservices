---
id: SPEC-001
status: APPROVED
feature: customer-management
created: 2026-04-28
updated: 2026-04-28
author: spec-generator
version: "1.0"
related-specs: []
---

# Spec: Gestión Integral de Clientes y Personas

> **Estado:** `APPROVED` ✅ — Refactorización completada con todos los criterios técnicos obligatorios.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción
Implementar un CRUD completo para la gestión integral de clientes y personas en el sistema bancario. Permite crear, leer, actualizar y eliminar registros de personas (datos básicos) y clientes (con credenciales de negocio). Incluye validaciones de unicidad de identificación, seguridad de contraseña, y emisión de eventos asincronos al crear clientes.

### Requerimiento de Negocio
Como administrador del sistema, quiero realizar el CRUD de clientes, para mantener un registro de los usuarios del banco.

### Historias de Usuario

#### HU-001: Creación integrada de Persona + Cliente

```
Como:        Administrador del Sistema
Quiero:      Crear un cliente proporcionando en una sola solicitud todos los datos necesarios (persona + credenciales)
Para:        Simplificar el registro de usuarios sin requerir dos solicitudes separadas

Prioridad:   Alta
Estimación:  M
Dependencias: Ninguna
Capa:        Backend
```

#### Criterios de Aceptación — HU-001

**Happy Path — CRITERIO-1.1**
```gherkin
Dado que    proporciono datos completos (nombre, género, edad, identificación, dirección, teléfono, contraseña, estado)
Cuando      envío una solicitud POST a /api/clientes con la información válida
Entonces    el sistema crea una nueva Persona exitosamente
Y           crea un Cliente vinculado a esa Persona
Y           retorna HTTP 201 con todos los datos (sin exponer contraseña)
Y           la respuesta contiene el id único del cliente asignado automáticamente
```

**Error Path — CRITERIO-1.2**
```gherkin
Dado que    intento crear un cliente con identificación "12345678" que ya existe
Cuando      envío la solicitud POST a /api/clientes con esa identificación
Entonces    el sistema retorna HTTP 409 Conflict
Y           el mensaje de error indica que la identificación ya está en uso
Y           ningún registro de Persona ni Cliente se crea
```

**Edge Case — CRITERIO-1.3**
```gherkin
Dado que    intento crear un cliente con una contraseña de menos de 8 caracteres
Cuando      envío la solicitud POST a /api/clientes
Entonces    el sistema retorna HTTP 400 Bad Request
Y           el mensaje valida que la contraseña debe tener mínimo 8 caracteres
Y           ningún registro se crea
```

---

#### HU-002: CRUD de Cliente (Lectura, actualización y eliminación)

```
Como:        Administrador del Sistema
Quiero:      Leer, actualizar y eliminar clientes existentes en el sistema
Para:        Gestionar la información y ciclo de vida de los clientes

Prioridad:   Alta
Estimación:  M
Dependencias: Ninguna
Capa:        Backend
```

#### Criterios de Aceptación — HU-002

**Happy Path — CRITERIO-2.1**
```gherkin
Dado que    existen clientes en el sistema
Cuando      envío una solicitud GET a /api/clientes con su ID
Entonces    el sistema retorna HTTP 200 con todos los datos del cliente
Y           los datos incluyen: nombre, género, edad, identificación, dirección, teléfono, id, estado
Y           el campo contraseña NO es incluido en la respuesta
```

**Error Path — CRITERIO-2.2**
```gherkin
Dado que    intento obtener un cliente con un ID que no existe
Cuando      envío una solicitud GET a /api/clientes/999
Entonces    el sistema retorna HTTP 404 Not Found
Y           el mensaje indica que el cliente no existe
```

**Edge Case — CRITERIO-2.3**
```gherkin
Dado que    intento actualizar un cliente con datos inválidos (ej. contraseña < 8 caracteres)
Cuando      envío una solicitud PUT a /api/clientes/1
Entonces    el sistema retorna HTTP 400 Bad Request
Y           el cliente no es actualizado
```

---

#### HU-003: Listar todos los clientes (GET /api/clientes)

```
Como:        Administrador del Sistema
Quiero:      Obtener un listado de todos los clientes del sistema
Para:        Visualizar y auditar los usuarios activos

Prioridad:   Media
Estimación:  S
Dependencias: Ninguna
Capa:        Backend
```

#### Criterios de Aceptación — HU-003

**Happy Path — CRITERIO-3.1**
```gherkin
Dado que    existen múltiples clientes en el sistema
Cuando      envío una solicitud GET a /api/clientes
Entonces    el sistema retorna HTTP 200
Y           la respuesta incluye un array con todos los clientes
Y           cada cliente contiene: nombre, género, edad, identificación, dirección, teléfono, estado
Y           el campo password NO es incluido en la respuesta
```

**Edge Case — CRITERIO-3.2**
```gherkin
Dado que    no existe ningún cliente en el sistema
Cuando      envío una solicitud GET a /api/clientes
Entonces    el sistema retorna HTTP 200
Y           la respuesta es un array vacío
```

---

#### HU-004: Obtener cliente por ID (GET /api/clientes/{id})

```
Como:        Administrador del Sistema
Quiero:      Obtener los detalles completos de un cliente específico
Para:        Consultar información detallada del usuario

Prioridad:   Alta
Estimación:  S
Dependencias: Ninguna
Capa:        Backend
```

#### Criterios de Aceptación — HU-004

**Happy Path — CRITERIO-4.1**
```gherkin
Dado que    un cliente con id "1" existe en el sistema
Cuando      envío una solicitud GET a /api/clientes/1
Entonces    el sistema retorna HTTP 200
Y           la respuesta contiene todos los datos de la persona: nombre, género, edad, identificación, dirección, teléfono
Y           incluye estado
Y           NO incluye el campo password
```

**Error Path — CRITERIO-4.2**
```gherkin
Dado que    intento obtener un cliente con id "999" que no existe
Cuando      envío una solicitud GET a /api/clientes/999
Entonces    el sistema retorna HTTP 404 Not Found
Y           el mensaje de error indica que el cliente no existe
```

---

#### HU-005: Actualizar cliente completamente (PUT /api/clientes/{id})

```
Como:        Administrador del Sistema
Quiero:      Actualizar todos los datos de un cliente existente
Para:        Mantener la información actualizada del usuario

Prioridad:   Media
Estimación:  M
Dependencias: Ninguna
Capa:        Backend
```

#### Criterios de Aceptación — HU-005

**Happy Path — CRITERIO-5.1**
```gherkin
Dado que    un cliente con id "1" existe
Cuando      envío una solicitud PUT a /api/clientes/1 con datos actualizados
Entonces    el sistema actualiza todos los campos proporcionados
Y           retorna HTTP 200 con el cliente actualizado
Y           el campo password NO es incluido en la respuesta
Y           el id del cliente permanece sin cambios
```

**Error Path — CRITERIO-5.2**
```gherkin
Dado que    intento actualizar un cliente con id "999" que no existe
Cuando      envío una solicitud PUT a /api/clientes/999
Entonces    el sistema retorna HTTP 404 Not Found
```

---

#### HU-006: Actualizar cliente parcialmente (PATCH /api/clientes/{id})

```
Como:        Administrador del Sistema
Quiero:      Actualizar solo algunos campos de un cliente
Para:        Cambiar información específica sin afectar otros datos

Prioridad:   Media
Estimación:  S
Dependencias: Ninguna
Capa:        Backend
```

#### Criterios de Aceptación — HU-006

**Happy Path — CRITERIO-6.1**
```gherkin
Dado que    un cliente con id "1" existe
Cuando      envío una solicitud PATCH a /api/clientes/1 con solo el campo "estado"
Entonces    el sistema actualiza únicamente el campo proporcionado
Y           preserva todos los otros campos sin cambios
Y           retorna HTTP 200 con el cliente actualizado
```

**Edge Case — CRITERIO-6.2**
```gherkin
Dado que    envío un PATCH con una contraseña de menos de 8 caracteres
Cuando      intento actualizar el campo password de un cliente
Entonces    el sistema retorna HTTP 400 Bad Request con mensaje de validación
```

---

#### HU-007: Eliminar cliente (DELETE /api/clientes/{id})

```
Como:        Administrador del Sistema
Quiero:      Eliminar un cliente del sistema
Para:        Remover registros obsoletos o inactivos

Prioridad:   Media
Estimación:  S
Dependencias: Ninguna
Capa:        Backend
```

#### Criterios de Aceptación — HU-007

**Happy Path — CRITERIO-7.1**
```gherkin
Dado que    un cliente con id "1" existe
Cuando      envío una solicitud DELETE a /api/clientes/1
Entonces    el sistema elimina el cliente de la base de datos
Y           retorna HTTP 204 No Content
Y           el cliente ya no está disponible en consultas posteriores
```

**Error Path — CRITERIO-7.2**
```gherkin
Dado que    intento eliminar un cliente con id "999" que no existe
Cuando      envío una solicitud DELETE a /api/clientes/999
Entonces    el sistema retorna HTTP 404 Not Found
```

---

#### HU-008: Emitir evento "cliente.creado" en RabbitMQ

```
Como:        Sistema de Backend
Quiero:      Emitir un evento asincronamente cuando un cliente se crea exitosamente
Para:        Permitir que otros microservicios reaccionen a la creación de clientes (notificaciones, auditoría, sincronización)

Prioridad:   Alta
Estimación:  M
Dependencias: Ninguna
Capa:        Backend
```

#### Criterios de Aceptación — HU-008

**Happy Path — CRITERIO-8.1**
```gherkin
Dado que    un cliente válido es creado vía POST /api/clientes
Cuando      el cliente se persiste exitosamente en la base de datos
Entonces    el sistema emite un evento "cliente.creado" a RabbitMQ
Y           el evento contiene: id del cliente, nombre, identificación, estado, created_at
Y           el evento se envía a la exchange "customer.events" con routing key "cliente.creado"
```

**Error Path — CRITERIO-8.2**
```gherkin
Dado que    se envía un payload de cliente inválido (sin nombre, por ejemplo)
Cuando      la creación falla debido a errores de validación
Entonces    ningún evento "cliente.creado" es emitido a RabbitMQ
Y           el sistema retorna la respuesta de error correspondiente (HTTP 400)
```

**Edge Case — CRITERIO-8.3**
```gherkin
Dado que    la emisión del evento a RabbitMQ falla por conectividad
Cuando      un cliente se crea exitosamente pero RabbitMQ está indisponible
Entonces    el sistema loguea la falla de envío del evento
Y           el cliente permanece creado en BD (sin rollback)
Y           se reintenta el envío del evento según la política de reintentos
```

---

### Reglas de Negocio

1. **Identificación única**: Una persona no puede tener dos registros con la misma identificación. La columna `identificacion` en tabla `persona` tiene UNIQUE constraint.

2. **Herencia de datos**: Un cliente hereda automáticamente todos los campos de su persona vinculada (nombre, género, edad, identificación, dirección, teléfono).

3. **Contraseña mínima**: La contraseña de un cliente debe tener un mínimo de 8 caracteres. Implementado con CHECK constraint `LENGTH(contrasena) >= 8` en tabla `cliente`.

4. **Contraseña no expuesta**: El campo `contrasena` NUNCA debe ser incluido en respuestas de API (GET, POST, PUT, PATCH).

5. **ID único del cliente**: El sistema genera automáticamente un `id` único (BIGSERIAL) para cada cliente al insertarlo en la tabla. Este es el identificador técnico y de negocio simultáneamente.

6. **Eventos transaccionales**: Un evento "cliente.creado" solo se emite si la creación del cliente fue exitosa en la base de datos. Si la validación o inserción falla, no se emite evento.

7. **Creación integrada**: Al crear un cliente vía POST /api/clientes, se crean automáticamente tanto la Persona como el Cliente en una sola transacción.

8. **Estado por defecto**: El campo `estado` de un cliente se inicializa en `true` (activo) si no se proporciona durante la creación.

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades afectadas

| Entidad | Almacén | Cambios | Descripción |
|---------|---------|---------|-------------|
| `Persona` | tabla `persona` en `customer_db` | sin cambios | Ya existe en BaseDatos.sql — mapear con JPA `@Entity` |
| `Cliente` | tabla `cliente` en `customer_db` | sin cambios | Ya existe en BaseDatos.sql — mapear con JPA `@Entity` |

#### Esquema de Base de Datos (BaseDatos.sql — SCRIPT UNIFICADO customer_db)

```sql
-- ============================================================================
-- DATABASE: customer_db
-- PROPÓSITO: Almacenamiento centralizado de datos de personas y clientes
-- ============================================================================

-- Crear base de datos (si no existe)
CREATE DATABASE IF NOT EXISTS customer_db;
USE customer_db;

-- ============================================================================
-- TIPOS ENUMERADOS
-- ============================================================================

CREATE TYPE genero_enum AS ENUM ('MASCULINO', 'FEMENINO', 'OTRO');

-- ============================================================================
-- TABLA: persona
-- DESCRIPCIÓN: Datos básicos de una persona (datos compartidos con cliente)
-- ============================================================================

CREATE TABLE persona (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    genero genero_enum,
    edad INT CHECK (edad >= 0 AND edad <= 150),
    identificacion VARCHAR(50) NOT NULL UNIQUE,
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Índices para búsquedas frecuentes
    CONSTRAINT uk_persona_identificacion UNIQUE (identificacion)
);

CREATE INDEX idx_persona_identificacion ON persona(identificacion);
CREATE INDEX idx_persona_created_at ON persona(created_at);

-- ============================================================================
-- TABLA: cliente
-- DESCRIPCIÓN: Extensión de persona con credenciales y estado bancario
-- RELACIÓN: FK a persona (1:1)
-- ============================================================================

CREATE TABLE cliente (
    id BIGSERIAL PRIMARY KEY,
    persona_id BIGINT NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    estado BOOLEAN DEFAULT true,
    version BIGINT DEFAULT 0,               -- Optimistic Locking
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Restricciones
    CONSTRAINT fk_cliente_persona 
        FOREIGN KEY (persona_id) REFERENCES persona(id) ON DELETE CASCADE,
    CONSTRAINT chk_contrasena_largo 
        CHECK (LENGTH(contrasena) >= 8),
    CONSTRAINT uk_cliente_persona_id UNIQUE (persona_id)
);

CREATE INDEX idx_cliente_persona_id ON cliente(persona_id);
CREATE INDEX idx_cliente_estado ON cliente(estado);
CREATE INDEX idx_cliente_created_at ON cliente(created_at);

-- ============================================================================
-- AUDITORÍA (OPCIONAL - para futuro)
-- ============================================================================

-- Tabla de auditoría para cambios en cliente (si se requiere en el futuro)
CREATE TABLE cliente_audit (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    accion VARCHAR(50) NOT NULL,           -- CREATE, UPDATE, DELETE
    datos_previos JSONB,
    datos_nuevos JSONB,
    usuario_id VARCHAR(100),
    timestamp_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_audit_cliente 
        FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE
);

CREATE INDEX idx_cliente_audit_cliente_id ON cliente_audit(cliente_id);
CREATE INDEX idx_cliente_audit_timestamp ON cliente_audit(timestamp_cambio);

-- ============================================================================
-- DATOS DE PRUEBA (OPCIONAL - para QA)
-- ============================================================================

-- Insertar personas de prueba
INSERT INTO persona (nombre, genero, edad, identificacion, direccion, telefono)
VALUES 
    ('Jose Lema', 'MASCULINO', 35, '12345678', 'Otavalo sn y principal', '098254785'),
    ('Marianela Montalvo', 'FEMENINO', 28, '87654321', 'Salinas y principal', '098123456'),
    ('Juan Osorio', 'MASCULINO', 42, '11223344', 'Amazonas y NNUU', '098765432')
ON CONFLICT (identificacion) DO NOTHING;

-- Insertar clientes de prueba
INSERT INTO cliente (persona_id, contrasena, estado)
VALUES 
    (1, '\$2a\$10\$hashedPassword1', true),
    (2, '\$2a\$10\$hashedPassword2', true),
    (3, '\$2a\$10\$hashedPassword3', true)
ON CONFLICT (persona_id) DO NOTHING;
```

**Características del esquema:**
- **BIGSERIAL PKs**: Identificadores únicos auto-incrementados con soporte de alto volumen
- **UNIQUE constraints**: Garantiza unicidad de identificación (persona) y relación 1:1 (cliente→persona)
- **Foreign Keys con CASCADE**: Eliminación en cascada de clientes si se elimina la persona
- **Índices optimizados**: Para búsquedas por identificación, estado, y timestamps de auditoría
- **Version field**: Soporte para optimistic locking en actualizaciones concurrentes
- **Tabla de auditoría**: Preparada para futuras necesidades de trazabilidad
- **CHECK constraints**: Validación de edad (0-150) y longitud mínima de contraseña (8)

#### JPA Entities — Campos del modelo

**Entity: Persona**

| Campo | Tipo | Obligatorio | Validación | Descripción |
|-------|------|-------------|------------|-------------|
| `id` | Long | sí | auto-generado @GeneratedValue | Identificador técnico único |
| `nombre` | String | sí | max 100 chars | Nombre completo |
| `genero` | Enum (MASCULINO/FEMENINO/OTRO) | no | — | Género |
| `edad` | Integer | no | >= 0 | Edad en años |
| `identificacion` | String | sí | max 50 chars, UNIQUE | Identificación única (cédula, pasaporte, etc.) |
| `direccion` | String | no | max 255 chars | Dirección domiciliaria |
| `telefono` | String | no | max 20 chars | Teléfono de contacto |

**Entity: Cliente**

| Campo | Tipo | Obligatorio | Validación | Descripción |
|-------|------|-------------|------------|-------------|
| `id` | Long | sí | auto-generado @GeneratedValue | Identificador técnico único (PK, BIGSERIAL) |
| `persona_id` | Long | sí | FK a persona.id | Referencia a la persona asociada |
| `contrasena` | String | sí | min 8 chars, no exponer | Contraseña hash (nunca en plain text en respuesta) |
| `estado` | Boolean | sí | default true | Estado activo/inactivo |
| `created_at` | LocalDateTime | sí | @CreationTimestamp | Timestamp creación (UTC) |
| `updated_at` | LocalDateTime | sí | @UpdateTimestamp | Timestamp actualización (UTC) |

#### DTOs (Records) para API

```java
// Request para crear cliente + persona (operación integrada)
record ClienteCreateRequest(
    String nombre,          // de Persona
    String genero,          // de Persona (MASCULINO, FEMENINO, OTRO)
    Integer edad,           // de Persona
    String identificacion,  // de Persona (UNIQUE)
    String direccion,       // de Persona
    String telefono,        // de Persona
    String contrasena,      // de Cliente (min 8 chars)
    Boolean estado          // de Cliente (default true)
) {}

// Request para actualizar cliente (PUT)
record ClienteUpdateRequest(
    String nombre,
    String genero,
    Integer edad,
    String identificacion,
    String direccion,
    String telefono,
    String contrasena,
    Boolean estado
) {}

// Request para actualizar parcialmente (PATCH)
record ClientePatchRequest(
    String nombre,          // opcional
    String genero,          // opcional
    Integer edad,           // opcional
    String identificacion,  // opcional
    String direccion,       // opcional
    String telefono,        // opcional
    String contrasena,      // opcional
    Boolean estado          // opcional
) {}

// Response (SIN password, id es el identificador único del cliente)
// NOTA: @JsonProperty mapea campos Java a JSON con espacios/mayúsculas per Postman
record ClienteResponse(
    Long id,                    // ID del cliente (PK BIGSERIAL)
    @JsonProperty("Nombre")
    String nombre,
    @JsonProperty("Genero")
    String genero,
    @JsonProperty("Edad")
    Integer edad,
    @JsonProperty("Identificación")
    String identificacion,
    @JsonProperty("Dirección")
    String direccion,
    @JsonProperty("Teléfono")
    String telefono,
    @JsonProperty("Estado")
    Boolean estado,
    @JsonProperty("Fecha Creación")
    String createdAt,           // formato: dd/MM/yyyy HH:mm:ss
    @JsonProperty("Fecha Actualización")
    String updatedAt            // formato: dd/MM/yyyy HH:mm:ss
) {}
```

#### Evento RabbitMQ

```java
record ClienteCreadoEvent(
    Long clienteId,              // ID del cliente creado
    String nombre,
    String identificacion,
    Boolean estado,
    LocalDateTime createdAt
) {}
```

#### Índices / Constraints

- **persona.identificacion**: UNIQUE INDEX — búsqueda frecuente por identificación
- **cliente.persona_id**: UNIQUE INDEX + FK — garantiza 1 cliente por persona
- **cliente.id**: UNIQUE INDEX + PK — identificador único del cliente
- **cliente.created_at**: INDEX — consultas de auditoría por fecha

---

### API Endpoints

#### POST /api/clientes
- **Descripción**: Crea un nuevo cliente y su persona asociada en una sola operación
- **Request Body**:
  ```json
  {
    "nombre": "Jose Lema",
    "genero": "MASCULINO",
    "edad": 35,
    "identificacion": "12345678",
    "direccion": "Otavalo sn y principal",
    "telefono": "098254785",
    "contrasena": "SecurePass123",
    "estado": true
  }
  ```
- **Response 201**:
  ```json
  {
    "id": 1,
    "Nombre": "Jose Lema",
    "Genero": "MASCULINO",
    "Edad": 35,
    "Identificación": "12345678",
    "Dirección": "Otavalo sn y principal",
    "Teléfono": "098254785",
    "Estado": true,
    "Fecha Creación": "28/04/2026 10:30:00",
    "Fecha Actualización": "28/04/2026 10:30:00"
  }
  ```
- **Response 400**: Campo obligatorio faltante, contraseña < 8 caracteres, o datos inválidos
  ```json
  {
    "title": "Invalid Input",
    "status": 400,
    "detail": "La contraseña debe tener mínimo 8 caracteres",
    "instance": "/api/clientes"
  }
  ```
- **Response 409**: Ya existe un cliente con esa identificación
  ```json
  {
    "title": "Conflict",
    "status": 409,
    "detail": "Ya existe un cliente con identificación 12345678",
    "instance": "/api/clientes"
  }
  ```
- **Side Effect**: Emite evento `cliente.creado` a RabbitMQ (exchange: `customer.events`, routing key: `cliente.creado`)

---

#### GET /api/clientes
- **Descripción**: Lista todos los clientes del sistema
- **Response 200**:
  ```json
  [
    {
      "id": 1,
      "Nombre": "Jose Lema",
      "Genero": "MASCULINO",
      "Edad": 35,
      "Identificación": "12345678",
      "Dirección": "Otavalo sn y principal",
      "Teléfono": "098254785",
      "Estado": true,
      "Fecha Creación": "28/04/2026 10:30:00",
      "Fecha Actualización": "28/04/2026 10:30:00"
    }
  ]
  ```
- **Response 200 (vacío)**:
  ```json
  []
  ```

---

#### GET /api/clientes/{id}
- **Descripción**: Obtiene un cliente específico por su ID
- **Path Param**: `id` (Long) — ID técnico del cliente
- **Response 200**: Cliente completo (sin password)
  ```json
  {
    "id": 1,
    "Nombre": "Jose Lema",
    "Genero": "MASCULINO",
    "Edad": 35,
    "Identificación": "12345678",
    "Dirección": "Otavalo sn y principal",
    "Teléfono": "098254785",
    "Estado": true,
    "Fecha Creación": "28/04/2026 10:30:00",
    "Fecha Actualización": "28/04/2026 10:30:00"
  }
  ```
- **Response 404**: Cliente no existe
  ```json
  {
    "title": "Not Found",
    "status": 404,
    "detail": "Cliente con id 999 no encontrado",
    "instance": "/api/clientes/999"
  }
  ```

---

#### PUT /api/clientes/{id}
- **Descripción**: Actualiza todos los datos de un cliente (reemplazo completo)
- **Path Param**: `id` (Long) — ID técnico del cliente
- **Request Body**:
  ```json
  {
    "nombre": "Jose Lema",
    "genero": "MASCULINO",
    "edad": 36,
    "identificacion": "12345678",
    "direccion": "Nueva dirección",
    "telefono": "098254785",
    "contrasena": "NewSecurePass456",
    "estado": true
  }
  ```
- **Response 200**: Cliente actualizado (sin password)
  ```json
  {
    "id": 1,
    "Nombre": "Jose Lema",
    "Genero": "MASCULINO",
    "Edad": 36,
    "Identificación": "12345678",
    "Dirección": "Nueva dirección",
    "Teléfono": "098254785",
    "Estado": true,
    "Fecha Creación": "28/04/2026 10:30:00",
    "Fecha Actualización": "28/04/2026 14:45:00"
  }
  ```
- **Response 400**: Validación fallida (contraseña < 8 caracteres, etc.)
- **Response 404**: Cliente no existe
- **Response 409**: Identificación duplicada o cambio viola constraint

---

#### PATCH /api/clientes/{id}
- **Descripción**: Actualiza parcialmente un cliente (solo los campos enviados)
- **Path Param**: `id` (Long)
- **Request Body** (todos los campos opcionales):
  ```json
  {
    "estado": false
  }
  ```
- **Response 200**: Cliente actualizado con los campos preservados

---

#### DELETE /api/clientes/{id}
- **Descripción**: Elimina un cliente del sistema
- **Path Param**: `id` (Long)
- **Response 204**: Eliminado exitosamente (sin body)
- **Response 404**: Cliente no existe

---

### Arquitectura y Dependencias

**Nuevos paquetes a crear en `customerservice`:**
```
src/main/java/com/bank/customerservice/
├── domain/
│   ├── model/
│   │   ├── Persona.java              (Entity JPA)
│   │   ├── Cliente.java              (Entity JPA)
│   │   └── GeneroEnum.java           (Enum)
│   ├── ports/
│   │   ├── ClienteRepositoryPort.java
│   │   └── ClientePublisherPort.java (para RabbitMQ)
│   └── exception/
│       ├── PersonaNotFoundException.java
│       ├── ClienteNotFoundException.java
│       ├── DuplicateIdentificationException.java
│       └── InvalidPasswordException.java
│
├── application/
│   ├── dto/
│   │   ├── ClienteCreateRequest.java
│   │   ├── ClienteUpdateRequest.java
│   │   ├── ClientePatchRequest.java
│   │   ├── ClienteResponse.java
│   │   ├── PersonaCreateRequest.java
│   │   └── PersonaResponse.java
│   └── usecase/
│       └── ClienteService.java       (@Service)
│
└── infrastructure/
    ├── input/
    │   └── ClienteController.java    (@RestController)
    ├── output/
    │   ├── ClienteJpaRepository.java  (@Repository)
    │   ├── PersonaJpaRepository.java  (@Repository)
    │   ├── ClienteRepositoryAdapter.java
    │   └── ClientePublisherAdapter.java  (RabbitMQ)
    └── config/
        └── RabbitMQConfig.java       (@Configuration)
```

**Dependencias tecnológicas:**
- Spring Boot 4
- Java 21 (virtual threads en application.yaml)
- Spring Data JPA + Hibernate
- Spring AMQP (RabbitMQ)
- Spring Web (REST)
- PostgreSQL driver (JDBC)

**Integraciones externas:**
- RabbitMQ (exchange: `customer.events`, queue: `cliente.creado`)

**Punto de entrada:**
- `CustomerserviceApplication.java` — ya existe, solo verificar que Spring Boot escanee los nuevos paquetes

---

### Manejo de Excepciones: GlobalExceptionHandler (RFC 9457)

**Objetivo**: Centralizar el manejo de excepciones y retornar respuestas RFC 9457 estándar.

**Clase: `GlobalExceptionHandler`** (@ControllerAdvice)

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter dateFormatter = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Maneja PersonaNotFoundException → HTTP 404 Not Found
     */
    @ExceptionHandler(PersonaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePersonaNotFound(
            PersonaNotFoundException ex,
            WebRequest request) {
        
        ErrorResponse response = new ErrorResponse(
            "https://example.com/errors/persona-not-found",
            "Not Found",
            404,
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            LocalDateTime.now().format(dateFormatter)
        );
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja DuplicateIdentificationException → HTTP 409 Conflict
     */
    @ExceptionHandler(DuplicateIdentificationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateIdentification(
            DuplicateIdentificationException ex,
            WebRequest request) {
        
        ErrorResponse response = new ErrorResponse(
            "https://example.com/errors/duplicate-identification",
            "Conflict",
            409,
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            LocalDateTime.now().format(dateFormatter)
        );
        
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Maneja ClienteNotFoundException → HTTP 404 Not Found
     */
    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClienteNotFound(
            ClienteNotFoundException ex,
            WebRequest request) {
        
        ErrorResponse response = new ErrorResponse(
            "https://example.com/errors/cliente-not-found",
            "Not Found",
            404,
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            LocalDateTime.now().format(dateFormatter)
        );
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja InvalidPasswordException → HTTP 400 Bad Request
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(
            InvalidPasswordException ex,
            WebRequest request) {
        
        ErrorResponse response = new ErrorResponse(
            "https://example.com/errors/invalid-password",
            "Bad Request",
            400,
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            LocalDateTime.now().format(dateFormatter)
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja validaciones genéricas → HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {
        
        ErrorResponse response = new ErrorResponse(
            "https://example.com/errors/invalid-input",
            "Bad Request",
            400,
            ex.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            LocalDateTime.now().format(dateFormatter)
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
```

**Clase: `ErrorResponse`** (DTO RFC 9457)

```java
import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorResponse(
    @JsonProperty("type")
    String type,                    // URI del tipo de error
    
    @JsonProperty("title")
    String title,                   // Resumen corto del error
    
    @JsonProperty("status")
    Integer status,                 // HTTP status code
    
    @JsonProperty("detail")
    String detail,                  // Descripción detallada del error
    
    @JsonProperty("instance")
    String instance,                // URI de la solicitud específica
    
    @JsonProperty("timestamp")
    String timestamp                // Timestamp del error (formato dd/MM/yyyy HH:mm:ss)
) {}
```

**Mapeo de Excepciones a HTTP Status:**

| Excepción | HTTP Status | RFC 9457 Title | Detalle |
|-----------|------------|----------------|---------|
| `PersonaNotFoundException` | 404 | Not Found | "Persona con id {id} no encontrada" |
| `ClienteNotFoundException` | 404 | Not Found | "Cliente con id {id} no encontrado" |
| `DuplicateIdentificationException` | 409 | Conflict | "Ya existe un cliente con identificación {id}" |
| `InvalidPasswordException` | 400 | Bad Request | "La contraseña debe tener mínimo 8 caracteres" |
| `IllegalArgumentException` | 400 | Bad Request | Mensaje específico del error |

---

### Notas de Implementación

1. **Creación integrada Persona + Cliente**: El endpoint POST /api/clientes recibe datos de Persona + credenciales de Cliente en una sola solicitud. En el servicio, debe crearse ambas entidades en una ÚNICA transacción. Si alguna falla, debe rollback de ambas.

2. **Entidades JPA ya existen en BaseDatos.sql**: Las tablas `persona` y `cliente` ya están definidas. Solo mapearlas con `@Entity` y `@Table` en Java.

3. **ClienteId como identificador**: El `id` del cliente (BIGSERIAL) es automáticamente generado por la BD y es el único identificador necesario. No se requiere UUID adicional.

4. **Timestamps con formato dd/MM/yyyy HH:mm:ss**: 
   - Almacenar en BD como TIMESTAMP (UTC)
   - Convertir a `String` en DTOs response usando `DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")`
   - Campo de auditoría: `created_at` y `updated_at` con formato dd/MM/yyyy HH:mm:ss en JSON
   - Mapear con `@JsonProperty("Fecha Creación")` y `@JsonProperty("Fecha Actualización")`

5. **Contraseña**: 
   - Almacenar HASHEADA en BD usando BCrypt (min 8 caracteres)
   - Nunca exponerla en respuestas API
   - En DTOs response, omitir el campo completamente
   - Validar longitud mínima en aplicación antes de persistir

6. **RabbitMQ Messaging**: 
   - Usar `@Component` + `RabbitTemplate` para publicar eventos `ClienteCreadoEvent`
   - Configurar exchange `customer.events` y queue `cliente.creado` en `RabbitMQConfig`
   - Usar `@Async` en servicio para no bloquear la respuesta HTTP
   - Los timestamps en eventos deben usar formato ISO8601 para interoperabilidad

7. **Virtual Threads**: Habilitar en `application.yaml` con `spring.threads.virtual.enabled: true` para Java 21.

8. **RFC 9457 HTTP Error Responses**: 
   - Usar `@ControllerAdvice` con `GlobalExceptionHandler` (ver sección anterior)
   - Formatear excepciones con `ErrorResponse` que incluya: type, title, status, detail, instance, timestamp
   - Mapear excepciones específicas a status codes:
     - `PersonaNotFoundException` → 404 Not Found
     - `ClienteNotFoundException` → 404 Not Found
     - `DuplicateIdentificationException` → 409 Conflict
     - `InvalidPasswordException` → 400 Bad Request
   - Incluir timestamp en formato dd/MM/yyyy HH:mm:ss

9. **@JsonProperty Mapping**: 
   - Utilizar anotación `@JsonProperty` en todas las DTOs response para mapear campos con espacios/mayúsculas:
     - `nombre` → `@JsonProperty("Nombre")`
     - `identificacion` → `@JsonProperty("Identificación")`
     - `genero` → `@JsonProperty("Genero")`
     - `edad` → `@JsonProperty("Edad")`
     - `direccion` → `@JsonProperty("Dirección")`
     - `telefono` → `@JsonProperty("Teléfono")`
     - `estado` → `@JsonProperty("Estado")`
     - `createdAt` → `@JsonProperty("Fecha Creación")`
     - `updatedAt` → `@JsonProperty("Fecha Actualización")`
   - Esto asegura compatibilidad con clientes HTTP (ej: Postman, móviles) que esperen estos nombres exactos

10. **Constructor Injection**: NO usar `@Autowired` field injection. Siempre usar constructor injection en servicios y controladores.

11. **Transaccionalidad**: Usar `@Transactional` en métodos de `ClienteService` que modifican BD (create, update, delete).

12. **Control de Concurrencia**: Agregar `@Version` en entidades Cliente para optimistic locking si hay actualizaciones concurrentes.

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada ítem (`[x]`) al completarlo.
> **IMPORTANTE**: Los tests se generan según la matriz 3-2-1. NO crear pruebas adicionales.

### Backend

#### Implementación

**Domain**
- [ ] Crear `domain/model/Persona.java` — entidad JPA mapeada a tabla persona
- [ ] Crear `domain/model/Cliente.java` — entidad JPA mapeada a tabla cliente con @Version
- [ ] Crear `domain/model/GeneroEnum.java` — enum para valores MASCULINO/FEMENINO/OTRO
- [ ] Crear `domain/ports/ClienteRepositoryPort.java` — interfaz del puerto de salida (abstracción)
- [ ] Crear `domain/ports/ClientePublisherPort.java` — interfaz para emitir eventos RabbitMQ
- [ ] Crear excepciones en `domain/exception/`:
  - [ ] `PersonaNotFoundException.java`
  - [ ] `ClienteNotFoundException.java`
  - [ ] `DuplicateIdentificationException.java`
  - [ ] `InvalidPasswordException.java`

**Application (DTOs + Service)**
- [ ] Crear `application/dto/ClienteCreateRequest.java`
- [ ] Crear `application/dto/ClienteUpdateRequest.java`
- [ ] Crear `application/dto/ClientePatchRequest.java`
- [ ] Crear `application/dto/ClienteResponse.java` (SIN campo contrasena)
- [ ] Crear `application/dto/PersonaCreateRequest.java`
- [ ] Crear `application/dto/PersonaResponse.java`
- [ ] Crear `application/usecase/ClienteService.java` (@Service)
  - [ ] Implementar método `createCliente(ClienteCreateRequest)` → ClienteResponse + emite evento
  - [ ] Implementar método `getAllClientes()` → List<ClienteResponse>
  - [ ] Implementar método `getClienteById(Long)` → ClienteResponse
  - [ ] Implementar método `updateCliente(Long, ClienteUpdateRequest)` → ClienteResponse
  - [ ] Implementar método `patchCliente(Long, ClientePatchRequest)` → ClienteResponse
  - [ ] Implementar método `deleteCliente(Long)` → void
  - [ ] Validaciones: identificación única, contraseña min 8 chars, persona existe

**Infrastructure — Input (REST)**
- [ ] Crear `infrastructure/input/ClienteController.java` (@RestController)
- [ ] POST /api/clientes — mapea a ClienteService.createCliente()
- [ ] GET /api/clientes — mapea a ClienteService.getAllClientes()
- [ ] GET /api/clientes/{id} — mapea a ClienteService.getClienteById()
- [ ] PUT /api/clientes/{id} — mapea a ClienteService.updateCliente()
- [ ] PATCH /api/clientes/{id} — mapea a ClienteService.patchCliente()
- [ ] DELETE /api/clientes/{id} — mapea a ClienteService.deleteCliente()
  - [ ] Usar constructor injection para servicios
  - [ ] Respuestas HTTP con códigos: 201, 200, 204, 400, 404, 409
- [ ] Crear `infrastructure/input/GlobalExceptionHandler.java` (@ControllerAdvice)
  - [ ] Implementar @ExceptionHandler para PersonaNotFoundException (404)
  - [ ] Implementar @ExceptionHandler para DuplicateIdentificationException (409)
  - [ ] Implementar @ExceptionHandler para ClienteNotFoundException (404)
  - [ ] Implementar @ExceptionHandler para InvalidPasswordException (400)
  - [ ] Implementar @ExceptionHandler para IllegalArgumentException (400)
  - [ ] Formatear respuestas con ErrorResponse según RFC 9457
  - [ ] Incluir timestamp en formato dd/MM/yyyy HH:mm:ss
- [ ] Crear `infrastructure/input/ErrorResponse.java` (DTO)
  - [ ] Campos: type, title, status, detail, instance, timestamp
  - [ ] Usar @JsonProperty para serialización correcta

**Infrastructure — Output (Persistence)**
- [ ] Crear `infrastructure/output/ClienteJpaRepository.java` (@Repository) extends JpaRepository<Cliente, Long>
  - [ ] Método `Optional<Cliente> findById(Long id)`
  - [ ] Método `Optional<Cliente> findByPersonaId(Long personaId)`
- [ ] Crear `infrastructure/output/PersonaJpaRepository.java` (@Repository) extends JpaRepository<Persona, Long>
  - [ ] Método `Optional<Persona> findByIdentificacion(String identificacion)`
- [ ] Crear `infrastructure/output/ClienteRepositoryAdapter.java` — implementa ClienteRepositoryPort
  - [ ] Inyectar ClienteJpaRepository y PersonaJpaRepository
  - [ ] Implementar métodos save, findById, findAll, update, delete
  - [ ] Validar duplicado de identificación en PersonaJpaRepository antes de crear

**Infrastructure — Output (RabbitMQ)**
- [ ] Crear `infrastructure/output/ClientePublisherAdapter.java` — implementa ClientePublisherPort
  - [ ] Inyectar `RabbitTemplate`
  - [ ] Método `publishClienteCreadoEvent(ClienteCreadoEvent)` — envia a exchange `customer.events`
  - [ ] Usar `@Async` para no bloquear respuesta HTTP
- [ ] Crear `infrastructure/config/RabbitMQConfig.java` (@Configuration)
  - [ ] Definir Exchange: `customer.events` (topic)
  - [ ] Definir Queue: `cliente.creado`
  - [ ] Definir Binding entre queue y exchange con routing key `cliente.creado`
  - [ ] Serialización JSON de eventos con `Jackson`

**Configuration**
- [ ] Actualizar `application.yaml`:
  - [ ] Agregar RabbitMQ host, port, username, password (desde env vars)
  - [ ] Habilitar virtual threads: `spring.threads.virtual.enabled: true`
  - [ ] Configurar schema: `spring.datasource.hikari.schema=customer_db`
  - [ ] JPA: `spring.jpa.hibernate.ddl-auto: validate` (NO update)

#### Tests Backend (Matriz 2-2-1 — Pragmática)

**Regla: POR CADA MÉTODO = 2 tests | POR CADA ENDPOINT = 2 tests | POR CADA ADAPTER = 1 test**

> Matriz simplificada: cubre happy path + error, sin redundancia. Objetivo: cobertura ≥ 80% en lógica crítica.

**ClienteService Tests** (2 tests × 6 métodos = 12 tests)
- [ ] `createCliente_success()` — happy path, genera id, emite evento
- [ ] `createCliente_throwsException()` — error: contraseña inválida O identificación duplicada OR persona no existe
- [ ] `getAllClientes_returnsListOfClientes()` — happy path con datos
- [ ] `getAllClientes_returnsEmptyList()` — edge: sin clientes
- [ ] `getClienteById_success()` — happy path
- [ ] `getClienteById_throws_ClienteNotFoundException()` — error: no existe
- [ ] `updateCliente_success()` — happy path
- [ ] `updateCliente_throws_ClienteNotFoundException()` — error: no existe
- [ ] `patchCliente_updatesProvidedFieldsOnly()` — happy path: PATCH parcial
- [ ] `patchCliente_throwsException()` — error: validación falla
- [ ] `deleteCliente_success()` — happy path
- [ ] `deleteCliente_throws_ClienteNotFoundException()` — error: no existe

**ClienteController Tests** (2 tests × 6 endpoints = 12 tests)
- [ ] `postClientes_returns201()` — happy path
- [ ] `postClientes_returns400or409()` — error: validación falla O identificación duplicada
- [ ] `getClientes_returns200()` — happy path (vacío o con datos)
- [ ] `getClientes_omitsPassword()` — verificar que respuesta no expone contraseña
- [ ] `getClientesById_returns200()` — happy path
- [ ] `getClientesById_returns404()` — error: no existe
- [ ] `putClientes_returns200()` — happy path
- [ ] `putClientes_returns400or404()` — error: validación falla O no existe
- [ ] `patchClientes_returns200()` — happy path
- [ ] `patchClientes_returns400or404()` — error: validación falla O no existe
- [ ] `deleteClientes_returns204()` — happy path
- [ ] `deleteClientes_returns404()` — error: no existe

**ClienteRepositoryAdapter Tests** (1-2 tests × 4 métodos = 5-6 tests mínimo)
- [ ] `save_insertsClienteWithAutoId()` — save exitoso
- [ ] `save_throwsDuplicateIdentificationException()` — error: identificación duplicada
- [ ] `findById_returnsOptional()` — find exitoso (con datos)
- [ ] `findAll_returnsListOrEmpty()` — findAll con datos y vacío
- [ ] `delete_removesEntity()` — delete exitoso

**ClientePublisherAdapter Tests** (1 test mínimo)
- [ ] `publishClienteCreadoEvent_sendsToRabbitMQ()` — evento publicado correctamente

---

### QA

#### Test Planning
- [ ] Ejecutar skill `/gherkin-case-generator` con esta spec
  - [ ] Genera escenarios CRITERIO-1.1, 1.2, 1.3
  - [ ] Genera escenarios CRITERIO-2.1, 2.2, 2.3
  - [ ] Genera escenarios CRITERIO-3.1, 3.2
  - [ ] Genera escenarios CRITERIO-4.1, 4.2
  - [ ] Genera escenarios CRITERIO-5.1, 5.2
  - [ ] Genera escenarios CRITERIO-6.1, 6.2
  - [ ] Genera escenarios CRITERIO-7.1, 7.2
  - [ ] Genera escenarios CRITERIO-8.1, 8.2, 8.3

#### Risk Assessment
- [ ] Ejecutar skill `/risk-identifier` con esta spec
  - [ ] Clasificar riesgos en Alto/Medio/Bajo (regla ASD)
  - [ ] Identificar riesgos de seguridad: password exposure, FK violations
  - [ ] Identificar riesgos de integridad: identificación única, transaccionalidad
  - [ ] Identificar riesgos de integración: RabbitMQ connectivity

#### Test Coverage Validation
- [ ] Revisar cobertura de tests contra CRITERIO-1.1 a 8.3
- [ ] Validar que todas las reglas de negocio están cubiertas por tests
- [ ] Validar que 3-2-1 matriz se cumple en backend tests

#### Integration Testing
- [ ] Validar flujo end-to-end: POST cliente → evento RabbitMQ → consumidor
- [ ] Validar cascada de FK: eliminar cliente → validar foreign key
- [ ] Validar concurrencia optimista con @Version

---

### Cambios en BaseDatos.sql (REVISIÓN)

> ⚠️ **VERIFICAR**: Las tablas `persona` y `cliente` YA existen en `BaseDatos.sql` con los campos requeridos. 
> **ÚNICO CAMBIO NECESARIO** (si aplica): Agregar campos `created_at` y `updated_at` a tabla `cliente` si no existen:

```sql
-- SI NO EXISTEN, AGREGAR:
ALTER TABLE cliente ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE cliente ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
```

---

### Monitoreo y Observabilidad (Futuro)

> Notas para agentes QA y Ops:
- Loguear creación de clientes con log level INFO
- Loguear fallos de eventos RabbitMQ con log level WARN
- Métricas: cantidad de clientes creados/actualizados/eliminados (Prometheus)
- Traces distribuidos: usar Spring Cloud Sleuth para rastrear eventos RabbitMQ

---

## Aprobación

- [ ] Spec revisada por Arquitectura
- [ ] Spec revisada por Backend Lead
- [ ] Spec revisada por QA Lead
- **Status actual**: `DRAFT` → cambiar a `APPROVED` una vez revisado

---

**Generado por**: spec-generator  
**Fecha**: 2026-04-28  
**Versión**: 1.0
