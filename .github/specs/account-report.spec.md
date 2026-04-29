---
id: SPEC-005
status: DRAFT
feature: account-report
created: 2026-04-29
updated: 2026-04-29
author: spec-generator
version: "1.0"
related-specs: []
---

# Spec: Reporte de Estado de Cuenta

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción
Funcionalidad que permite a los clientes consultar un reporte detallado de sus movimientos por rango de fechas. El reporte agrupa todas las cuentas del cliente con sus movimientos filtrados en el rango de fechas especificado, proporcionando una vista auditable de las finanzas.

### Requerimiento de Negocio
Ver [account-report.md](../requirements/account-report.md) — HU-005: Reporte de Estado de Cuenta

### Historias de Usuario

#### HU-005: Reporte de Estado de Cuenta

```
Como:        Cliente autenticado
Quiero:      Consultar mis movimientos por rango de fechas
Para:        Auditar y verificar mis transacciones financieras

Prioridad:   Alta
Estimación:  M
Dependencias: Ninguna (requiere que Cliente y Cuenta existan)
Capa:        Backend + API REST
```

#### Criterios de Aceptación — HU-005

**Happy Path — Obtener reporte con múltiples cuentas**
```gherkin
CRITERIO-5.1: Reporte exitoso para cliente con múltiples cuentas
  Dado que:  un cliente con id "1" existe y tiene 2 cuentas asociadas con movimientos
  Cuando:    realizo GET a `/api/reportes?cliente=1&fecha=2022-01-01,2022-12-31`
  Entonces:  retorna 200 OK
  Y:        la respuesta contiene estructura JSON con "cliente" y "cuentas"
  Y:        cada cuenta incluye sus movimientos dentro del rango de fechas
  Y:        el saldo_disponible en cada movimiento refleja el estado de la cuenta
```

**Happy Path — Reporte con estructura exacta**
```gherkin
CRITERIO-5.2: Estructura JSON del reporte es correcta
  Dado que:  un cliente "Jose Lema" (id 1) tiene una cuenta "478758" tipo "Ahorro"
  Cuando:    consulto el reporte para ese cliente
  Entonces:  la respuesta tiene estructura:
             {
               "cliente": { "id": 1, "nombre": "Jose Lema" },
               "cuentas": [
                 {
                   "numeroCuenta": "478758",
                   "tipo": "Ahorro",
                   "saldo": 2000.00,
                   "estado": true,
                   "movimientos": [
                     {
                       "fecha": "2022-02-10",
                       "cliente": "Jose Lema",
                       "numeroCuenta": "478758",
                       "tipo": "Ahorro",
                       "saldoInicial": 2000,
                       "estado": true,
                       "movimiento": -575,
                       "saldoDisponible": 1425
                     }
                   ]
                 }
               ]
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
  Dado que:  el sistema acepta fechas en formato "yyyy-MM-dd"
  Cuando:    realizo GET a `/api/reportes?cliente=1&fecha=01/01/2022,31/12/2022`
  Entonces:  retorna 400 Bad Request
  Y:        el detail indica: "Invalid date format. Expected yyyy-MM-dd"
```

**Error Path — Cliente no encontrado**
```gherkin
CRITERIO-5.9: Cliente inexistente
  Dado que:  no existe cliente con id "9999"
  Cuando:    realizo GET a `/api/reportes?cliente=9999&fecha=2022-01-01,2022-12-31`
  Entonces:  retorna 404 Not Found
  Y:        el detail indica: "Client with id 9999 not found"
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
1. **Parámetros obligatorios**: cliente y fecha son requeridos en la query string
2. **Formato de fecha**: debe ser "yyyy-MM-dd,yyyy-MM-dd" (rango separado por coma)
3. **Rango válido**: fecha inicio ≤ fecha fin
4. **Filtro temporal**: solo incluir movimientos con `fecha >= inicio AND fecha <= fin`
5. **Dato de cliente**: obtener nombre del cliente desde customerservice
6. **Cuentas incluidas**: todas las cuentas activas e inactivas del cliente
7. **Saldo en movimiento**: reflejar el saldo disponible después del movimiento
8. **Unicidad**: no duplicar movimientos en el reporte

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades afectadas

| Entidad | Almacén | Cambios | Descripción |
|---------|---------|---------|-------------|
| `Movimiento` | tabla `movimiento` (banking_db) | nueva entidad de dominio | Captura cada transacción en una cuenta |
| `Cuenta` | tabla `cuenta` (banking_db) | sin cambios | Cuentas existentes |
| `Cliente` | tabla `cliente` (customer_db) | sin cambios | Clientes existentes |
| `ReporteEstadoCuenta` | no aplica (DTO) | nueva | Modelo de respuesta del reporte |

#### Campos del modelo — Movimiento (Entity + Domain)

| Campo | Tipo | Obligatorio | Validación | Descripción |
|-------|------|-------------|------------|-------------|
| `id` | BigInt | sí | auto-generado | Identificador único |
| `fecha` | LocalDateTime (UTC) | sí | no nula | Timestamp del movimiento |
| `tipoMovimiento` | String | sí | DEPOSITO \| RETIRO | Tipo de transacción |
| `valor` | BigDecimal | sí | > 0 | Monto del movimiento |
| `saldo` | BigDecimal | sí | >= 0 | Saldo de la cuenta después del movimiento |
| `cuentaId` | BigInt | sí | FK a cuenta.id | Referencia a la cuenta |

#### DTOs de Respuesta

**ReporteEstadoCuentaResponse**
```java
public record ReporteEstadoCuentaResponse(
    ClienteResumenDto cliente,
    List<CuentaConMovimientosDto> cuentas
) {}

public record ClienteResumenDto(
    Long id,
    String nombre
) {}

public record CuentaConMovimientosDto(
    String numeroCuenta,
    String tipo,        // tipo_cuenta → campo "tipo"
    BigDecimal saldo,   // saldo_disponible
    Boolean estado,
    List<MovimientoDto> movimientos
) {}

public record MovimientoDto(
    LocalDate fecha,
    String cliente,
    String numeroCuenta,
    String tipo,         // tipo_cuenta
    BigDecimal saldoInicial,
    Boolean estado,
    BigDecimal movimiento,
    BigDecimal saldoDisponible
) {}
```

#### Índices / Constraints
- **Índice en `movimiento.cuenta_id`**: búsquedas frecuentes por cuenta
- **Índice en `movimiento.fecha`**: filtros por rango de fechas
- **Índice compuesto `(cuenta_id, fecha)`**: optimización de búsquedas combinadas

### API Endpoints

#### GET /api/reportes
- **Descripción**: Obtiene el reporte de estado de cuenta para un cliente en un rango de fechas
- **Auth requerida**: sí (implementar en futuro)
- **Query Parameters**:
  - `cliente` (Long, obligatorio): ID del cliente
  - `fecha` (String, obligatorio): Rango en formato "yyyy-MM-dd,yyyy-MM-dd"
- **Response 200 OK**:
  ```json
  {
    "data": {
      "cliente": {
        "id": 1,
        "nombre": "Jose Lema"
      },
      "cuentas": [
        {
          "numeroCuenta": "478758",
          "tipo": "Ahorro",
          "saldo": 2000.00,
          "estado": true,
          "movimientos": [
            {
              "fecha": "2022-02-10",
              "cliente": "Jose Lema",
              "numeroCuenta": "478758",
              "tipo": "Ahorro",
              "saldoInicial": 2000,
              "estado": true,
              "movimiento": -575,
              "saldoDisponible": 1425
            }
          ]
        }
      ]
    },
    "timestamp": "2026-04-29T10:30:00Z"
  }
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
- `customerservice` vía HTTP (para obtener datos del cliente por ID)
- BD `banking_db` (lectura de movimientos)

**Impacto en punto de entrada:**
- Registrar `ReporteEstadoCuentaService` como bean en contexto Spring
- Registrar `ClienteGatewayAdapter` como bean
- No es necesario modificar `BankingserviceApplication.java` si se usan anotaciones `@Service`, `@Repository`, `@Component`

### Notas de Implementación
- **No crear Movimiento vacío**: Los movimientos se crean cuando hay transacciones (futuros depósitos/retiros).
- **Llamada síncrona a customerservice**: Implementar con RestTemplate o WebClient para obtener nombre del cliente.
- **Reporte como lectura**: Solo lectura de BD, sin crear nuevos movimientos.
- **Filtrado en Java**: Filtrar movimientos por rango de fechas en el service (no en SQL).
- **Saldo inicial del movimiento**: Es el saldo de la cuenta antes de ese movimiento (calcular restando el valor del movimiento al saldo actual).

---

## 3. LISTA DE TAREAS

> Checklist accionable para backend y QA. Marcar cada ítem (`[x]`) al completarlo.

### Backend

#### Implementación
- [ ] Crear domain model `Movimiento` con factory methods en `domain/model/`
- [ ] Crear port de salida `ClienteGateway` interface en `domain/ports/out/`
- [ ] Crear DTOs (records) en `application/dto/`: `ReporteEstadoCuentaResponse`, `ClienteResumenDto`, `CuentaConMovimientosDto`, `MovimientoDto`
- [ ] Implementar `ReporteEstadoCuentaService` con lógica en `application/service/` (orquestar flujo, validar parámetros, filtrar movimientos)
- [ ] Implementar `MovimientoRepository` JPA en `infrastructure/output/persistence/`
- [ ] Implementar `ClienteGatewayAdapter` en `infrastructure/output/adapter/` (REST call a customerservice)
- [ ] Implementar `ReporteController` GET `/api/reportes` en `infrastructure/input/rest/`
- [ ] Registrar DTOs y servicios como beans en `infrastructure/config/`
- [ ] Crear excepciones de dominio si aplica: `ClienteNotFound`, `InvalidDateRange`

#### Tests Backend (Matriz 3-2-1)

**Regla: POR CADA MÉTODO = 3 tests | POR CADA ENDPOINT = 3 tests | POR CADA ADAPTER = 2 tests**

| Test Class | Cantidad | Tests a crear |
|-----------|---------|------------|
| `ReporteEstadoCuentaServiceTests.java` | 3 por método | `generarReporte_success`: happy path con múltiples cuentas y movimientos<br>`generarReporte_clienteNotFound`: cliente inexistente<br>`generarReporte_dateRangeInvalid`: fecha inicio > fecha fin<br>`filtrarMovimientosporFecha_success`: filtra correctamente<br>`filtrarMovimientosporFecha_emptyRange`: sin movimientos en rango<br>`filtrarMovimientosporFecha_multipleMovements`: múltiples movimientos |
| `ReporteControllerTests.java` | 3 por endpoint | `getReporte_200_success`: parámetros válidos<br>`getReporte_400_missingClienteParam`: sin parámetro cliente<br>`getReporte_400_missingFechaParam`: sin parámetro fecha<br>`getReporte_400_invalidDateFormat`: fecha en formato incorrecto<br>`getReporte_400_invalidDateRange`: inicio > fin<br>`getReporte_404_clienteNotFound`: cliente no existe |
| `ClienteGatewayAdapterTests.java` | 2 por método | `getCliente_success`: retorna cliente<br>`getCliente_httpError`: servicio indisponible |
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
