-- ============================================
-- Script de Base de Datos - Banking Microservices
-- ============================================

-- Crear bases de datos
SELECT 'CREATE DATABASE customer_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'customer_db')\gexec
SELECT 'CREATE DATABASE banking_db' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'banking_db')\gexec

-- ============================================
-- customer_db: Persona, Cliente
-- ============================================
\c customer_db

CREATE TYPE genero_enum AS ENUM ('MASCULINO', 'FEMENINO', 'OTRO');

CREATE TABLE persona (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    genero genero_enum,
    edad INT CHECK (edad >= 0),
    identificacion VARCHAR(50) NOT NULL UNIQUE,
    direccion VARCHAR(255),
    telefono VARCHAR(20)
);

CREATE TABLE cliente (
    id BIGSERIAL PRIMARY KEY,
    persona_id BIGINT NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    estado BOOLEAN DEFAULT true,
    CONSTRAINT fk_cliente_persona 
        FOREIGN KEY (persona_id) REFERENCES persona(id)
    CONSTRAINT chk_contrasena_largo 
        CHECK (LENGTH(contrasena) >= 8)
);

-- ============================================
-- banking_db: Cuenta, Movimiento
-- ============================================
\c banking_db

CREATE TABLE cuenta (
    id BIGSERIAL PRIMARY KEY,
    numero_cuenta VARCHAR(20) NOT NULL UNIQUE,
    tipo_cuenta VARCHAR(20) NOT NULL,
    saldo_inicial DECIMAL(15,2) NOT NULL DEFAULT 0,
    saldo_disponible DECIMAL(15,2) NOT NULL DEFAULT 0,
    estado BOOLEAN DEFAULT true,
    cliente_id BIGINT NOT NULL
);

CREATE TABLE movimiento (
    id BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento VARCHAR(20) NOT NULL,
    valor DECIMAL(15,2) NOT NULL,
    saldo DECIMAL(15,2) NOT NULL,
    cuenta_id BIGINT NOT NULL,
    CONSTRAINT fk_movimiento_cuenta FOREIGN KEY (cuenta_id) REFERENCES cuenta(id)
);