---
id: SPEC-003
status: DRAFT
feature: movement-management
created: 2026-04-29
updated: 2026-04-30
author: spec-generator
version: "1.0"
related-specs: ["SPEC-001"]
---

# Spec: Registro de Movimientos y Saldo Real

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción
El sistema debe registrar todas las transacciones (depósitos y retiros) en las cuentas bancarias y mantener un historial completo de movimientos. Cada movimiento incluye fecha automática, tipo (depósito o retiro), valor y el saldo resultante de la operación. Para optimizar consultas y validaciones, el saldo disponible se persiste en la tabla `cuenta` y se actualiza transaccionalmente junto con cada movimiento.

### 2. ANÁLISIS DE LA ARQUITECTURA DE DATOS

La persistencia del saldo disponible en `cuenta` reduce la complejidad operativa y evita depender del último movimiento para reconstruir el estado actual. Esta decisión mejora el rendimiento de lectura y simplifica la validación de saldo al momento de registrar un nuevo movimiento.

Como la operación sigue siendo concurrente, el servicio debe proteger la actualización del saldo de la cuenta dentro de una transacción única. La estrategia recomendada es usar control de concurrencia sobre la entidad `Cuenta` mediante `@Version` y actualización atómica del saldo disponible durante el proceso de registro. No es necesario consultar el último movimiento para calcular el saldo anterior, porque la fuente de verdad operativa es `cuenta.saldo_disponible`.

El historial de `movimiento` se conserva como bitácora auditable, mientras que `cuenta.saldo_disponible` representa el saldo vigente para operaciones de negocio y validaciones inmediatas.

### Requerimiento de Negocio
Como sistema bancario, quiero registrar transacciones, para mantener el historial y actualizar el saldo.

### Historias de Usuario

#### HU-01: Registrar depósito en cuenta

```
Como:        Sistema bancario
Quiero:      Crear un movimiento de tipo "Depósito"
Para:        registrar el nuevo saldo resultante del movimiento

Prioridad:   Alta
Estimación:  M
Dependencias: HU de cuentas (ya existe)
Capa:        Backend
```

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Depósito exitoso incrementa saldo
  Dado que:  una cuenta con id "1" existe con saldo 1000.00
  Cuando:    registro un movimiento de tipo "Depósito" con valor 500.00
  Entonces:  el movimiento se crea exitosamente
  Y:         el movimiento persiste el saldo resultante en 1500.00
  Y:         la fecha del movimiento se registra automáticamente
  Y:         el endpoint retorna status 201
```

**Error Path**
```gherkin
CRITERIO-1.2: Rechazo de depósito con valor cero
  Dado que:  una cuenta con id "1" existe
  Cuando:    intento crear un movimiento con valor 0
  Entonces:  el sistema retorna status 400
  Y:         el mensaje de error indica "El valor debe ser mayor a cero"
  Y:         ningún movimiento es registrado
  Y:         el saldo permanece sin cambios
```

**Error Path**
```gherkin
CRITERIO-1.3: Rechazo de depósito con valor negativo
  Dado que:  una cuenta con id "1" existe
  Cuando:    intento crear un movimiento con valor -100.00
  Entonces:  el sistema retorna status 400
  Y:         el mensaje de error indica "El valor debe ser positivo"
  Y:         ningún movimiento es registrado
```

---

#### HU-02: Registrar retiro en cuenta

```
Como:        Sistema bancario
Quiero:      Crear un movimiento de tipo "Retiro"
Para:        registrar el nuevo saldo resultante con validación de fondos

Prioridad:   Alta
Estimación:  M
Dependencias: HU-01
Capa:        Backend
```

#### Criterios de Aceptación — HU-02

**Happy Path**
```gherkin
CRITERIO-2.1: Retiro exitoso decrementa saldo
  Dado que:  una cuenta con id "1" existe con saldo 1000.00
  Cuando:    registro un movimiento de tipo "Retiro" con valor 300.00
  Entonces:  el movimiento se crea exitosamente
  Y:         el movimiento persiste el saldo resultante en 700.00
  Y:         el valor se registra internamente como -300.00
  Y:         la fecha se asigna automáticamente
  Y:         el endpoint retorna status 201
```

**Error Path**
```gherkin
CRITERIO-2.2: Rechazo de retiro por saldo insuficiente
  Dado que:  una cuenta con id "1" existe con saldo 100.00
  Cuando:    intento realizar un retiro de 150.00
  Entonces:  el sistema retorna status 409 (Conflict)
  Y:         el mensaje indica "Saldo no disponible"
  Y:         el saldo permanece en 100.00
  Y:         ningún movimiento es registrado
```

**Error Path**
```gherkin
CRITERIO-2.3: Rechazo de retiro con valor cero
  Dado que:  una cuenta con id "1" existe
  Cuando:    intento crear un retiro con valor 0
  Entonces:  el sistema retorna status 400
  Y:         el mensaje indica "El valor debe ser mayor a cero"
  Y:         ningún movimiento es registrado
```

---

#### HU-03: Listar movimientos de una cuenta

```
Como:        Usuario del sistema
Quiero:      Consultar el historial de movimientos de una cuenta
Para:        auditar y revisar todas las transacciones registradas

Prioridad:   Alta
Estimación:  S
Dependencias: HU-01, HU-02
Capa:        Backend
```

#### Criterios de Aceptación — HU-03

**Happy Path**
```gherkin
CRITERIO-3.1: Listar movimientos por cuenta
  Dado que:  una cuenta con id "1" tiene movimientos asociados
  Cuando:    envío una solicitud GET a `/api/movimientos?cuentaId=1`
  Entonces:  el sistema retorna status 200
  Y:         retorna una lista de movimientos de esa cuenta
  Y:         los movimientos están ordenados por fecha descendente
  Y:         cada movimiento incluye: fecha, tipo, valor, saldo
```

**Error Path**
```gherkin
CRITERIO-3.2: Cuenta sin movimientos retorna lista vacía
  Dado que:  una cuenta con id "99" existe pero no tiene movimientos
  Cuando:    envío GET a `/api/movimientos?cuentaId=99`
  Entonces:  el sistema retorna status 200
  Y:         retorna una lista vacía []
```

**Error Path**
```gherkin
CRITERIO-3.3: Cuenta inexistente retorna 404
  Dado que:  no existe una cuenta con id "999"
  Cuando:    envío GET a `/api/movimientos?cuentaId=999`
  Entonces:  el sistema retorna status 404
  Y:         el mensaje indica "Cuenta no encontrada"
```

---

#### HU-04: Validar fecha automática de movimiento

```
Como:        Sistema bancario
Quiero:      Asignar automáticamente la fecha y hora actual a cada movimiento
Para:        garantizar el registro temporal exacto sin intervención del usuario

Prioridad:   Alta
Estimación:  XS
Dependencias: HU-01, HU-02
Capa:        Backend
```

#### Criterios de Aceptación — HU-04

**Happy Path**
```gherkin
CRITERIO-4.1: Fecha se asigna automáticamente
  Dado que:  creo un movimiento sin especificar fecha
  Cuando:    el servidor recibe la solicitud
  Entonces:  la fecha y hora actual se asignan automáticamente (UTC)
  Y:         la fecha no puede ser modificada por el usuario
  Y:         el movimiento refleja la fecha/hora exacta del servidor
```

---

### Reglas de Negocio

1. **Validación de Valor**: Todo movimiento debe tener un valor > 0 en la solicitud. No se permiten valores cero o negativos.
2. **Tipo de Movimiento**: Solo se aceptan tipos "Depósito" o "Retiro" (case-sensitive).
3. **Saldo Insuficiente**: Un retiro no se procesa si el saldo disponible en `cuenta` es menor que el valor del retiro. Código HTTP: 409 con mensaje: "Saldo no disponible".
4. **Persistencia de Valor Signed**: 
  - Depósito: se persiste como `valor = +500.00` (positivo)
  - Retiro: se persiste como `valor = -300.00` (negativo)
  - Cálculo del movimiento: `saldo_movimiento = saldo_anterior + valor_signed`
  - Cálculo de cuenta: `cuenta.saldo_disponible = cuenta.saldo_disponible + valor_signed`
5. **Visualización de Valor**: En la respuesta, el campo `Movimiento` siempre muestra el valor absoluto (sin signo), independientemente del tipo.
6. **Fecha Automática**: La fecha se asigna en el servidor (no del cliente) y no puede ser modificada.
7. **Atomicidad**: La creación del movimiento y la actualización del saldo deben ser atómicas (una transacción única).
8. **Integridad**: Cada movimiento debe tener una referencia válida a una cuenta existente.
9. **Histórico Inmutable**: Los movimientos registrados no se pueden eliminar ni modificar (solo lectura después de creación).

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades afectadas
| Entidad | Almacén | Cambios | Descripción |
|---------|---------|---------|-------------|
| `Movimiento` | tabla `movimiento` (banking_db) | **nueva** | Registra cada transacción |
| `Cuenta` | tabla `cuenta` (banking_db) | saldo disponible persistido y actualizado | Mantiene el saldo vigente para consultas y validación inmediata |
| `Movimiento` | tabla `movimiento` (banking_db) | **nueva** | Registra cada transacción, el valor signed y el saldo resultante |

#### Campos del modelo Movimiento
| Campo | Tipo | Obligatorio | Validación | Descripción |
|-------|------|-------------|------------|-------------|
| `id` | BIGINT | sí | auto-generado | Identificador único |
| `fecha` | TIMESTAMP (UTC) | sí | auto-generado (server) | Fecha y hora del movimiento |
| `tipo_movimiento` | VARCHAR(20) | sí | "Depósito" \| "Retiro" | Tipo de transacción |
| `valor` | DECIMAL(15,2) | sí | signed | Monto de la transacción (positivo para depósito, negativo para retiro) |
| `saldo` | DECIMAL(15,2) | sí | calculado | Saldo de la cuenta después del movimiento |
| `cuenta_id` | BIGINT | sí | FK válida | Referencia a la cuenta afectada |

#### Índices / Constraints
- `PK (id)` — búsqueda rápida de movimiento
- `FK (cuenta_id)` — referencia a cuenta, borrado restringido
- `INDEX (cuenta_id, fecha DESC)` — búsqueda y ordenamiento frecuente por cuenta y fecha
- `CHECK (valor <> 0)` — garantiza que no se persistan movimientos con valor nulo
- `CHECK (tipo_movimiento IN ('Depósito', 'Retiro'))` — valores permitidos

#### Respuesta de Formato Especificado
Según el requerimiento, la respuesta debe incluir los campos legibles:

```json
{
  "Fecha": "10/2/2022",
  "Cliente": "Marianela Montalvo",
  "Numero Cuenta": "225487",
  "Tipo": "Corriente",
  "Saldo Inicial": 100,
  "Estado": true,
  "Movimiento": 600,
  "Saldo Disponible": 700
}
```

**Mapeo de campos**:
- `Fecha` ← movimiento.fecha (formato: dd/MM/yyyy via CustomSerializer Jackson)
- `Cliente` ← persona.nombre (desde FK cliente_ref de la cuenta)
- `Numero Cuenta` ← cuenta.numero_cuenta
- `Tipo` ← cuenta.tipo_cuenta
- `Saldo Inicial` ← cuenta.saldo_inicial
- `Estado` ← cuenta.estado
- `Movimiento` ← ABS(movimiento.valor) (siempre valor absoluto, sin signo, en respuesta)
- `Saldo Disponible` ← movimiento.saldo (saldo resultante después del movimiento)

---

### API Endpoints

#### POST /api/movimientos
- **Descripción**: Crea un nuevo movimiento (depósito o retiro)
- **Auth requerida**: no (MVP)
- **Request Body**:
  ```json
  {
    "cuentaId": 1,
    "tipo": "Depósito",
    "valor": 500.00
  }
  ```
- **Response 201 - Éxito**:
  ```json
  {
    "data": {
      "id": 42,
      "fecha": "2026-04-29T10:30:00Z",
      "tipo": "Depósito",
      "valor": 500.00,
      "saldo": 1500.00,
      "cuentaId": 1
    },
    "timestamp": "2026-04-29T10:30:00Z"
  }
  ```
- **Response 400 - Validación fallida**:
  ```json
  {
    "type": "https://api.example.com/errors/validation-error",
    "title": "Bad Request — Validation failed",
    "status": 400,
    "detail": "El valor debe ser mayor a cero",
    "instance": "/api/movimientos"
  }
  ```
- **Response 409 - Saldo insuficiente**:
  ```json
  {
    "type": "https://api.example.com/errors/insufficient-funds",
    "title": "Conflict — Insufficient funds",
    "status": 409,
    "detail": "Saldo no disponible. Balance: 100.00, Requested: 150.00",
    "instance": "/api/movimientos"
  }
  ```
- **Response 404 - Cuenta no encontrada**:
  ```json
  {
    "type": "https://api.example.com/errors/not-found",
    "title": "Not Found",
    "status": 404,
    "detail": "Cuenta no encontrada con id: 999",
    "instance": "/api/movimientos"
  }
  ```

---

#### GET /api/movimientos
- **Descripción**: Lista movimientos filtrados por cuentaId
- **Auth requerida**: no (MVP)
- **Query Parameters**:
  - `cuentaId` (required): ID de la cuenta
  - `offset` (optional, default: 0): Para paginación
  - `limit` (optional, default: 50): Registros por página
- **Response 200 - Éxito**:
  ```json
  {
    "data": [
      {
        "Fecha": "10/2/2022",
        "Cliente": "Marianela Montalvo",
        "Numero Cuenta": "225487",
        "Tipo": "Corriente",
        "Saldo Inicial": 100,
        "Estado": true,
        "Movimiento": 600,
        "Saldo Disponible": 700
      },
      {
        "Fecha": "11/2/2022",
        "Cliente": "Marianela Montalvo",
        "Numero Cuenta": "225487",
        "Tipo": "Corriente",
        "Saldo Inicial": 700,
        "Estado": true,
        "Movimiento": 100,
        "Saldo Disponible": 600
      }
    ],
    "timestamp": "2026-04-29T10:30:00Z"
  }
  ```
- **Response 404 - Cuenta no encontrada**:
  ```json
  {
    "type": "https://api.example.com/errors/not-found",
    "title": "Not Found",
    "status": 404,
    "detail": "Cuenta no encontrada con id: 999",
    "instance": "/api/movimientos"
  }
  ```

---

#### GET /api/movimientos?cuentaId={cuentaId}&from={fromDate}&to={toDate}
- **Descripción**: Lista movimientos de una cuenta en un rango de fechas
- **Auth requerida**: no (MVP)
- **Query Parameters**:
  - `cuentaId` (required): ID de la cuenta
  - `from` (optional): Fecha inicial (formato: yyyy-MM-dd)
  - `to` (optional): Fecha final (formato: yyyy-MM-dd)
  - `offset` (optional, default: 0)
  - `limit` (optional, default: 50)
- **Response 200 - Éxito**: Array de movimientos en rango, ordenados por fecha DESC
- **Response 404**: Cuenta no encontrada

---

### Arquitectura y Dependencias

#### Paquetes nuevos requeridos
- `com.bank.bankingservice.domain.model.Movimiento` — entidad de dominio
- `com.bank.bankingservice.application.dto.MovimientoCreateRequest` — DTO entrada
- `com.bank.bankingservice.application.dto.MovimientoResponse` — DTO salida
- `com.bank.bankingservice.application.dto.MovimientoListResponse` — DTO listado
- `com.bank.bankingservice.application.service.MovimientoService` — lógica de negocio
- `com.bank.bankingservice.domain.ports.in.MovimientoUseCase` — puerto de entrada
- `com.bank.bankingservice.domain.ports.out.MovimientoRepository` — puerto de salida
- `com.bank.bankingservice.infrastructure.output.MovimientoJpaRepository` — adaptador JPA
- `com.bank.bankingservice.infrastructure.input.rest.MovimientoController` — controlador REST
- Excepciones: `MovimientoValidationException`, `InsufficientFundsException`

#### Integraciones y dependencias
- **No requiere servicios externos** — es una operación local a MS-Banking
- **Impacto en BD**: Usar tabla `movimiento` ya definida en BaseDatos.sql

#### Relación con otras specs
- **SPEC-001 (Cuentas)**: Depende de la existencia y operatividad de cuentas
- **HU-006 (Comunicación Asíncrona)**: Futuro — emitir evento "movimiento.creado" a RabbitMQ (no incluido en esta spec)

---

### Notas de Implementación

> **IMPORTANTE — Validación de Saldo en Dominio**:
> La lógica de validación de saldo insuficiente se implementa en el dominio (`Movimiento.registrar()` o `Cuenta.registrarMovimiento()`), no en el servicio. Esto sigue el patrón de **Domain-Driven Design**.
>
> **Persistencia de Valor Signed**:
> - El campo `valor` en BD se persiste con signo: positivo para depósitos, negativo para retiros.
> - La lógica de cálculo en dominio es: `saldo_movimiento = saldo_anterior + valor_signed`.
> - `cuenta.saldo_disponible` se actualiza en la misma transacción usando el mismo `valor_signed`.
> - En las respuestas (DTOs), siempre se visualiza como valor absoluto: `Movimiento = ABS(valor)`.
>
> **Formato de Fecha con CustomSerializer**:
> - Implementar un `LocalDateTimeSerializer` personalizado que convierta TIMESTAMP a formato "dd/MM/yyyy".
> - Aplicar mediante anotación `@JsonSerialize(using = LocalDateTimeSerializer.class)` en el DTO `MovimientoListResponse.Fecha`.
> - Alternativa: Configurar Jackson en `application.yaml` con `spring.jackson.serialization.write-dates-as-timestamps: false` + `spring.jackson.time-zone: UTC`.
>
> **Manejo de Excepciones**:
> - `InsufficientFundsException` es capturada por el **Global Exception Handler** y mapeada automáticamente a HTTP 409.
> - El handler debe incluir el mensaje exacto: "Saldo no disponible" (sin detalles técnicos adicionales).
> - Ver configuración en `com.bank.bankingservice.infrastructure.config.GlobalExceptionHandler`.
>
> **Atomicidad de Operación**:
> La creación del movimiento, la actualización de `cuenta.saldo_disponible` y el cálculo del saldo resultante deben ejecutarse en una sola transacción. Usar `@Transactional` en el servicio.

> **Concurrencia y Consistencia**:
> - La fuente de verdad operativa para validar saldo es `cuenta.saldo_disponible`.
> - El acceso concurrente debe protegerse con `@Version` en la entidad `Cuenta` para evitar escrituras perdidas.
> - No se requiere bloquear la lectura del último movimiento para calcular el saldo anterior.
>
> **Sin Devoluciones Futuras**:
> Este MVP no soporta "devoluciones" o "reversiones" de movimientos. Cada movimiento es definitivo una vez registrado.
>
> **Patrón Hexagonal**:
> - Domain: `Movimiento` como agregado raíz, métodos de validación y cálculo en la clase
> - Application: `MovimientoService` implementa `MovimientoUseCase`, orquesta repositorio y validaciones
> - Infrastructure: `MovimientoJpaRepository` (JPA), `MovimientoController` (REST)

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada ítem (`[x]`) al completarlo.

### Backend

#### Implementación — Domain

- [ ] Crear `Movimiento.java` — agregado raíz con métodos:
  - `static Movimiento registrarDeposito(Long cuentaId, BigDecimal valor)` 
  - `static Movimiento registrarRetiro(Long cuentaId, BigDecimal valor)`
  - `registrar()` — aplica lógica de validación y cálculo de saldo
  - Getters para: id, fecha, tipo, valor, saldo, cuentaId

- [ ] Crear `MovimientoType.java` (enum) — valores: `DEPOSITO`, `RETIRO`

- [ ] Crear excepciones de dominio:
  - `MovimientoValidationException` — valor inválido, tipo inválido
  - `InsufficientFundsException extends AccountConflictException` — saldo insuficiente

- [ ] Crear puerto de entrada `MovimientoUseCase.java`:
  - `Movimiento registrarDeposito(Long cuentaId, BigDecimal valor)`
  - `Movimiento registrarRetiro(Long cuentaId, BigDecimal valor)`
  - `List<Movimiento> listarMovimientosPorCuenta(Long cuentaId)`
  - `List<Movimiento> listarMovimientosPorCuentaEnRango(Long cuentaId, LocalDate from, LocalDate to)`

- [ ] Crear puerto de salida `MovimientoRepository.java`:
  - `Movimiento save(Movimiento movimiento)`
  - `List<Movimiento> findByCuentaId(Long cuentaId, int offset, int limit)`
  - `List<Movimiento> findByCuentaIdAndFechaRange(Long cuentaId, LocalDate from, LocalDate to)`

#### Implementación — Application

- [ ] Crear DTO `MovimientoCreateRequest.java` (record):
  - `Long cuentaId`
  - `String tipo` ("Depósito" o "Retiro")
  - `BigDecimal valor`

- [ ] Crear DTO `MovimientoResponse.java` (record):
  - `Long id`
  - `String fecha` (formato ISO8601)
  - `String tipo`
  - `BigDecimal valor`
  - `BigDecimal saldo`
  - `Long cuentaId`

- [ ] Crear DTO `MovimientoListResponse.java` (record) con campos:
  - `String Fecha` (formato dd/MM/yyyy)
  - `String Cliente`
  - `String Numero Cuenta`
  - `String Tipo` (tipo de cuenta)
  - `BigDecimal Saldo Inicial`
  - `Boolean Estado`
  - `BigDecimal Movimiento`
  - `BigDecimal Saldo Disponible`

- [ ] Crear `MovimientoService.java` — implementa `MovimientoUseCase`:
  - Constructor con inyección: `MovimientoRepository`, `CuentaRepository`
  - Método `registrarDeposito()` — valida, crea movimiento, actualiza `cuenta.saldo_disponible`, guarda
  - Método `registrarRetiro()` — valida saldo, crea movimiento, actualiza `cuenta.saldo_disponible`, guarda
  - Método `listarMovimientosPorCuenta()` — usa repository
  - Método `listarMovimientosPorCuentaEnRango()` — usa repository con filtro de fechas
  - `@Transactional` en métodos que modifican estado

#### Implementación — Infrastructure

- [ ] Crear `MovimientoJpaRepository.java` (interface extends JpaRepository):
  - Métodos personalizados según la spec

- [ ] Crear entity `MovimientoJpaEntity.java` (o mapear a Movimiento directamente):
  - `@Table(name = "movimiento", schema = "banking_db")`
  - `@Id`, `@GeneratedValue`, `@Column` apropiados
  - `@ManyToOne` a CuentaJpaEntity

- [ ] Crear `MovimientoMapper.java` — convierte entre Domain y JPA:
  - `toDomain(MovimientoJpaEntity)`
  - `toPersistence(Movimiento)`
  - `toListResponse(Movimiento, Cuenta)` — con datos de cliente

- [ ] Crear `MovimientoController.java`:
  - `@PostMapping("/api/movimientos")` — crea movimiento
    - Parsea `tipo` a enum, valida, llama al servicio
    - Retorna 201 con `MovimientoResponse`
  - `@GetMapping("/api/movimientos")` — lista movimientos
    - Query param: `cuentaId` (requerido)
    - Retorna 200 con array de `MovimientoListResponse`
  - `@GetMapping("/api/movimientos?cuentaId={id}&from={from}&to={to}")` — filtra por rango
    - Retorna 200 con array filtrado
  - Manejo de excepciones:
    - `MovimientoValidationException` → 400
    - `InsufficientFundsException` → 409
    - `AccountNotFoundException` → 404

#### Tests Backend (Matriz 3-2-1)

**MovimientoService Tests** (3 tests por método)

- [ ] `registrarDeposito_success` — depósito exitoso, saldo se incrementa
- [ ] `registrarDeposito_throws_ValidationException_when_valor_cero` — valor 0 rechazado
- [ ] `registrarDeposito_throws_ValidationException_when_valor_negativo` — valor negativo rechazado
- [ ] `registrarRetiro_success` — retiro exitoso, saldo se decrementa
- [ ] `registrarRetiro_throws_InsufficientFundsException` — saldo insuficiente
- [ ] `registrarRetiro_throws_ValidationException_when_valor_cero` — valor 0 rechazado
- [ ] `listarMovimientosPorCuenta_returns_lista_ordenada_por_fecha_DESC`
- [ ] `listarMovimientosPorCuenta_returns_empty_when_no_movimientos`
- [ ] `listarMovimientosPorCuenta_throws_AccountNotFoundException_when_cuenta_no_existe`

**MovimientoController Tests** (3 tests por endpoint)

- [ ] `POST_movimientos_returns_201_cuando_deposito_valido` — happy path depósito
- [ ] `POST_movimientos_returns_400_cuando_valor_cero` — validación
- [ ] `POST_movimientos_returns_409_cuando_saldo_insuficiente_en_retiro` — conflicto
- [ ] `GET_movimientos_returns_200_con_lista` — happy path listado
- [ ] `GET_movimientos_returns_404_cuando_cuenta_no_existe` — cuenta no encontrada
- [ ] `GET_movimientos_returns_empty_cuando_sin_movimientos` — lista vacía
- [ ] `GET_movimientos_con_rango_returns_200_filtrado_por_fechas` — happy path rango
- [ ] `GET_movimientos_con_rango_returns_200_empty_cuando_fuera_de_rango` — sin resultados
- [ ] `GET_movimientos_con_rango_returns_404_cuando_cuenta_no_existe` — no encontrada

**MovimientoJpaRepository Tests** (2 tests por método)

- [ ] `save_returns_persisted_movimiento` — guardado exitoso
- [ ] `save_throws_DataIntegrityViolationException_when_valor_cero` — restricción BD
- [ ] `findByCuentaId_returns_movimientos_ordenados_DESC` — búsqueda y orden
- [ ] `findByCuentaId_returns_empty_when_no_movimientos` — lista vacía
- [ ] `findByCuentaIdAndFechaRange_returns_filtrados_por_rango` — filtro rango
- [ ] `findByCuentaIdAndFechaRange_returns_empty_cuando_fuera_de_rango` — sin resultados

---

### QA

- [ ] Ejecutar skill `/gherkin-case-generator` → criterios CRITERIO-1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 3.1, 3.2, 3.3, 4.1
- [ ] Ejecutar skill `/risk-identifier` → clasificación ASD de riesgos
- [ ] Revisar cobertura de tests contra criterios de aceptación
- [ ] Validar que todas las reglas de negocio están cubiertas
- [ ] Ejecutar suite de pruebas — cobertura >= 80%
- [ ] Actualizar estado spec: `status: IMPLEMENTED` cuando todos los tests pasen

