# HU-002: Administración de Cuentas Bancarias

## Título

Administración de Cuentas Bancarias

## Historia de Usuario

Como administrador del sistema, quiero gestionar las cuentas de los clientes, para habilitar sus productos financieros.

## Criterios de Aceptación

#### Scenario: Crear una cuenta con todos los campos
**Given** el sistema requiere una nueva cuenta
**When** proporciono número de cuenta, tipo de cuenta, saldo inicial y estado
**Then** la entidad cuenta se crea con todos los campos especificados
**And** el sistema genera un identificador único para la cuenta

#### Scenario: El número de cuenta debe ser único
**Given** una cuenta con número "123456" ya existe
**When** intento crear otra cuenta con el mismo número
**Then** el sistema retorna un error de validación
**And** el mensaje de error indica que el número de cuenta ya está en uso

#### Scenario: Tipos de cuenta válidos
**Given** el sistema permite tipos de cuenta "Ahorros" y "Corriente"
**When** creo una cuenta con tipo "Ahorros"
**Then** la cuenta se crea exitosamente
**And** cuando creo otra cuenta con tipo "Corriente"
**Then** también se crea exitosamente

#### Scenario: Tipos de cuenta inválidos
**Given** el sistema solo acepta tipos "Ahorros" o "Corriente"
**When** intento crear una cuenta con tipo "Invalido"
**Then** el sistema retorna un error de validación
**And** el mensaje indica que el tipo de cuenta no es válido

#### Scenario: La cuenta se asocia a un cliente
**Given** un cliente con clienteId "1" existe en el sistema
**When** creo una cuenta vinculada al cliente "1"
**Then** la cuenta se crea exitosamente
**And** la cuenta tiene un campo cliente_ref que referencia al cliente

#### Scenario: GET /cuentas retorna todas las cuentas
**Given** existen cuentas en el sistema
**When** envío una solicitud GET a `/cuentas`
**Then** el sistema retorna una lista de todas las cuentas
**And** cada cuenta incluye número de cuenta, tipo, saldo y estado

#### Scenario: GET /cuentas/{id} retorna una cuenta específica
**Given** una cuenta con id "1" existe
**When** envío una solicitud GET a `/cuentas/1`
**Then** el sistema retorna la cuenta con todos sus datos

#### Scenario: POST /cuentas crea una nueva cuenta
**Given** se proporciona datos válidos de cuenta
**When** envío una solicitud POST a `/cuentas` con la información
**Then** el sistema crea una nueva cuenta
**And** retorna la cuenta creada con estado 201

#### Scenario: PUT /cuentas/{id} actualiza una cuenta existente
**Given** una cuenta con id "1" existe
**When** envío una solicitud PUT a `/cuentas/1` con datos actualizados
**Then** el sistema actualiza la información de la cuenta
**And** retorna la cuenta actualizada

#### Scenario: El número de cuenta no es editable
**Given** una cuenta con número "123456" existe
**When** intento actualizar el número de cuenta a "789012"
**Then** el sistema ignora el cambio de número de cuenta
**And** el número de cuenta permanece como "123456"

#### Scenario: PATCH /cuentas/{id} actualiza parcialmente una cuenta
**Given** una cuenta con id "1" existe
**When** envío una solicitud PATCH a `/cuentas/1` con datos parciales
**Then** el sistema actualiza solo los campos proporcionados
**And** preserva los campos sin cambios

#### Scenario: DELETE /cuentas/{id} elimina una cuenta
**Given** una cuenta con id "1" existe
**When** envío una solicitud DELETE a `/cuentas/1`
**Then** el sistema elimina la cuenta
**And** retorna una confirmación de éxito

#### Scenario: Actualizar cliente_ref mediante evento
**Given** se recibe un evento de cliente creado
**When** el sistema procesa el evento "cliente.creado"
**Then** las cuentas pendientes se actualizan con la referencia del cliente
**And** la cuenta queda vinculada al cliente correcto