---
id: SPEC-005
status: APPROVED
feature: account-report
created: 2026-04-29
updated: 2026-04-30
author: spec-generator
version: "2.0"
related-specs: [SPEC-006]
---

# Spec: Reporte de Estado de Cuenta (Refactorización — Eventual Consistency)

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED
> **Cambio Major — v2.0**: Arquitectura desacoplada vía RabbitMQ + JOIN local con `cliente_ref` (SPEC-006)

---

## 1. REQUERIMIENTOS

### Descripción
Funcionalidad que permite a los clientes consultar un reporte detallado de sus movimientos por rango de fechas. El reporte realiza un **JOIN local** entre `movimiento` y `cliente_ref` (tabla sincronizada vía RabbitMQ desde MS-Customer), eliminando la dependencia síncrona hacia otro microservicio. Responde con un **array plano** de movimientos en formato Excel-friendly con llaves en español.

### Requerimiento de Negocio
Ver [account-report.md](../requirements/account-report.md) — HU-005: Reporte de Estado de Cuenta (Refactorizado con Eventual Consistency)

### Historias de Usuario

#### HU-005: Reporte de Estado de Cuenta (Eventual Consistency)

```
Como:        Cliente autenticado
Quiero:      Consultar mis movimientos por rango de fechas
Para:        Auditar y verificar mis transacciones financieras

Prioridad:   Alta
Estimación:  M
Dependencias: SPEC-006 (tabla cliente_ref sincronizada)
Capa:        Backend + API REST
```

#### Criterios de Aceptación — HU-005

**Happy Path — Array de movimientos exitoso**
```gherkin
CRITERIO-5.1: Reporte exitoso con múltiples movimientos
  Dado que:  un cliente con id "1" existe en cliente_ref y tiene movimientos en rango
  Cuando:    realizo GET a `/api/reportes?fecha=01/01/2022,31/12/2022&cliente=1`
  Entonces:  retorna 200 OK
  Y:         la respuesta es un array JSON con cada movimiento como objeto
  Y:         cada objeto contiene: Fecha, Cliente, Numero Cuenta, Tipo, Saldo Inicial, Estado, Movimiento, Saldo Disponible
  Y:         las fechas están en formato dd/MM/yyyy
```

**Happy Path — Reporte con estructura exacta**
```gherkin
CRITERIO-5.2: Estructura JSON del reporte es correcta
  Dado que:  un cliente "Jose Lema" (id 1) tiene un movimiento de -575 en cuenta "478758"
  Cuando:    consulto el reporte para ese cliente
  Entonces:  la respuesta es un array cuyos elementos tienen la siguiente forma (llaves exactas en español):
             {
               "Fecha": "10/02/2022",
               "Cliente": "Jose Lema",
               "Numero Cuenta": "478758",
               "Tipo": "Ahorro",
               "Saldo Inicial": 2000,
               "Estado": true,
               "Movimiento": -575,
               "Saldo Disponible": 1425
             }
```

**Happy Path — Múltiples cuentas**
```gherkin
CRITERIO-5.3: Reporte con múltiples cuentas independientes
  Dado que:  un cliente tiene 3 cuentas diferentes
  Cuando:    consulto el reporte de estado de cuenta
  Entonces:  el array "cuentas" contiene 3 objetos
  Y:        cada cuenta muestra sus movimientos independientes
  Y:        los saldos no se mezclan entre cuentas
```

**Happy Path — Sin movimientos en rango**
```gherkin
CRITERIO-5.4: Reporte sin movimientos en el rango especificado
  Dado que:  un cliente tiene cuentas pero sin movimientos en el rango 2022-01-01 a 2022-01-31
  Cuando:    consulto el reporte para ese rango
  Entonces:  retorna 200 OK
  Y:        las cuentas aparecen en la respuesta con array "movimientos" vacío
  Y:        el saldo_disponible es el saldo actual de la cuenta
```

**Happy Path — Rango de fechas filtrado**
```gherkin
CRITERIO-5.5: Movimientos filtrados correctamente por rango de fechas
  Dado que:  existen movimientos en fechas: 2022-01-15, 2022-02-10, 2022-12-25
  Cuando:    especifico rango 2022-02-01 a 2022-12-20
  Entonces:  el reporte solo incluye movimientos de 2022-02-10
  Y:        los movimientos de 2022-01-15 y 2022-12-25 no aparecen
```

**Error Path — Parámetro cliente ausente**
```gherkin
CRITERIO-5.6: Validación de parámetro cliente obligatorio
  Dado que:  no se especifica el parámetro cliente
  Cuando:    realizo GET a `/api/reportes?fecha=2022-01-01,2022-12-31`
  Entonces:  retorna 400 Bad Request
  Y:        el body contiene tipo "https://api.example.com/errors/invalid-request"
  Y:        el detail indica: "Missing required parameter: cliente"
```

**Error Path — Parámetro fecha ausente**
```gherkin
CRITERIO-5.7: Validación de parámetro fecha obligatorio
  Dado que:  no se especifica el parámetro fecha
  Cuando:    realizo GET a `/api/reportes?cliente=1`
  Entonces:  retorna 400 Bad Request
  Y:        el body contiene detail: "Missing required parameter: fecha"
```

**Error Path — Formato de fecha inválido**
```gherkin
CRITERIO-5.8: Validación del formato de fecha
  Dado que:  el sistema acepta fechas en formato "dd/MM/yyyy"
  Cuando:    realizo GET a `/api/reportes?cliente=1&fecha=01-01-2022,31-12-2022`
  Entonces:  retorna 400 Bad Request
  Y:        el detail indica: "Invalid date format. Expected dd/MM/yyyy"
```

**Error Path — Cliente no encontrado en cliente_ref**
```gherkin
CRITERIO-5.9: Cliente inexistente en tabla local
  Dado que:  no existe cliente con id "9999" en la tabla `cliente_ref`
  Cuando:    realizo GET a `/api/reportes?cliente=9999&fecha=01/01/2022,31/12/2022`
  Entonces:  retorna 404 Not Found
  Y:        el detail indica: "Client with id 9999 not found in local registry"
```

**Edge Case — Fecha inicio posterior a fecha fin**
```gherkin
CRITERIO-5.10: Validación de rango de fechas válido
  Dado que:  la fecha inicio es 2022-12-31 y la fecha fin es 2022-01-01
  Cuando:    realizo la consulta
  Entonces:  retorna 400 Bad Request
  Y:        el detail indica: "Start date must be before or equal to end date"
```

### Reglas de Negocio
1. **Parámetros obligatorios**: `cliente` y `fecha` son requeridos en la query string
2. **Formato de fecha**: debe ser `dd/MM/yyyy,dd/MM/yyyy` (rango separado por coma)
3. **Rango válido**: fecha inicio ≤ fecha fin
4. **Filtro temporal**: solo incluir movimientos con `fecha >= inicio AND fecha <= fin`
5. **Dato de cliente**: obtener nombre del cliente desde tabla local `cliente_ref` (sincronizada vía RabbitMQ)
6. **Sin dependencia síncrona**: NO usar RestTemplate/WebClient hacia MS-Customer
7. **Array plano**: estructura de respuesta es un array de objetos (no anidado)
8. **Llaves exactas en español**: `Fecha`, `Cliente`, `Numero Cuenta`, `Tipo`, `Saldo Inicial`, `Estado`, `Movimiento`, `Saldo Disponible`
9. **Eventual Consistency**: Si cliente no está en `cliente_ref` aún, retornar 404 (no reintentar automáticamente)

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades afectadas

| Entidad | Almacén | Cambios | Descripción |
|---------|---------|---------|-------------|
| `Movimiento` | tabla `movimiento` (banking_db) | existente | Captura cada transacción en una cuenta |
| `ClienteRef` | tabla `cliente_ref` (banking_db) | **nueva tabla** | Copia local sincronizada de cliente (SPEC-006) |
| `Cuenta` | tabla `cuenta` (banking_db) | sin cambios | Cuentas existentes |

#### Campos del modelo — Movimiento (Entity + Domain)

| Campo | Tipo | Obligatorio | Validación | Descripción |
|-------|------|-------------|------------|-------------|
| `id` | BigInt | sí | auto-generado | Identificador único |
| `fecha` | LocalDateTime (UTC) | sí | no nula | Timestamp del movimiento |
| `tipoMovimiento` | String | sí | DEPOSITO \| RETIRO | Tipo de transacción |
| `valor` | BigDecimal | sí | > 0 | Monto del movimiento |
| `saldo` | BigDecimal | sí | >= 0 | Saldo de la cuenta después del movimiento |
| `cuentaId` | BigInt | sí | FK a cuenta.id | Referencia a la cuenta |

#### Tabla `cliente_ref` (sincronizada vía RabbitMQ — SPEC-006)

```sql
CREATE TABLE cliente_ref (
  id BIGSERIAL PRIMARY KEY,
  cliente_id BIGINT NOT NULL UNIQUE,
  nombre VARCHAR(100) NOT NULL,
  identificacion VARCHAR(50),
  version INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cliente_ref_cliente_id ON cliente_ref(cliente_id);
```

#### DTOs de Respuesta

**ReporteEstadoCuentaResponse** (Array plano de `MovimientoReporteDto`)
```java
public record MovimientoReporteDto(
  String Fecha,              // Formato dd/MM/yyyy
  String Cliente,            // Nombre del cliente desde cliente_ref
  String NumeroCuenta,       // "Numero Cuenta"
  String Tipo,               // tipo_cuenta
  BigDecimal SaldoInicial,   // "Saldo Inicial"
  Boolean Estado,            // Estado de la cuenta
  BigDecimal Movimiento,     // Valor del movimiento (puede ser negativo)
  BigDecimal SaldoDisponible // "Saldo Disponible" (saldo después del movimiento)
) {}
```

#### Índices / Constraints
- **Índice en `movimiento.cuenta_id`**: búsquedas frecuentes por cuenta
- **Índice en `movimiento.fecha`**: filtros por rango de fechas
- **Índice compuesto `(cuenta_id, fecha)`**: optimización de búsquedas combinadas
- **Índice en `cliente_ref.cliente_id`**: búsqueda por cliente

### API Endpoints

#### GET /api/reportes
- **Descripción**: Obtiene el reporte de estado de cuenta para un cliente en un rango de fechas. Realiza **JOIN local** entre `movimiento`, `cuenta` y `cliente_ref`.
- **Auth requerida**: sí (futuro)
- **Query Parameters**:
  - `cliente` (Long, obligatorio): ID del cliente
  - `fecha` (String, obligatorio): Rango en formato `dd/MM/yyyy,dd/MM/yyyy`
- **Response 200 OK**: Array de `MovimientoReporteDto` (llaves exactas en español)
  ```json
  [
    {
      "Fecha": "10/02/2022",
      "Cliente": "Jose Lema",
      "Numero Cuenta": "478758",
      "Tipo": "Ahorro",
      "Saldo Inicial": 2000,
      "Estado": true,
      "Movimiento": -575,
      "Saldo Disponible": 1425
    }
  ]
  ```
- **Response 400 Bad Request**: parámetros ausentes, formato inválido o rango inválido
  ```json
  {
    "type": "https://api.example.com/errors/invalid-request",
    "title": "Invalid Request",
    "status": 400,
    "detail": "Missing required parameter: cliente",
    "instance": "/api/reportes"
  }
  ```
- **Response 404 Not Found**: cliente no existe
  ```json
  {
    "type": "https://api.example.com/errors/not-found",
    "title": "Not Found",
    "status": 404,
    "detail": "Client with id 9999 not found",
    "instance": "/api/reportes?cliente=9999&fecha=2022-01-01,2022-12-31"
  }
  ```

### Arquitectura y Dependencias

**Paquetes nuevos requeridos:**
- `com.bank.bankingservice.domain.model.Movimiento` (entidad de dominio pura)
- `com.bank.bankingservice.domain.ports.out.ClienteGateway` (puerto para consultar cliente)
- `com.bank.bankingservice.application.dto.{ReporteEstadoCuentaResponse, ClienteResumenDto, CuentaConMovimientosDto, MovimientoDto}`
- `com.bank.bankingservice.application.service.ReporteEstadoCuentaService` (caso de uso)
- `com.bank.bankingservice.infrastructure.input.rest.ReporteController` (REST)
- `com.bank.bankingservice.infrastructure.output.adapter.ClienteGatewayAdapter` (adapter para consumir customerservice)
- `com.bank.bankingservice.infrastructure.output.persistence.MovimientoRepository` (JPA)

**Dependencias externas:**
- **RabbitMQ** (consumidor de eventos `cliente.creado` y `cliente.actualizado` — ver SPEC-006)
- BD `banking_db` (lectura de movimientos y tabla `cliente_ref`)

**Impacto en punto de entrada:**
- Registrar `ReporteEstadoCuentaService` como bean en contexto Spring
- Registrar `ClienteGatewayAdapter` como bean
- No es necesario modificar `BankingserviceApplication.java` si se usan anotaciones `@Service`, `@Repository`, `@Component`

### Notas de Implementación
- **Tabla cliente_ref**: Creada y sincronizada por SPEC-006 vía RabbitMQ. SPEC-005 solo la **lee**.
- **Query SQL**: Realizar una consulta con JOIN entre `movimiento`, `cuenta` y `cliente_ref` para obtener los campos necesarios en una sola pasada.
- **Formato de fecha**: Conversión de `LocalDateTime` → String con patrón `dd/MM/yyyy` usando `DateTimeFormatter.ofPattern("dd/MM/yyyy")`.
- **Saldo inicial del movimiento**: Es el saldo de la cuenta antes del movimiento (calcular: `saldoDisponible - valorMovimiento` si saldoDisponible representa after-move).
- **Resiliencia**: Si `cliente_ref` no tiene el cliente, retornar 404 (validación falla, no reintentar automáticamente).
- **Array plano**: Respuesta es `List<MovimientoReporteDto>` con llaves exactas en español.

---

## 3. LISTA DE TAREAS

> Checklist accionable para backend y QA. Marcar cada ítem (`[x]`) al completarlo.

### Backend

#### Implementación
- [ ] **Prerequisito**: SPEC-006 implementado (tabla `cliente_ref` + consumer de RabbitMQ)
- [ ] Crear domain model `Movimiento` con factory methods en `domain/model/`
- [ ] Crear domain model `ClienteRef` (lectura simple) en `domain/model/`
- [ ] Crear DTO `MovimientoReporteDto` (record) en `application/dto/`
- [ ] Implementar `ReporteEstadoCuentaService` en `application/service/`:
  - [ ] Validar parámetros: `cliente`, `fecha` (obligatorios)
  - [ ] Parsear fecha en formato `dd/MM/yyyy` (usar `DateTimeFormatter`)
  - [ ] Validar rango (inicio ≤ fin)
  - [ ] Query con JOIN: SELECT de `movimiento` + `cuenta` + `cliente_ref`
  - [ ] Filtrar por rango de fechas
  - [ ] Convertir resultado a `List<MovimientoReporteDto>`
  - [ ] Mapear llaves exactas en español
- [ ] Implementar `MovimientoRepository` JPA en `infrastructure/output/persistence/`
  - [ ] Método: `findByClienteIdAndFechaBetween(Long clienteId, LocalDateTime start, LocalDateTime end)` o query personalizada con JOIN
- [ ] Implementar `ClienteRefRepository` JPA en `infrastructure/output/persistence/`
  - [ ] Método: `findByClienteId(Long clienteId)` → `Optional<ClienteRef>`
- [ ] Implementar `ReporteController` GET `/api/reportes` en `infrastructure/input/rest/`
  - [ ] Parsear query parameters
  - [ ] Validar parámetros
  - [ ] Llamar a service
  - [ ] Retornar array o error HTTP según corresponda
- [ ] Crear excepciones de dominio:
  - [ ] `ClienteNotFoundException` (404)
  - [ ] `InvalidDateFormatException` (400)
  - [ ] `InvalidDateRangeException` (400)
  - [ ] `MissingParameterException` (400)

#### Tests Backend (Matriz 3-2-1)

**Regla: POR CADA MÉTODO = 3 tests | POR CADA ENDPOINT = 3 tests | POR CADA ADAPTER = 2 tests**

| Test Class | Cantidad | Tests a crear |
|-----------|---------|------------|
| `ReporteEstadoCuentaServiceTests.java` | 3 por método | `generarReporte_success`: happy path con múltiples cuentas y movimientos<br>`generarReporte_clienteNotFound`: cliente inexistente<br>`generarReporte_dateRangeInvalid`: fecha inicio > fecha fin<br>`filtrarMovimientosporFecha_success`: filtra correctamente<br>`filtrarMovimientosporFecha_emptyRange`: sin movimientos en rango<br>`filtrarMovimientosporFecha_multipleMovements`: múltiples movimientos |
| `ReporteControllerTests.java` | 3 por endpoint | `getReporte_200_success`: parámetros válidos<br>`getReporte_400_missingClienteParam`: sin parámetro cliente<br>`getReporte_400_missingFechaParam`: sin parámetro fecha<br>`getReporte_400_invalidDateFormat`: fecha en formato incorrecto<br>`getReporte_400_invalidDateRange`: inicio > fin<br>`getReporte_404_clienteNotFound`: cliente no existe |
| `ClienteRefRepositoryTests.java` | 2 por método | `findByClienteId_success`: retorna cliente<br>`findByClienteId_notFound`: cliente inexistente |
| `MovimientoRepositoryTests.java` | 2 por método | `findByCuentaIdAndFechaBetween_success`: retorna movimientos filtrados<br>`findByCuentaIdAndFechaBetween_empty`: sin movimientos en rango |

**No generar:**
- Tests de métodos helper privados
- Tests de integración (requieren BD real)

### QA

#### Plan de Validación (a ejecutarse tras implementación)
- [ ] Crear test cases Gherkin en `docs/output/qa/account-report.feature` usando skill `gherkin-case-generator`
- [ ] Validar estructura JSON exacta según requirement
- [ ] Validar filtrado de fechas en UI/API
- [ ] Validar manejo de múltiples cuentas
- [ ] Validar mensajes de error HTTP 400/404
- [ ] Prueba de performance: reporte con 1000+ movimientos

#### Riesgos identificados (usar skill `risk-identifier`)
- [ ] Integración con customerservice (disponibilidad, latencia)
- [ ] Precisión del filtrado por fecha (timezone)
- [ ] Manejo de cuentas sin movimientos
- [ ] Cálculo correcto de saldoInicial en movimientos históricos
