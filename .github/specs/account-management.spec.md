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
Gestión completa del ciclo de vida de las cuentas bancarias de los clientes. Permite crear, listar, actualizar y eliminar cuentas, asegurando validaciones de negocio como la unicidad del número de cuenta y la integridad de los tipos de cuenta permitidos.

### Requerimiento de Negocio
Como administrador del sistema, quiero gestionar las cuentas de los clientes, para habilitar sus productos financieros.

### Historias de Usuario

#### HU-02: Administración de Cuentas Bancarias

```
Como:        Administrador del sistema
Quiero:      gestionar las cuentas de los clientes
Para:        habilitar sus productos financieros

Prioridad:   Alta
Estimación:  M
Dependencias: HU-01 (Gestión de Clientes)
Capa:        Backend
```

#### Criterios de Aceptación — HU-02

**Happy Path**
```gherkin
CRITERIO-2.1: Crear una cuenta exitosamente
  Dado que:  el sistema requiere una nueva cuenta y el cliente_id "1" existe
  Cuando:    proporciono número de cuenta, tipo de cuenta "Ahorros", saldo inicial y estado
  Entonces:  la entidad cuenta se crea con todos los campos especificados
  Y:         el sistema genera un identificador único y se asocia al cliente "1"

CRITERIO-2.2: Listar todas las cuentas
  Dado que:  existen cuentas en el sistema
  Cuando:    envío una solicitud GET a /cuentas
  Entonces:  el sistema retorna una lista de todas las cuentas con sus datos básicos

CRITERIO-2.3: Actualizar una cuenta existente
  Dado que:  una cuenta con id "1" existe
  Cuando:    envío una solicitud PUT o PATCH a /cuentas/1 con datos actualizados
  Entonces:  el sistema actualiza la información (salvo el número de cuenta)
```

**Error Path**
```gherkin
CRITERIO-2.4: Evitar número de cuenta duplicado
  Dado que:  una cuenta con número "123456" ya existe
  Cuando:    intento crear otra cuenta con el mismo número
  Entonces:  el sistema retorna un error de validación (409 Conflict) indicando duplicidad

CRITERIO-2.5: Tipos de cuenta inválidos
  Dado que:  el sistema solo acepta "Ahorros" o "Corriente"
  Cuando:    intento crear una cuenta con tipo "Invalido"
  Entonces:  el sistema retorna un error de validación (400 Bad Request)

CRITERIO-2.6: Evitar edición de número de cuenta
  Dado que:  una cuenta con número "123456" existe
  Cuando:    intento actualizar el número de cuenta a "789012" vía PUT/PATCH
  Entonces:  el sistema restringe la modificación y el número original se preserva (o retorna 400)
```

### Reglas de Negocio
1. **Unicidad:** El número de cuenta debe ser único en toda la base de datos.
2. **Inmutabilidad:** El número de cuenta no se puede modificar una vez creado.
3. **Tipos de Cuenta Permitidos:** Solo se permiten cuentas de tipo `Ahorros` y `Corriente`.
4. **Relación Cliente:** Toda cuenta debe estar ineludiblemente asociada a un `cliente_id`.
5. **Formato RFC 9457:** Los errores deben seguir la convención Problem Details for HTTP APIs y enviar metadata de timestamps.

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades afectadas
| Entidad | Almacén | Cambios | Descripción |
|---------|---------|---------|-------------|
| `Cuenta` | tabla `cuenta` en `banking_db` | nueva | Información de la cuenta bancaria del cliente |

#### Campos del modelo (Mapeo BD)
| Campo | Tipo | Obligatorio | Validación | Descripción |
|-------|------|-------------|------------|-------------|
| `id` | BIGSERIAL | sí | auto-generado | Identificador único interno |
| `numero_cuenta` | VARCHAR(20) | sí | Unico | Número público de cuenta |
| `tipo_cuenta` | VARCHAR(20) | sí | In Enum (Ahorros, Corriente) | Tipo de producto financiero |
| `saldo_inicial` | DECIMAL(15,2)| sí | >= 0 | Monto inicial al abrir la cuenta |
| `saldo_disponible`| DECIMAL(15,2)| sí | calculado | Saldo real transaccional |
| `estado` | BOOLEAN | sí | default true | Cuenta activa o inactiva |
| `cliente_id` | BIGINT | sí | FK a Cliente (RabbitMQ ref) | ID del cliente propietario |

*(Nota: Aunque baseDatos.sql no usa createdAt/updatedAt directos en cuenta, se adaptarán según necesidad arquitectónica de Spring Boot si se añaden luego o se manejan a nivel de DTO).*

#### Índices / Constraints
- `UNIQUE(numero_cuenta)`: Para asegurar unicidad a nivel de BD.
- `FK_cuenta_cliente`: (Lógica/Referencial) Asocia la cuenta con un identificador de cliente válido.

### API Endpoints

#### POST /cuentas
- **Descripción**: Crea una nueva cuenta bancaria
- **Request Body**:
  ```json
  {
    "numeroCuenta": "string",
    "tipoCuenta": "Ahorros | Corriente",
    "saldoInicial": "decimal",
    "estado": "boolean",
    "clienteId": "number"
  }
  ```
- **Response 201**: Datos de la cuenta creada, incluyendo id generado y saldo disponible inicial = saldo inicial, wrap en envelope json (data, timestamp).
- **Response 400**: Validación de campos (tipo_cuenta inválido).
- **Response 409**: Conflict - Número de cuenta ya en uso.

#### GET /cuentas
- **Descripción**: Lista todas las cuentas
- **Response 200**:
  ```json
  {
    "data": [{ "id": 1, "numeroCuenta": "...", "tipoCuenta": "Ahorros", "saldoDisponible": 1000 }],
    "timestamp": "..."
  }
  ```

#### GET /cuentas/{id}
- **Descripción**: Obtiene una cuenta por su id
- **Response 200**: cuenta completa en envelope
- **Response 404**: Error RFC 9457 Not Found

#### PUT /cuentas/{id}
- **Descripción**: Actualiza una cuenta existente
- **Request Body**: tipoCuenta, estado (ignora numeroCuenta si se envía)
- **Response 200**: cuenta actualizada
- **Response 404**: Not found

#### PATCH /cuentas/{id}
- **Descripción**: Actualiza parcialmente una cuenta (ej. cambiar solo estado)
- **Request Body**: partial de campos
- **Response 200**: cuenta actualizada

#### DELETE /cuentas/{id}
- **Descripción**: Elimina o inactiva una cuenta lógicamente
- **Response 204/:200**: Confirmación de eliminación.

### Arquitectura y Dependencias
- Paquetes nuevos requeridos: Ninguno extra para el CRUD general.
- Servicios externos: Recepción de RabbitMQ para confirmación asíncrona de `cliente_id` (HU-006 - relacionado).
- Backend a seguir: Arquitectura Hexagonal PURA en `bankingservice` (Domain -> Application -> Infra).

---

## 3. LISTA DE TAREAS

### Backend

#### Implementación
- [ ] Crear DTOs: `CuentaCreateRequest`, `CuentaUpdateRequest`, `CuentaResponse` en `application/dto/`
- [ ] Listar dominio: Entidad `Cuenta` en `domain/model/`, Excepciones en `domain/exception/`
- [ ] Interfaces de Puertos: `CuentaUseCase` en `domain/ports/in/` y `CuentaRepository` en `domain/ports/out/`
- [ ] Mapear Entidad JPA `CuentaEntity` a esquema `banking_db` en `infrastructure/output/persistence/`
- [ ] Implementar adaptador `CuentaRepositoryAdapter` en `infrastructure/output/persistence/`
- [ ] Implementar Servicio `CuentaService` (`@Service`) en `application/service/` implementando `CuentaUseCase`
- [ ] Implementar Controller `CuentaController` en `infrastructure/input/rest/` para CRUD `/cuentas`
- [ ] Configurar inyección en `infrastructure/config/` y Excepciones para RFC 9457

#### Tests Backend (Matriz 3-2-1)
- [ ] `CuentaUseCaseTests.java`: 3 tests por método (create, getById, update, delete)
- [ ] `CuentaControllerTests.java`: 3 tests por endpoint
- [ ] `CuentaRepositoryAdapterTests.java`: 2 tests por método (save, findById)

### QA
- [ ] Ejecutar skill `/gherkin-case-generator`
- [ ] Ejecutar skill `/risk-identifier`
- [ ] Revisar cobertura de tests contra criterios de aceptación
- [ ] Validar integridad transaccional DB
- [ ] Actualizar estado spec: `status: IMPLEMENTED`
