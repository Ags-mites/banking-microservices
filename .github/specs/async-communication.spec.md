---
id: SPEC-006
status: DRAFT
feature: async-communication
created: 2026-04-29
updated: 2026-04-29
author: spec-generator
version: "1.0"
related-specs: []
---

# Spec: Desacoplamiento y Comunicación Asíncrona (RabbitMQ)

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción

Sistema de comunicación asíncrona entre MS-Customer y MS-Banking mediante RabbitMQ. MS-Customer emite eventos cuando se crea o actualiza un cliente. MS-Banking consume estos eventos y mantiene una tabla local `cliente_ref` para garantizar resiliencia, eventual consistency, autonomía para reportes e independencia de esquemas de base de datos.

### Requerimiento de Negocio

Como arquitecto, quiero que los servicios se comuniquen vía RabbitMQ, para asegurar la resiliencia.

### Historias de Usuario

#### HU-01: Emitir evento "cliente.creado" en MS-Customer

```
Como:        MS-Customer (Backend)
Quiero:      emitir un evento "cliente.creado" a RabbitMQ
Para:        que otros servicios sean notificados de forma asíncrona

Prioridad:   Alta
Estimación:  M
Dependencias: Ninguna
Capa:        Backend (Infrastructure Messaging)
```

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Cliente creado dispara evento exitosamente
  Dado que:  un cliente es creado exitosamente en MS-Customer
  Cuando:    la transacción de creación se confirma
  Entonces:  el sistema emite un evento "cliente.creado" a RabbitMQ
  Y:         el evento contiene los campos: clienteId, identificacion, nombre
```

**Error Path**
```gherkin
CRITERIO-1.2: Error al publicar evento
  Dado que:  se intenta crear un cliente
  Cuando:    la conexión a RabbitMQ falla
  Entonces:  se registra el error en logs
  Y:         el cliente sigue siendo creado en la base de datos (publicación desacoplada)
  Y:         se reintenta el envío del evento según política de reintentos
```

**Edge Case**
```gherkin
CRITERIO-1.3: Evento sin campos opcionales
  Dado que:  se crea un cliente sin descripción adicional
  Cuando:    la transacción se confirma
  Entonces:  el evento se publica solo con los campos obligatorios
```

---

#### HU-02: Consumir evento "cliente.creado" en MS-Banking

```
Como:        MS-Banking (Backend)
Quiero:      consumir eventos "cliente.creado" desde RabbitMQ
Para:        sincronizar una tabla local cliente_ref con la información del cliente

Prioridad:   Alta
Estimación:  M
Dependencias: HU-01
Capa:        Backend (Infrastructure Messaging)
```

#### Criterios de Aceptación — HU-02

**Happy Path**
```gherkin
CRITERIO-2.1: Evento recibido y procesado exitosamente
  Dado que:  MS-Customer emite el evento "cliente.creado"
  Cuando:    el evento llega a la cola de MS-Banking
  Entonces:  MS-Banking procesa el evento
  Y:         MS-Banking actualiza su tabla local cliente_ref con los datos
  Y:         los datos incluyen clienteId, nombre e identificacion
  Y:         el cliente está disponible para crear cuentas inmediatamente
```

**Error Path**
```gherkin
CRITERIO-2.2: Falla temporal en el procesamiento del evento
  Dado que:  MS-Banking intenta procesar un evento "cliente.creado"
  Cuando:    ocurre una excepción temporal (BD no disponible)
  Entonces:  el mensaje NO se elimina de la cola
  Y:         se reintenta el procesamiento automáticamente
  Y:         después de 3 intentos fallidos, se envía a una cola de deadletter
```

**Edge Case**
```gherkin
CRITERIO-2.3: Evento duplicado procesado idempotentemente
  Dado que:  MS-Banking ya procesó un evento "cliente.creado" anteriormente
  Cuando:    el mismo evento llega nuevamente (por reintento o desduplicación fallida)
  Entonces:  MS-Banking ejecuta un upsert sobre cliente_ref
  Y:         no hay excepción de llave duplicada
  Y:         el registro final conserva nombre e identificacion sincronizados
```

---

#### HU-03: Mantener tabla cliente_ref en MS-Banking

```
Como:        MS-Banking
Quiero:      mantener una tabla local cliente_ref sincronizada
Para:        poder validar clientes sin acceso directo a BD de MS-Customer

Prioridad:   Alta
Estimación:  M
Dependencias: HU-02
Capa:        Backend (Domain Model + Infrastructure)
```

#### Criterios de Aceptación — HU-03

**Happy Path**
```gherkin
CRITERIO-3.1: Consultar cliente_ref en MS-Banking
  Dado que:  MS-Banking tiene la tabla cliente_ref sincronizada
  Cuando:    creo una cuenta y necesito validar el cliente
  Entonces:  MS-Banking consulta su propia tabla cliente_ref
  Y:         obtiene los datos del cliente (clienteId, identificacion, nombre)
  Y:         no necesita llamar a MS-Customer directamente
  Y:         puede generar reportes locales sin depender de customer_db
```

**Error Path**
```gherkin
CRITERIO-3.2: Cliente no encontrado en cliente_ref
  Dado que:  intento crear una cuenta con un clienteId inexistente
  Cuando:    consulto la tabla cliente_ref en MS-Banking
  Entonces:  la consulta retorna null/vacío
  Y:         se lanza una AccountValidationException
  Y:         se retorna HTTP 400 Bad Request
```

**Edge Case**
```gherkin
CRITERIO-3.3: Eventual consistency - delay temporal
  Dado que:  acabo de crear un cliente en MS-Customer
  Cuando:    intento crear una cuenta en MS-Banking inmediatamente (antes de 100ms)
  Entonces:  es posible que el cliente_ref no esté sincronizado aún
  Y:         se retorna HTTP 400 con mensaje "Cliente aún no sincronizado, intente nuevamente"
  Y:         el cliente se sincroniza en los próximos segundos
```

#### HU-06: Generación autónoma de reportes en MS-Banking

```
Como:        MS-Banking
Quiero:      generar reportes usando solo su tabla local cliente_ref y cuenta
Para:        no depender de customer_db para consultas operativas o analíticas

Prioridad:   Alta
Estimación:  S
Dependencias: HU-02, HU-03
Capa:        Backend (Infrastructure + Reporting)
```

#### Criterios de Aceptación — HU-06

**Happy Path**
```gherkin
CRITERIO-6.1: Reporte generado con datos locales
  Dado que:  existe un cliente sincronizado en cliente_ref
  Cuando:    MS-Banking genera un reporte de cuentas o movimientos
  Entonces:  el reporte usa clienteId, nombre e identificacion desde cliente_ref
  Y:         no realiza consultas a customer_db
```

**Error Path**
```gherkin
CRITERIO-6.2: Reporte sin referencia sincronizada
  Dado que:  no existe aún el cliente en cliente_ref
  Cuando:    MS-Banking intenta generar el reporte
  Entonces:  el sistema responde con el dato faltante como referencia pendiente
  Y:         no intenta consultar la base de datos de MS-Customer
```

---

#### HU-04: Recuperación ante caída de MS-Banking

```
Como:        MS-Banking
Quiero:      procesar eventos pendientes cuando me recupero de una caída
Para:        no perder datos de clientes creados mientras estuve fuera de línea

Prioridad:   Alta
Estimación:  M
Dependencias: HU-02
Capa:        Backend (Infrastructure Messaging)
```

#### Criterios de Aceptación — HU-04

**Happy Path**
```gherkin
CRITERIO-4.1: Procesar eventos pendientes al recuperarse
  Dado que:  MS-Banking estuvo caído durante 5 minutos
  Cuando:    se reinicia MS-Banking
  Entonces:  el servicio consume los eventos pendientes de la cola
  Y:         actualiza cliente_ref con todos los clientes que se crearon en el interim
  Y:         no hay pérdida de datos
```

**Error Path**
```gherkin
CRITERIO-4.2: Recuperación parcial de eventos
  Dado que:  MS-Banking se recupera pero hay problemas de BD
  Cuando:    intenta procesar múltiples eventos del queue
  Entonces:  procesa los eventos exitosamente mientras pueda
  Y:         detiene momentáneamente al encontrar error
  Y:         reintentos continúan automáticamente
```

---

#### HU-05: Aislamiento de esquemas de BD entre servicios

```
Como:        Arquitecto
Quiero:      que MS-Customer y MS-Banking tengan esquemas independientes
Para:        evitar acoplamiento en la BD y permitir evolución independiente

Prioridad:   Media
Estimación:  S
Dependencias: Ninguna
Capa:        Backend (Infrastructure)
```

#### Criterios de Aceptación — HU-05

**Happy Path**
```gherkin
CRITERIO-5.1: Esquemas separados por base de datos
  Dado que:  el proyecto tiene 2 base de datos (customer_db, banking_db)
  Cuando:    MS-Customer modifica su esquema en customer_db
  Entonces:  MS-Banking no se ve afectado (usa banking_db)
  Y:         cada microservicio solo accede a su BD via su propio Repository
```

**Error Path**
```gherkin
CRITERIO-5.2: Intento de acceso directo entre servicios bloqueado
  Dado que:  MS-Banking intenta conectar directamente a customer_db
  Cuando:    se intenta ejecutar una query directa
  Entonces:  la conexión falla (no hay credenciales, no hay acceso de red)
  Y:         MS-Banking solo obtiene datos via eventos de RabbitMQ
```

---

### Reglas de Negocio

1. **Autonomía de BD**: Cada microservicio tiene su propia BD. MS-Banking NO puede consultar customer_db directamente.
2. **Eventual Consistency**: MS-Banking sincronizará cliente_ref de forma asíncrona. Puede haber un delay de hasta 5 segundos.
3. **Resiliencia**: Si RabbitMQ falla, los eventos se encolan. Cuando se recupera, se procesan sin pérdida.
4. **Idempotencia**: El mismo evento procesado múltiples veces no debe crear duplicados.
5. **Replicación de datos**: `cliente_ref` debe almacenar `clienteId`, `nombre` e `identificacion` para habilitar reportes locales autónomos.
6. **Reintentos**: Máximo 3 reintentos automáticos antes de enviar a deadletter queue.
7. **Aislamiento**: MS-Customer no conoce detalles de MS-Banking. Solo emite eventos.
8. **FKs lógicas**: No existen foreign keys físicas entre `customer_db` y `banking_db`; la relación entre ambos servicios es lógica y se materializa por `cliente_id` en eventos y tablas locales.

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades afectadas

| Entidad | Almacén | Cambios | Descripción |
|---------|---------|---------|-------------|
| `ClienteRefEntity` | tabla `cliente_ref` en banking_db | **NUEVA** | Referencia local de cliente para MS-Banking y reportes |
| `ClienteCreadoEvent` | mensaje JSON en RabbitMQ | **NUEVA** | DTO para evento de cliente creado |

#### Campos del modelo — ClienteRefEntity

| Campo | Tipo | Obligatorio | Validación | Descripción |
|-------|------|-------------|------------|-------------|
| `id` | BIGSERIAL | sí | auto-generado (BD) | Identificador único en banking_db |
| `cliente_id` | BIGINT | sí | UNIQUE | ID del cliente en customer_db (foráneo lógico) |
| `identificacion` | VARCHAR(50) | sí | NOT NULL | Identificación del cliente |
| `nombre` | VARCHAR(100) | sí | NOT NULL | Nombre completo del cliente |
| `created_at` | TIMESTAMP | sí | DEFAULT CURRENT_TIMESTAMP | Timestamp de creación |
| `updated_at` | TIMESTAMP | sí | DEFAULT CURRENT_TIMESTAMP | Timestamp de última actualización |
| `version` | INTEGER | no | DEFAULT 0 | Versión para optimistic locking |

#### Campos del modelo — ClienteCreadoEvent

```java
public record ClienteCreadoEvent(
  Long clienteId,           // ID del cliente en customer_db
  String identificacion,    // Identificación del cliente
  String nombre             // Nombre del cliente
) {}
```

#### Índices / Constraints

```sql
-- En banking_db.cliente_ref
CREATE UNIQUE INDEX idx_cliente_ref_cliente_id ON cliente_ref(cliente_id);
CREATE INDEX idx_cliente_ref_identificacion ON cliente_ref(identificacion);
```

**Justificación:**
- `cliente_id` UNIQUE: evita duplicados al procesar eventos múltiples veces
- `identificacion` INDEX: búsqueda rápida por identificación
- `nombre` se conserva replicado para reportes y consultas locales

---

### Configuración de RabbitMQ

#### Exchange y Queues

| Componente | Nombre | Tipo | Propósito |
|-----------|--------|------|----------|
| Exchange | `customer.events` | Direct | Distribuye eventos de MS-Customer |
| Queue (MS-Banking) | `cliente.creado.queue` | Durable | Consume eventos de cliente creado |
| Queue (Deadletter) | `cliente.creado.deadletter.queue` | Durable | Guarda eventos con error tras 3 reintentos |

**Política de reintentos / DLQ**
- Reintento automático para errores transitorios de persistencia en PostgreSQL, bloqueo de fila o indisponibilidad temporal.
- Máximo 3 intentos antes de enviar el mensaje a `cliente.creado.deadletter.queue`.
- Los mensajes fallidos deben conservar payload y metadatos suficientes para reprocesamiento manual.

#### Configuración de Reintentos

```yaml
# EN MS-BANKING application.yaml
spring:
  rabbitmq:
    listener:
      simple:
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000      # 1 segundo
          max-interval: 10000         # 10 segundos
          multiplier: 2.0
          retryable-exceptions:
            - org.springframework.dao.CannotAcquireLockException
            - org.springframework.dao.DataAccessResourceFailureException
            - org.springframework.amqp.AmqpConnectException
```

---

### API Endpoints

#### No hay endpoints HTTP nuevos para esta feature

El flujo es completamente **event-driven** via RabbitMQ. Los endpoints existentes (crear cliente, crear cuenta) disparan o consumen eventos internamente.

#### Eventos de Dominio (Mensajes RabbitMQ)

##### evento: cliente.creado

**Publicador:** MS-Customer (al crear cliente exitosamente)

**Consumidor:** MS-Banking (sincroniza cliente_ref)

**Estructura:**
```json
{
  "clienteId": 123,
  "identificacion": "123456789",
  "nombre": "Juan Pérez"
}
```

**Routing:**
- Exchange: `customer.events`
- Routing Key: `cliente.creado`
- Queue: `cliente.creado.queue` (en MS-Banking)

**Garantías:**
- **Durabilidad:** Queue es durable (persistida en RabbitMQ)
- **Reintentos:** Máx 3 intentos (configurados en listener)
- **Deadletter:** Mensajes fallidos van a `cliente.creado.deadletter.queue`
- **Idempotencia:** MS-Banking ejecuta upsert sobre `cliente_ref` usando `cliente_id` como clave lógica

---

### Arquitectura y Dependencias

#### Paquetes nuevos requeridos

- `com.bank.customerservice.application.dto` — DTOs para eventos (ClienteCreadoEvent)
- `com.bank.customerservice.infrastructure.messaging` — ya existe ClienteEventPublisher
- `com.bank.bankingservice.infrastructure.output.persistence` — ClienteRefEntity, ClienteRefRepositoryAdapter
- `com.bank.bankingservice.infrastructure.messaging` — ClienteCreadoEventListener (nuevo)

#### Servicios externos

- **RabbitMQ** (ya configurado en compose): host=rabbitmq, puerto=5672

#### Cambios en BaseDatos.sql

Script unificado definitivo:

```sql
SELECT 'CREATE DATABASE customer_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'customer_db')\gexec
SELECT 'CREATE DATABASE banking_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'banking_db')\gexec

\c customer_db

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE TYPE genero_enum AS ENUM ('MASCULINO', 'FEMENINO', 'OTRO');

CREATE TABLE persona (
  id BIGSERIAL PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  genero genero_enum,
  edad INT CHECK (edad >= 0),
  identificacion VARCHAR(50) NOT NULL UNIQUE,
  direccion VARCHAR(255),
  telefono VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cliente (
  id BIGSERIAL PRIMARY KEY,
  persona_id BIGINT NOT NULL UNIQUE,
  contrasena VARCHAR(255) NOT NULL,
  estado BOOLEAN DEFAULT true,
  version INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_cliente_persona
    FOREIGN KEY (persona_id) REFERENCES persona(id),
  CONSTRAINT chk_contrasena_largo
    CHECK (LENGTH(contrasena) >= 8)
);

\c banking_db

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE cliente_ref (
  id BIGSERIAL PRIMARY KEY,
  cliente_id BIGINT NOT NULL UNIQUE,
  nombre VARCHAR(100) NOT NULL,
  identificacion VARCHAR(50) NOT NULL,
  version INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cuenta (
  id BIGSERIAL PRIMARY KEY,
  numero_cuenta VARCHAR(20) NOT NULL UNIQUE,
  tipo_cuenta VARCHAR(20) NOT NULL,
  saldo_inicial DECIMAL(15,2) NOT NULL DEFAULT 0,
  saldo_disponible DECIMAL(15,2) NOT NULL DEFAULT 0,
  estado BOOLEAN DEFAULT true,
  version INTEGER NOT NULL DEFAULT 0,
  cliente_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_cuenta_cliente_ref
    FOREIGN KEY (cliente_id) REFERENCES cliente_ref(cliente_id)
);

CREATE TABLE movimiento (
  id BIGSERIAL PRIMARY KEY,
  fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tipo_movimiento VARCHAR(20) NOT NULL,
  valor DECIMAL(15,2) NOT NULL,
  saldo DECIMAL(15,2) NOT NULL,
  cuenta_id BIGINT NOT NULL,
  transaction_id VARCHAR(100),
  CONSTRAINT fk_movimiento_cuenta FOREIGN KEY (cuenta_id) REFERENCES cuenta(id),
  CONSTRAINT chk_movimiento_valor_no_cero CHECK (valor <> 0),
  CONSTRAINT chk_movimiento_tipo_valido CHECK (tipo_movimiento IN ('Depósito', 'Retiro'))
);

CREATE INDEX idx_cliente_ref_cliente_id ON cliente_ref (cliente_id);
CREATE INDEX idx_cliente_ref_identificacion ON cliente_ref (identificacion);
CREATE INDEX idx_movimiento_fecha ON movimiento (fecha);
CREATE INDEX idx_movimiento_cuenta_fecha ON movimiento (cuenta_id, fecha DESC);

CREATE UNIQUE INDEX uq_movimiento_transaction_id
  ON movimiento(transaction_id)
  WHERE transaction_id IS NOT NULL;
```

---

### Notas de Implementación

> 1. **ClienteCreadoEventListener** debe estar en MS-Banking, no en MS-Customer.
> 2. **Idempotencia**: El listener debe usar UPSERT (`INSERT ... ON CONFLICT (cliente_id) DO UPDATE`) para evitar errores con duplicados y mantener sincronizados `nombre` e `identificacion`.
> 3. **Replicación de datos**: `cliente_ref` debe almacenar `clienteId`, `nombre` e `identificacion`; no depende de consultas directas a customer_db.
> 4. **Sincronización inicial**: Si hay clientes históricos en MS-Customer, se debe ejecutar un script de migración inicial antes de activar el listener.
> 5. **Monitoreo**: Revisar regularmente las deadletter queues para detectar eventos no procesables.
> 6. **Documentación**: Especificar en README.md de cada servicio qué eventos emite y cuáles consume.
> 7. **Virtual Threads**: Usar Java 21 Virtual Threads para manejar concurrencia eficientemente.

---

## 3. LISTA DE TAREAS

### Backend — MS-Customer (Publicador)

#### Implementación

- [ ] Verificar que `ClienteCreadoEvent` existe como record en `application/dto/`
- [ ] Verificar que `ClienteEventPublisher` existe y publica a exchange `customer.events`
- [ ] Asegurar que el servicio de creación de cliente (`ClienteService.crear()`) invoca `ClienteEventPublisher.publicarClienteCreadoEvent()`
- [ ] Probar end-to-end: crear cliente → evento publicado en RabbitMQ

#### Tests Backend — MS-Customer (Matriz 3-2-1)

**ClienteEventPublisher Tests:**
- [ ] `test_publicarClienteCreadoEvent_success` — verifica que RabbitTemplate.convertAndSend es invocado correctamente
- [ ] `test_publicarClienteCreadoEvent_rabbitMQDown_logsError` — verifica manejo de excepción sin crash

**ClienteService Tests (método crear):**
- [ ] `test_crear_publicaEventoClienteCreado` — verifica que el evento se publica tras crear cliente
- [ ] `test_crear_eventPublicationFailure_noThrow` — verifica que fallo de evento no impide creación del cliente
- [ ] `test_crear_withInvalidPassword_throwsException` — caso borde de validación

---

### Backend — MS-Banking (Consumidor)

#### Implementación

- [ ] Crear `ClienteRefEntity` en `infrastructure/output/persistence/`
- [ ] Crear `SpringDataClienteRefRepository` (JPA Repository)
- [ ] Crear `ClienteRefRepositoryAdapter` con métodos: `save()`, `findByClienteId()`, `findByIdentificacion()`, `upsert()`
- [ ] Crear `ClienteCreadoEventListener` en `infrastructure/messaging/` con `@RabbitListener`
- [ ] Configurar el listener para consumir de queue `cliente.creado.queue`
- [ ] Implementar lógica UPSERT en listener (INSERT ON CONFLICT)
- [ ] Implementar manejo de excepciones (reintento automático + deadletter)
- [ ] Registrar la configuración de RabbitMQ en `RabbitMQConfig.java`
- [ ] Actualizar `BaseDatos.sql` con tabla `cliente_ref`
- [ ] Actualizar `CuentaService.validarClienteExiste()` para usar `ClienteRefRepository` en lugar de acceso directo

#### Tests Backend — MS-Banking (Matriz 3-2-1)

**ClienteCreadoEventListener Tests:**
- [ ] `test_handleClienteCreadoEvent_success` — listener procesa evento exitosamente
- [ ] `test_handleClienteCreadoEvent_idempotent` — mismo evento procesado 2x sin error
- [ ] `test_handleClienteCreadoEvent_databaseUnavailable_throwsException` — caso error con reintento

**ClienteRefRepositoryAdapter Tests:**
- [ ] `test_upsert_insertNewClienteRef` — inserta nuevo cliente_ref
- [ ] `test_upsert_updateExistingClienteRef` — actualiza cliente_ref existente

**CuentaService Tests (método validarClienteExiste):**
- [ ] `test_validarClienteExiste_success` — encuentra cliente en cliente_ref
- [ ] `test_validarClienteExiste_notFound_throwsException` — cliente no existe, lanza AccountValidationException
- [ ] `test_validarClienteExiste_clienteRefNotYetSynced_throwsException` — cliente aún no sincronizado (eventual consistency)

---

### QA

#### Gherkin Scenarios (Flujos de Integración)

- [ ] `test_end_to_end_cliente_creado_evento_consumido` — crear cliente en MS-Customer, verificar evento en queue, MS-Banking lo consume
- [ ] `test_eventual_consistency_delay` — crear cliente, esperar delay, verificar cliente_ref sincronizado
- [ ] `test_rabbitmq_down_recovery` — apagar RabbitMQ, crear cliente, encender RabbitMQ, verificar sincronización
- [ ] `test_evento_duplicado_idempotencia` — publicar mismo evento 2x, verificar sin duplicados en cliente_ref
- [ ] `test_deadletter_queue_evento_fallido` — simular error en listener, verificar evento en deadletter tras 3 reintentos

#### Plan de Pruebas de Performance (si aplica)

- [ ] Load Test: 1000 eventos/seg durante 5 minutos, verificar latencia de sincronización < 5 segundos
- [ ] Spike Test: aumento de 100 a 5000 eventos/seg, verificar recuperación
- [ ] Soak Test: 100 eventos/seg durante 24 horas, verificar estabilidad

---

### Aprobación

**Status de la Spec:** `DRAFT` → cambiar a `APPROVED` una vez validado con stakeholders

**Checklist de Aprobación:**
- [ ] Arquitecto revisó flujo de eventos
- [ ] Backend Lead revisó modelos de datos
- [ ] QA Lead revisó plan de pruebas
- [ ] Operaciones revisó configuración de RabbitMQ
