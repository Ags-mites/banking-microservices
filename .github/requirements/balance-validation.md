# HU-004: Validación Estricta de Saldo Disponible

## Título

Validación Estricta de Saldo Disponible

## Historia de Usuario

Como sistema de seguridad, quiero validar fondos antes de un retiro, para evitar sobregiros.

## Criterios de Aceptación

#### Scenario: Validar saldo suficiente para retiro
**Given** una cuenta tiene saldo de 500.00
**When** realizo un retiro de 300.00
**Then** la operación es permitida
**And** el resultado es: (500 + (-300)) = 200 >= 0

#### Scenario: Validar saldo insuficiente para retiro
**Given** una cuenta tiene saldo de 200.00
**When** realizo un retiro de 300.00
**Then** la operación es rechazada
**And** el cálculo es: (200 + (-300)) = -100 < 0

#### Scenario: Lanzar excepción personalizada
**Given** una cuenta tiene saldo insuficiente
**When** intento realizar un retiro que excede el saldo
**Then** el sistema lanza una excepción personalizada
**And** el mensaje de error es "Saldo no disponible"

#### Scenario: Validación en dominio, no en controller
**Given** una solicitud de retiro llega al controller
**When** el controller llama al servicio de dominio
**Then** la validación de saldo se ejecuta en la capa de dominio
**And** el controller no contiene lógica de validación de saldo

#### Scenario: Validación en dominio, no en repositorio
**Given** el dominio decide rechazar una transacción
**When** la orden llega al repositorio
**Then** el repositorio no valida el saldo
**And** solo ejecuta la operación de persistencia

#### Scenario: Bloqueo optimista para evitar sobregiro
**Given** dos usuarios intentan retirar simultáneamente
**When** ambos retries son procesados al mismo tiempo
**Then** el primer retiro se completa
**And** el segundo retry falla por conflicto de versión
**And** la cuenta no queda en negativo

#### Scenario: Campo @Version en cuenta
**Given** una cuenta existe en la base de datos
**When** consulto la entidad cuenta
**Then** la entidad tiene un campo @Version
**And** este campo se incrementa con cada actualización

#### Scenario: Actualización exitosa con versión correcta
**Given** una cuenta tiene version "1"
**When** realizo un retiro válido
**Then** el movimiento se crea
**And** la versión de la cuenta se actualiza a "2"

#### Scenario: Actualización fallida por versión obsoleta
**Given** una cuenta tiene version "1"
**When** otro proceso ya actualizó la cuenta (version "2")
**Then** el sistema lanza OptimisticLockException
**And** el movimiento no es registrado