---
description: 'Ejecuta el Database Agent para diseñar esquemas de datos, generar entidades JPA y optimizar queries a partir de la spec aprobada.'
agent: Database Agent
---

Ejecuta el Database Agent para diseñar y gestionar el modelo de persistencia del feature.

**Feature**: ${input:featureName:nombre del feature en kebab-case}

**Instrucciones para @Database Agent:**

## Arquitectura Hexagonal PURA

| Capa | Paquete | Anotaciones |
|------|--------|-------------|
| **Domain** | `domain/model/` | ❌ POJO puro (sin JPA) |
| **Domain** | `domain/ports/out/` | ❌ Interfaz pura |
| **Infrastructure** | `infrastructure/output/` | ✅ `@Entity`, `@Repository` |

> 📌 Las entidades JPA van en `infrastructure/`, NO en `domain/`.

1. Lee `.github/instructions/backend.instructions.md` — confirma el motor de BD aprobada
2. Lee `.github/docs/lineamientos/dev-guidelines.md`
3. Lee la **Sección 2 — DISEÑO — Modelos de Datos** de `.github/specs/${input:featureName}.spec.md`
4. Escanea entidades existentes en `src/main/java/com/example/<service>/infrastructure/output/`
5. Ejecuta el flujo completo:
   - Diseña o actualiza el esquema en BaseDatos.sql (fuente de verdad)
   - Genera puerto de salida: `domain/ports/out/<Feature>RepositoryPort.java`
   - Genera entidad JPA: `infrastructure/output/<Feature>Entity.java`
   - Genera repositorio JPA: `infrastructure/output/<Feature>JpaRepository.java`
   - Genera adaptador: `infrastructure/output/<Feature>RepositoryAdapter.java`
   - Genera配置 wiring: `infrastructure/config/ApplicationConfig.java`
6. Presenta reporte consolidado de cambios al modelo de datos

**Prerequisito:** Debe existir `.github/specs/${input:featureName}.spec.md` con estado APPROVED y Sección 2 completa. Si no, ejecutar `/generate-spec` primero.

**Nota:** Ejecutar ANTES o en paralelo con el Backend Developer para que los contratos de persistencia estén definidos antes de implementar.