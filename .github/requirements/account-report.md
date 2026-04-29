# HU-005: Reporte de Estado de Cuenta

## Título

Reporte de Estado de Cuenta

## Historia de Usuario

Como cliente, quiero ver mis movimientos por rango de fechas, para auditar mis finanzas.

## Criterios de Aceptación

#### Scenario: Obtener reporte de estado de cuenta
**Given** un cliente con id "1" existe y tiene cuentas asociadas
**When** realizo una solicitud GET a `/reportes?fecha=2022-01-01,2022-12-31&cliente=1`
**Then** el sistema retorna un reporte con las cuentas del cliente
**And** cada cuenta incluye sus movimientos en el rango de fechas

#### Scenario: Estructura JSON del reporte
**Given** un cliente tiene una cuenta con movimientos
**When** consulto el reporte de estado de cuenta
**Then** la respuesta tiene la siguiente estructura:
```
{
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
}
```

#### Scenario: Reporte con múltiples cuentas
**Given** un cliente tiene varias cuentas
**When** consulto el reporte de estado de cuenta
**Then** el reporte incluye todas las cuentas del cliente
**And** cada cuenta muestra sus movimientos independientes

#### Scenario: Reporte sin movimientos en rango de fechas
**Given** un cliente tiene cuentas pero no hay movimientos en el rango
**When** consulto el reporte con rango de fechas sin movimientos
**Then** las cuentas se incluyen con lista de movimientos vacía
**And** el saldo de las cuentas es el saldo actual

#### Scenario: Reporte filtrado por rango de fechas
**Given** existen movimientos en diferentes fechas
**When** especifico un rango de fechas específico
**Then** el reporte solo incluye movimientos dentro del rango
**And** los movimientos fuera del rango no aparecen

#### Scenario: Reporte sin filtro de cliente
**Given** no se especifica el parámetro cliente
**When** realizo la solicitud GET a `/reportes?fecha=inicio,fin`
**Then** el sistema retorna un error de validación
**And** el mensaje indica que el parámetro cliente es obligatorio

#### Scenario: Reporte sin filtro de fechas
**Given** no se especifica el parámetro de fecha
**When** realizo la solicitud GET a `/reportes?cliente=1`
**Then** el sistema retorna un error de validación
**And** el mensaje indica que el parámetro de fecha es obligatorio

#### Scenario: Formato de fecha en consulta
**Given** el sistema acepta fechas en formato "yyyy-MM-dd"
**When** realizo una consulta con fecha "2022-01-01,2022-12-31"
**Then** el sistema parsea correctamente las fechas
**And** retorna los movimientos en ese rango