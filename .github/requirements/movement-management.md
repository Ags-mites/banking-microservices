# HU-003: Registro de Movimientos y Saldo Real

## Título

Registro de Movimientos y Saldo Real

## Historia de Usuario

Como sistema bancario, quiero registrar transacciones, para mantener el historial y actualizar el saldo.

## Criterios de Aceptación

#### Scenario: Registrar un depósito
**Given** una cuenta con saldo de 1000.00 existe
**When** registro un movimiento de tipo "Depósito" con valor 500.00
**Then** el movimiento se crea exitosamente
**And** el saldo de la cuenta se actualiza a 1500.00
**And** la fecha del movimiento se registra automáticamente

#### Scenario: Registrar un retiro
**Given** una cuenta con saldo de 1000.00 existe
**When** registro un movimiento de tipo "Retiro" con valor 300.00
**Then** el movimiento se crea exitosamente
**And** el saldo de la cuenta se actualiza a 700.00
**And** el valor se registra como negativo internamente

#### Scenario: Depósito incrementa el saldo
**Given** una cuenta tiene saldo de 500.00
**When** realizo un depósito de 200.00
**Then** el saldo final es 700.00
**And** el cálculo es: saldo anterior + valor del movimiento

#### Scenario: Retiro decrementa el saldo
**Given** una cuenta tiene saldo de 500.00
**When** realizo un retiro de 200.00
**Then** el saldo final es 300.00
**And** el cálculo es: saldo anterior - valor del movimiento

#### Scenario: Saldo insuficiente para retiro
**Given** una cuenta tiene saldo de 100.00
**When** intento realizar un retiro de 150.00
**Then** el sistema retorna un error de validación
**And** el mensaje de error indica "Saldo no disponible"
**And** el saldo de la cuenta permanece en 100.00
**And** ningún movimiento es registrado

#### Scenario: GET /movimientos por cuenta
**Given** una cuenta con id "1" tiene movimientos asociados
**When** envío una solicitud GET a `/movimientos?cuentaId=1`
**Then** el sistema retorna solo los movimientos de esa cuenta
**And** los movimientos están ordenados por fecha descendente

#### Scenario: POST /movimientos crea un nuevo movimiento
**Given** una cuenta válida existe con saldo suficiente
**When** envío una solicitud POST a `/movimientos` con tipo, valor y cuenta
**Then** el sistema crea el movimiento
**And** actualiza el saldo de la cuenta automáticamente
**And** retorna el movimiento creado con estado 201

#### Scenario: La fecha se registra automáticamente
**Given** una cuenta existe
**When** creo un movimiento sin especificar fecha
**Then** el sistema asigna la fecha y hora actual
**And** la fecha no puede ser modificada por el usuario

#### Scenario: Movimiento con valor cero
**Given** una cuenta existe
**When** intento crear un movimiento con valor 0
**Then** el sistema retorna un error de validación
**And** el mensaje indica que el valor debe ser mayor a cero

#### Scenario: Movimiento con valor negativo
**Given** una cuenta existe
**When** intento crear un movimiento con valor negativo
**Then** el sistema retorna un error de validación
**And** el mensaje indica que el valor debe ser positivo

#### Scenario: Formato de respuesta del movimiento
**Given** un movimiento fue registrado exitosamente
**When** consulto el movimiento vía GET
**Then** la respuesta incluye los campos: Fecha, Cliente, Numero Cuenta, Tipo, Saldo Inicial, Estado, Movimiento, Saldo Disponible
**And** el formato coincide con el ejemplo:
```
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

#### Scenario: Registrar retiro de 575 en cuenta de ahorro
**Given** Jose Lema tiene una cuenta de ahorro con número "478758" y saldo de 2000
**When** realizo un retiro de 575
**Then** el saldo de la cuenta se actualiza a 1425
**And** el movimiento queda registrado con valor -575

#### Scenario: Registrar depósito de 600 en cuenta corriente
**Given** Marianela Montalvo tiene una cuenta corriente con número "225487" y saldo de 100
**When** realizo un depósito de 600
**Then** el saldo de la cuenta se actualiza a 700
**And** el movimiento queda registrado con valor 600

#### Scenario: Registrar depósito de 150 en cuenta de ahorros
**Given** Marianela Montalvo tiene una cuenta de ahorros con número "495878" y saldo de 0
**When** realizo un depósito de 150
**Then** el saldo de la cuenta se actualiza a 150
**And** el movimiento queda registrado con valor 150

#### Scenario: Listado de movimientos por cliente y rango de fechas
**Given** existen movimientos registrados en el sistema
**When** consulto los movimientos filtrando por cliente y rango de fechas
**Then** el sistema retorna los movimientos en el formato JSON especificado
**And** incluye: Fecha, Cliente, Numero Cuenta, Tipo, Saldo Inicial, Estado, Movimiento, Saldo Disponible