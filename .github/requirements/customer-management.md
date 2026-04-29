# HU-001: Gestión Integral de Clientes y Personas

## Título

Gestión Integral de Clientes y Personas

## Historia de Usuario

Como administrador del sistema, quiero realizar el CRUD de clientes, para mantener un registro de los usuarios del banco.

## Criterios de Aceptación

#### Scenario: Crear una persona con todos los campos
**Given** que el sistema requiere una nueva persona
**When** proporciono nombre, género, edad, identificación, dirección y teléfono
**Then** la entidad persona se crea con todos los campos especificados
**And** el sistema genera un identificador único para la persona

#### Scenario: La identificación de persona debe ser única
**Given** una persona con identificación "12345678" ya existe
**When** intento crear otra persona con la misma identificación
**Then** el sistema retorna un error de validación
**And** el mensaje de error indica que la identificación ya está en uso

#### Scenario: Cliente hereda de Persona
**Given** una persona existe en el sistema
**When** creo un cliente asociado a esa persona
**Then** el cliente hereda todos los campos de Persona
**And** el cliente incluye campos adicionales: clienteId (identificador de negocio único), password y estado

#### Scenario: Crear cliente con campos de negocio
**Given** una persona con id "1" existe
**When** creo un cliente con password "secreto123" y estado "true"
**Then** el cliente se crea exitosamente
**And** el sistema genera automáticamente un clienteId único
**And** el cliente está vinculado a la persona con id "1"

#### Scenario: La password no debe ser expuesta en las respuestas
**Given** un cliente existe en el sistema
**When** consulto el cliente via GET /clientes/{id}
**Then** la respuesta no incluye el campo password
**And** solo retorna datos de persona, clienteId y estado

#### Scenario: GET /clientes retorna todos los clientes
**Given** existen clientes en el sistema
**When** envío una solicitud GET a `/clientes`
**Then** el sistema retorna una lista de todos los clientes
**And** cada cliente incluye datos de persona, clienteId y estado (sin password)

#### Scenario: GET /clientes/{id} retorna un cliente específico
**Given** un cliente con id "1" existe
**When** envío una solicitud GET a `/clientes/1`
**Then** el sistema retorna el cliente con todos sus datos de persona
**And** el campo password no es incluido en la respuesta

#### Scenario: POST /clientes crea un nuevo cliente
**Given** se proporciona datos válidos de cliente (datos de persona + password + estado)
**When** envío una solicitud POST a `/clientes` con la información del cliente
**Then** el sistema crea un nuevo cliente
**And** genera automáticamente un clienteId único
**And** retorna el cliente creado con estado 201 (sin password)

#### Scenario: PUT /clientes/{id} actualiza un cliente existente
**Given** un cliente con id "1" existe
**When** envío una solicitud PUT a `/clientes/1` con datos actualizados
**Then** el sistema actualiza la información del cliente
**And** retorna el cliente actualizado (sin password)

#### Scenario: PATCH /clientes/{id} actualiza parcialmente un cliente
**Given** un cliente con id "1" existe
**When** envío una solicitud PATCH a `/clientes/1` con datos parciales
**Then** el sistema actualiza solo los campos proporcionados
**And** preserva los campos sin cambios

#### Scenario: DELETE /clientes/{id} elimina un cliente
**Given** un cliente con id "1" existe
**When** envío una solicitud DELETE a `/clientes/1`
**Then** el sistema elimina el cliente
**And** retorna una confirmación de éxito

#### Scenario: Emitir evento cuando el cliente es creado
**Given** un cliente válido es creado vía POST
**When** el cliente se persiste exitosamente
**Then** el sistema emite un evento "cliente.creado" a RabbitMQ
**And** el evento contiene el clienteId y datos relevantes

#### Scenario: Evento no emitido en creación fallida
**Given** se envía un payload de cliente inválido
**When** la creación falla debido a errores de validación
**Then** ningún evento es emitido a RabbitMQ
**And** el sistema retorna la respuesta de error correspondiente