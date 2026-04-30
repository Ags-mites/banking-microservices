# Banking Microservices

Este proyecto es una prueba de concepto para un sistema bancario usando arquitectura de microservicios con Java 21, Spring Boot y RabbitMQ.

## Requisitos Previos

- Java 21
- Maven
- Docker y Docker Compose (Para la base de datos PostgreSQL y RabbitMQ)
- IDE a tu elección (IntelliJ IDEA, VSCode, Eclipse)

## Arquitectura

El sistema está compuesto por dos microservicios bajo el diseño de Clean Architecture (Arquitectura Hexagonal):

1. **Customer Service**: Encargado de la gestión de personas y clientes. Expone los endpoints de clientes (`/api/clientes`).
2. **Banking Service**: Encargado de las transacciones (depósitos/retiros) y cuentas. Expone cuentas (`/api/cuentas`) y movimientos (`/api/movimientos`), así como los reportes (`/api/reportes`).

Ambos servicios se actualizan de manera asíncrona (sincronía eventual) a través de eventos en **RabbitMQ**, garantizando alta disponibilidad.

## Cómo Iniciar

### 1. Iniciar Infraestructura (Bases de datos y RabbitMQ)
En la raíz del proyecto, inicializa los contenedores (asegúrate de que los puertos 5432 y 5672 estén libres en tu máquina local):

```bash
docker-compose up -d
```

### 2. Inicializar Datos
El archivo `BaseDatos.sql` (en la raíz del proyecto) debe ejecutarse dentro de PostgreSQL para estructurar la definición de todas las tablas e índices. También puedes usar un cliente como DBeaver, conectándote a `localhost:5432` con las credenciales que se definen en el `docker-compose.yml`.

### 3. Levantar Microservicios

Abre dos terminales diferentes y ejecuta cada microservicio:

**Terminal 1:**
```bash
cd customerservice
mvn spring-boot:run
```

**Terminal 2:**
```bash
cd bankingservice
mvn spring-boot:run
```

Una vez que ambos estén ejecutándose, `customerservice` usará un puerto (ej: `8081` si se configuró así, o un puerto específico de servidor) y `bankingservice` generalmente el `8080`. Comprueba tu `application.yaml` para el puerto local exacto de cada uno. *(Nota: Por defecto, ambos podrían intentar cargar en 8080, en esos casos puedes asignar port=8082 al Customer)*

## Como Usar y Probar

A continuación se muestra el orden sugerido de operaciones mediante `curl`. Asegúrate de reemplazar `[PUERTO_CUSTOMER]` y `[PUERTO_BANKING]` por los puertos activos de tus microservicios.

**Paso 1: Crear un Cliente (Customer Service)**
```bash
curl -X POST "http://localhost:[PUERTO_CUSTOMER]/api/clientes" \
-H "Content-Type: application/json" \
-d '{
  "nombre": "Jose Lema",
  "genero": "MASCULINO",
  "edad": 30,
  "identificacion": "1234567890",
  "direccion": "Otavalo sn y principal",
  "telefono": "098254785",
  "contrasena": "Secreta1234",
  "estado": true
}'
```

**Paso 2: Crear una Cuenta (Banking Service)**
Una vez creado el cliente, el Bankingservice recibirá su evento para la sincronización a través de RabbitMQ.
```bash
curl -X POST "http://localhost:[PUERTO_BANKING]/api/cuentas" \
-H "Content-Type: application/json" \
-d '{
  "clienteId": 1,
  "numeroCuenta": "478758",
  "tipoCuenta": "Ahorros",
  "saldoInicial": 2000,
  "estado": true
}'
```

**Paso 3: Realizar un Depósito o Retiro (Banking Service)**
```bash
curl -X POST "http://localhost:[PUERTO_BANKING]/api/movimientos" \
-H "Content-Type: application/json" \
-d '{
  "cuentaId": 1,
  "tipo": "Depósito",
  "valor": 100
}'
```
> *Si envías un valor mayor al saldo y usas "Retiro", el sistema de validación devolverá una excepción de fondos insuficientes.*

**Paso 4: Ver Reportes por Fecha**
```bash
curl -X GET "http://localhost:[PUERTO_BANKING]/api/reportes?fecha=2026-04-30&clienteId=1"
```

## Pruebas
Para ejecutar las pruebas en cada microservicio de manera unificada desde el root o en la carpeta específica de cada uno, puedes usar:
```bash
cd customerservice
mvn test
```
```bash
cd bankingservice
mvn test
```
