---
id: SPEC-004
status: DRAFT
feature: balance-validation
created: 2026-04-29
updated: 2026-04-29
author: spec-generator
version: "1.0"
related-specs: []
---

# Spec: Validación Estricta de Saldo Disponible

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción
El sistema valida que una cuenta tenga saldo suficiente antes de procesar un retiro. La validación ocurre en la capa de dominio, no en el controller ni en el repositorio. Para evitar sobregiros en operaciones concurrentes, se implementa bloqueo optimista usando el campo `@Version` en la entidad `Cuenta`.

### Requerimiento de Negocio
Como sistema de seguridad, quiero validar fondos antes de un retiro, para evitar sobregiros.

### Historias de Usuario

#### HU-001: Validación de Saldo Disponible en Retiros

```
Como:        Sistema de Seguridad
Quiero:      Validar que el saldo disponible sea suficiente antes de procesar un retiro
Para:        Evitar sobregiros y mantener la integridad contable de la cuenta

Prioridad:   Alta
Estimación:  M
Dependencias: Ninguna (Account Model existente)
Capa:        Backend — Domain + Application
```

#### Criterios de Aceptación — HU-001

**Criterio 1.1: Validar saldo suficiente para retiro**
```gherkin
Dado que:   una cuenta tiene saldo disponible de 500.00
Cuando:     realizo un retiro de 300.00
Entonces:   la operación es permitida
Y           el nuevo saldo calculado es (500.00 + (-300.00)) = 200.00 >= 0
```

**Criterio 1.2: Validar saldo insuficiente para retiro**
```gherkin
Dado que:   una cuenta tiene saldo disponible de 200.00
Cuando:     realizo un retiro de 300.00
Entonces:   la operación es rechazada
Y           el cálculo es (200.00 + (-300.00)) = -100.00 < 0
Y           se lanza una excepción personalizada
```

**Criterio 1.3: Mensaje de error estandarizado**
```gherkin
Dado que:   una cuenta tiene saldo insuficiente
Cuando:     intento realizar un retiro que excede el saldo disponible
Entonces:   el sistema lanza InsufficientBalanceException
Y           el mensaje de error es "Saldo no disponible"
Y           la respuesta HTTP es 409 (Conflict)
```

**Criterio 1.4: Validación en dominio, no en controller**
```gherkin
Dado que:   una solicitud de retiro llega al controller
Cuando:     el controller llama al servicio de dominio (use case)
Entonces:   la validación de saldo se ejecuta EN la capa domain (método de Cuenta)
Y           el controller NO contiene lógica de validación de saldo
```

**Criterio 1.5: Validación en dominio, no en repositorio**
```gherkin
Dado que:   el dominio (Cuenta) decide rechazar una transacción por saldo insuficiente
Cuando:     la orden llega al repositorio/adapter
Entonces:   el repositorio NO valida el saldo
Y           solo ejecuta la operación de persistencia
```

**Criterio 1.6: Bloqueo optimista para concurrencia (Scenario 1)**
```gherkin
Dado que:   dos usuarios (Thread-1 y Thread-2) intentan retirar simultáneamente de la misma cuenta
Cuando:     ambos retiros son procesados en paralelo
Entonces:   el primer retiro se completa exitosamente
Y           el segundo retiro falla con OptimisticLockException
Y           la cuenta NO queda en saldo negativo
```

**Criterio 1.7: Campo @Version en entidad Cuenta**
```gherkin
Dado que:   una cuenta existe en la base de datos
Cuando:     consulto la entidad Cuenta mapeada por JPA
Entonces:   la entidad tiene un campo anotado con @Version
Y           el tipo del campo es Integer
Y           el valor inicial es 0 o 1 (según la estrategia)
```

**Criterio 1.8: Actualización exitosa de versión**
```gherkin
Dado que:   una cuenta tiene version = 1
Cuando:     realizo un retiro válido (saldo >= monto)
Entonces:   el movimiento se crea exitosamente
Y           la versión de la cuenta se actualiza a 2
Y           el cambio es visible en la BD (vía SELECT)
```

**Criterio 1.9: Actualización fallida por versión obsoleta**
```gherkin
Dado que:   una cuenta tiene version = 1 (en memoria)
Cuando:     otro proceso/transacción ya actualizó la cuenta a version = 2
Entonces:   al intentar actualizar la cuenta con version = 1
Y           el sistema lanza OptimisticLockException
Y           el movimiento NO es registrado
Y           la transacción se revierte completamente
```

### Reglas de Negocio

1. **RN-001**: El saldo disponible de una cuenta nunca puede ser negativo tras un retiro.
2. **RN-002**: La validación de saldo ocurre en la capa de dominio (`Cuenta.retirar()` o equivalente), antes de persistir.
3. **RN-003**: El repositorio NO valida saldo, solo ejecuta persistencia si el dominio lo autoriza.
4. **RN-004**: Cada actualización de `Cuenta` incrementa el campo `@Version` automáticamente (JPA Optimistic Locking).
5. **RN-005**: Las excepciones de dominio (`InsufficientBalanceException`, `OptimisticLockException`) se mapean a HTTP 409.
6. **RN-006**: El controller responde con RFC 9457 + envelope JSON en caso de error.

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades Afectadas

| Entidad | Almacén | Cambios | Descripción |
|---------|---------|---------|-------------|
| `Cuenta` | tabla `cuenta` | Modificada — agregar método de retiro | Entidad de dominio — modelo principal de negocio |
| `Movimiento` | tabla `movimiento` | Nueva — se crea con cada retiro/depósito | Registro transaccional del cambio de saldo |

#### Campos del Modelo — Cuenta

| Campo | Tipo | Obligatorio | Validación | Descripción |
|-------|------|-------------|------------|-------------|
| `id` | Long | sí | auto-generado | Identificador único |
| `numeroCuenta` | String | sí | única, máx 20 chars | Número de cuenta (IBAN-like) |
| `tipoCuenta` | String | sí | enum: Ahorros, Corriente | Tipo de cuenta |
| `saldoInicial` | BigDecimal | sí | >= 0 | Saldo inicial (de apertura) |
| `saldoDisponible` | BigDecimal | sí | >= 0 | Saldo actual disponible para retiros |
| `version` | Integer | sí | auto-incrementado | Campo para Optimistic Locking (@Version) |
| `estado` | Boolean | sí | default: true | true = activa, false = inactiva |
| `clienteId` | Long | sí | referencia a cliente | ID del propietario |
| `created_at` | Timestamp | sí | auto-generado (UTC) | Timestamp de creación |
| `updated_at` | Timestamp | sí | auto-actualizado (UTC) | Timestamp de última actualización |

#### Campos del Modelo — Movimiento

| Campo | Tipo | Obligatorio | Validación | Descripción |
|-------|------|-------------|------------|-------------|
| `id` | Long | sí | auto-generado | Identificador único |
| `tipoMovimiento` | String | sí | enum: DEPOSITO, RETIRO | Tipo de transacción |
| `valor` | BigDecimal | sí | > 0 | Monto de la operación (siempre positivo) |
| `saldo` | BigDecimal | sí | >= 0 | Saldo resultante post-movimiento |
| `fecha` | Timestamp | sí | auto-generado (UTC) | Timestamp de la transacción |
| `cuentaId` | Long | sí | FK → cuenta.id | Referencia a la cuenta afectada |

#### Cambios en BaseDatos.sql

**NOTA**: La tabla `cuenta` ya tiene el campo `version`. No hay cambios requeridos en el schema.

```sql
-- Ya existe en BaseDatos.sql:
-- CREATE TABLE cuenta (
--     ...
--     version INTEGER DEFAULT 0,
--     ...
-- );
```

#### Índices / Constraints

- Indice existente en `cuenta(numero_cuenta)` — para búsquedas rápidas por número.
- FK en `movimiento(cuenta_id)` → `cuenta(id)` — asegurar referencial.

### API Endpoints

#### POST /api/v1/accounts/{accountId}/withdraw
**Descripción**: Realiza un retiro de la cuenta con validación de saldo.

**Auth requerida**: Sí (Bearer Token)

**Path Parameters**:
- `accountId` (Long) — ID de la cuenta

**Request Body**:
```json
{
  "amount": 300.00,
  "description": "Retiro en cajero automático" // opcional
}
```

**Response 200 — Éxito**:
```json
{
  "data": {
    "movimiento": {
      "id": 1001,
      "tipoMovimiento": "RETIRO",
      "valor": 300.00,
      "saldo": 200.00,
      "fecha": "2026-04-29T14:30:00Z"
    },
    "cuenta": {
      "id": 5,
      "numeroCuenta": "ACC-12345",
      "saldoDisponible": 200.00,
      "version": 2
    }
  },
  "timestamp": "2026-04-29T14:30:00Z"
}
```

**Response 400 — Bad Request**:
```json
{
  "type": "https://api.example.com/errors/validation-error",
  "title": "Bad Request",
  "status": 400,
  "detail": "El monto del retiro debe ser mayor a 0",
  "instance": "/api/v1/accounts/5/withdraw"
}
```

**Response 404 — Not Found**:
```json
{
  "type": "https://api.example.com/errors/not-found",
  "title": "Not Found",
  "status": 404,
  "detail": "Cuenta con ID 999 no existe",
  "instance": "/api/v1/accounts/999/withdraw"
}
```

**Response 409 — Conflict (Saldo Insuficiente)**:
```json
{
  "type": "https://api.example.com/errors/insufficient-balance",
  "title": "Conflict — Insufficient Balance",
  "status": 409,
  "detail": "Saldo no disponible. Saldo disponible: 200.00, monto solicitado: 300.00",
  "instance": "/api/v1/accounts/5/withdraw"
}
```

**Response 409 — Conflict (Optimistic Lock)**:
```json
{
  "type": "https://api.example.com/errors/optimistic-lock",
  "title": "Conflict — Optimistic Lock Exception",
  "status": 409,
  "detail": "La cuenta fue modificada por otro proceso. Por favor, intente nuevamente.",
  "instance": "/api/v1/accounts/5/withdraw"
}
```

#### GET /api/v1/accounts/{accountId}/movements
**Descripción**: Lista todos los movimientos (retiros/depósitos) de una cuenta.

**Auth requerida**: Sí

**Path Parameters**:
- `accountId` (Long) — ID de la cuenta

**Query Parameters**:
- `page` (int, default: 0) — número de página
- `size` (int, default: 10) — cantidad de registros por página

**Response 200**:
```json
{
  "data": [
    {
      "id": 1001,
      "tipoMovimiento": "RETIRO",
      "valor": 300.00,
      "saldo": 200.00,
      "fecha": "2026-04-29T14:30:00Z"
    },
    {
      "id": 1000,
      "tipoMovimiento": "DEPOSITO",
      "valor": 500.00,
      "saldo": 500.00,
      "fecha": "2026-04-29T10:00:00Z"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1
  },
  "timestamp": "2026-04-29T14:35:00Z"
}
```

### Arquitectura y Dependencias

**Paquetes existentes — SIN cambios**:
- `domain.model` — Entidad pura `Cuenta`
- `domain.exception` — `InsufficientBalanceException`, `OptimisticLockException`
- `application.service` — `CuentaService` (casos de uso)
- `application.dto` — Request/Response DTOs
- `infrastructure.input.rest` — `CuentaController`
- `infrastructure.output` — `CuentaRepository` (JPA Adapter)

**Nuevos métodos**:
- `Cuenta.retirar(BigDecimal monto)` — método de dominio que valida y aplica la lógica de retiro
- `CuentaService.withdraw(Long cuentaId, BigDecimal monto)` — caso de uso orquestador
- `CuentaController.withdraw(Long cuentaId, WithdrawRequest request)` — endpoint REST

**Servicios externos**: Ninguno.

**Impacto en punto de entrada**: Registrar el endpoint `/api/v1/accounts/{accountId}/withdraw` en el router.

### Notas de Implementación

- **Optimistic Locking**: JPA se encarga automáticamente de incrementar `@Version` e lanzar `OptimisticLockException` si hay conflicto. No es necesario implementar manualmente.
- **Transacción**: El método `CuentaService.withdraw()` debe estar decorado con `@Transactional` para garantizar atomicidad (crear Movimiento + actualizar Cuenta juntos).
- **Excepciones personalizadas**: `InsufficientBalanceException` (extends `DomainException`) debe ser lanzada desde el método `Cuenta.retirar()`, NO desde el service ni el controller.
- **Mapeo HTTP**: El `GlobalExceptionHandler` o `@RestControllerAdvice` mapeará `InsufficientBalanceException` a HTTP 409 (Conflict) + RFC 9457.
- **RFC 9457**: Todas las respuestas de error deben incluir `type`, `title`, `status`, `detail`, `instance`.

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada ítem (`[x]`) al completarlo.
> **IMPORTANTE**: Los tests se generan según la matriz 3-2-1. NO crear pruebas adicionales.

### Backend

#### Implementación

- [ ] Crear excepción personalizada `InsufficientBalanceException extends DomainException` en `domain/exception/`
- [ ] Implementar método `Cuenta.retirar(BigDecimal monto)` en `domain/model/Cuenta.java` — valida saldo + aplica cambio
- [ ] Implementar DTO `WithdrawRequest` en `application/dto/` — campos: `amount`, `description` (opcional)
- [ ] Implementar DTO `MovemientoResponse` en `application/dto/` — campos: id, tipoMovimiento, valor, saldo, fecha
- [ ] Implementar caso de uso `CuentaService.withdraw(Long cuentaId, BigDecimal monto)` en `application/service/CuentaService.java`
- [ ] Implementar endpoint `@PostMapping("/api/v1/accounts/{accountId}/withdraw")` en `infrastructure/input/rest/CuentaController.java`
- [ ] Implementar endpoint `@GetMapping("/api/v1/accounts/{accountId}/movements")` en `infrastructure/input/rest/CuentaController.java`
- [ ] Crear modelo JPA `MovimientoEntity` en `infrastructure/output/` — mapear tabla `movimiento`
- [ ] Crear `MovimientoRepository extends JpaRepository<MovimientoEntity, Long>` en `infrastructure/output/`
- [ ] Mapear `CuentaEntity` a JPA con anotación `@Version` en el campo `version`
- [ ] Implementar `CuentaRepositoryAdapter` — métodos `save()`, `findById()`, `findAllMovimientos(cuentaId)`
- [ ] Configurar `@Transactional` en `CuentaService.withdraw()` — garantizar atomicidad
- [ ] Registrar el handler de `InsufficientBalanceException` y `OptimisticLockException` en `GlobalExceptionHandler`

#### Tests Backend (Matriz 3-2-1)

**CuentaServiceTests.java** — 3 tests por método

- [ ] `testWithdraw_success_validBalance` — happy path: retiro exitoso con saldo suficiente
- [ ] `testWithdraw_throwsInsufficientBalanceException_when_amountExceedsBalance` — error path: saldo insuficiente
- [ ] `testWithdraw_throwsValidationException_when_amountIsZeroOrNegative` — edge case: validación de monto

**CuentaControllerTests.java** — 3 tests por endpoint

- [ ] `testPostWithdraw_returns201_when_validRequest` — POST `/api/v1/accounts/{id}/withdraw` — 201
- [ ] `testPostWithdraw_returns409_when_insufficientBalance` — POST con saldo insuficiente — 409
- [ ] `testPostWithdraw_returns400_when_invalidAmount` — POST con monto inválido — 400
- [ ] `testGetMovements_returns200_with_movements` — GET `/api/v1/accounts/{id}/movements` — 200
- [ ] `testGetMovements_returns404_when_accountNotFound` — GET con account inexistente — 404
- [ ] `testGetMovements_returns200_with_emptyList_when_noMovements` — GET sin movimientos — 200

**CuentaRepositoryAdapterTests.java** — 2 tests por método

- [ ] `testSave_persists_and_increments_version` — save() incrementa @Version
- [ ] `testSave_throws_OptimisticLockException_when_versionConflict` — save() con version obsoleta
- [ ] `testFindById_returns_entity` — findById() retorna entidad
- [ ] `testFindById_returns_empty_when_notFound` — findById() con ID inexistente
- [ ] `testFindAllMovimientos_returns_list` — findAllMovimientos(cuentaId) retorna lista
- [ ] `testFindAllMovimientos_returns_empty_when_noMovements` — findAllMovimientos() sin movimientos

### QA

- [ ] Ejecutar skill `/gherkin-case-generator` → criterios CRITERIO-1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9
- [ ] Ejecutar skill `/risk-identifier` → clasificación ASD (Alto/Medio/Bajo) de riesgos de concurrencia
- [ ] Ejecutar skill `/performance-analyzer` → plan de load testing con k6 para transacciones concurrentes
- [ ] Revisar cobertura de tests contra criterios de aceptación
- [ ] Validar que todas las reglas de negocio están cubiertas
- [ ] Validar que el bloqueo optimista funciona bajo concurrencia (test manual con threads)
- [ ] Actualizar estado spec: `status: IMPLEMENTED` (cuando se complete)

---

## Aprobación

Para que esta spec pase a estado `APPROVED`, debe cumplir:

- [ ] Requerimiento validado con el usuario (DoR)
- [ ] Criterios Gherkin alineados con el dominio
- [ ] Endpoints documentados con ejemplos HTTP claros
- [ ] Arquitectura hexagonal respetada
- [ ] Excepciones de dominio identificadas
- [ ] Plan de tests matriz 3-2-1 listo

