# HU-006: Desacoplamiento y Comunicación Asíncrona (Seniority)

## Título

Desacoplamiento y Comunicación Asíncrona

## Historia de Usuario

Como arquitecto, quiero que los servicios se comuniquen vía RabbitMQ, para asegurar la resiliencia.

## Criterios de Aceptación

#### Scenario: MS-Customer emite evento al crear cliente
**Given** un cliente es creado exitosamente en MS-Customer
**When** la transacción de creación se confirma
**Then** el sistema emite un evento "cliente.creado" a RabbitMQ
**And** el evento contiene: clienteId, identificación, nombre

#### Scenario: MS-Banking consume evento de cliente creado
**Given** MS-Customer emite el evento "cliente.creado"
**When** el evento llega a la cola de MS-Banking
**Then** MS-Banking procesa el evento
**And** actualiza su tabla local de cliente_ref con los datos del cliente

#### Scenario: Eventual consistency entre servicios
**Given** se crea un cliente en MS-Customer
**When** la creación es exitosa
**Then** MS-Banking recibe el evento de forma asíncrona
**And** puede haber un pequeño delay antes de que MS-Banking tenga la referencia

#### Scenario: MS-Banking no tiene acceso a DB de MS-Customer
**Given** MS-Banking necesita información del cliente
**When** MS-Banking intenta acceder a la base de datos de MS-Customer
**Then** la conexión no existe
**And** MS-Banking obtiene los datos solo via eventos de RabbitMQ

#### Scenario: MS-Banking consulta cliente_ref local
**Given** MS-Banking tiene la tabla cliente_ref sincronizada
**When** creo una cuenta y necesito validar el cliente
**Then** MS-Banking consulta su propia tabla cliente_ref
**And** no necesita llamar a MS-Customer directamente

#### Scenario: Evento recibido cuando MS-Banking no está disponible
**Given** MS-Banking está caído
**When** MS-Customer emite un evento de cliente creado
**Then** el mensaje queda en la cola de RabbitMQ
**And** cuando MS-Banking se recupere, procesa el evento

#### Scenario: Patrón Repository en MS-Customer
**Given** la arquitectura requiere patrón Repository
**When** necesito persistir un cliente
**Then** el servicio usa un repositorio abstracto
**And** la implementación concreta está aislada

#### Scenario: Patrón Ports & Adapters
**Given** se necesita abstraer la comunicación con RabbitMQ
**When** implemento el patrón Ports & Adapters
**Then** defino un puerto (interfaz) para emitir eventos
**And** implemento un adaptador concreto para RabbitMQ

#### Scenario: Reintento de procesamiento de evento
**Given** falla el procesamiento de un evento en MS-Banking
**When** ocurre un error temporal
**Then** el sistema reintenta el procesamiento
**And** después de varios intentos fallidos, el mensaje puede deadletter

#### Scenario: Aislamiento de bases de datos
**Given** cada microservicio tiene su propia base de datos
**When** MS-Customer modifica su esquema
**Then** MS-Banking no se ve afectado
**And** los cambios en MS-Banking no impactan a MS-Customer