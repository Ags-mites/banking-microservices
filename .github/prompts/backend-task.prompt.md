---
name: backend-task
description: Implementa una funcionalidad en el backend Spring Boot basada en una spec ASDD aprobada.
argument-hint: "<nombre-feature> (debe existir .github/specs/<nombre-feature>.spec.md)"
agent: Backend Developer
tools:
  - edit/createFile
  - edit/editFiles
  - read/readFile
  - search/listDirectory
  - search
  - execute/runInTerminal
---

Implementa el backend para el feature especificado, siguiendo la spec aprobada.

**Feature**: ${input:featureName:nombre del feature en kebab-case}

## Arquitectura Hexagonal PURA

| Capa | Paquete | Anotaciones |
|------|--------|-------------|
| **Domain** | `domain/model/` | ❌ Ninguna (100% POJO) |
| **Domain** | `domain/ports/` | ❌ Ninguna (interfaces) |
| **Application** | `application/usecase/` | ✅ `@Service` (wiring) |
| **Application** | `application/dto/` | ❌ Ninguna (Records) |
| **Infrastructure** | `infrastructure/output/` | ✅ `@Entity`, `@Repository` |
| **Infrastructure** | `infrastructure/input/` | ✅ `@RestController` |

> 📌 **REGLA**: Domain = Java PURO. Las anotaciones de framework van exclusivamente en Infrastructure.

## Pasos obligatorios:

1. **Lee la spec** en `.github/specs/${input:featureName:nombre-feature}.spec.md` — si no existe, detente e informa al usuario.
2. **Revisa el código existente** en `src/main/java/com/example/<service>/` para entender patrones actuales.
3. **Implementa en orden** (Arquitectura Hexagonal):
   - `domain/model/` — Entity + Value Objects (POJO puro)
   - `domain/ports/in/` — interfaces de casos de uso
   - `domain/ports/out/` — interfaces de repositorio
   - `application/usecase/` — casos de uso (@Service)
   - `application/dto/` — DTOs request/response (Records)
   - `infrastructure/output/` — Entity JPA + Repository + Adapter
   - `infrastructure/input/` — controlador REST
   - `infrastructure/config/` — wiring (@Configuration)
4. **Verifica sintaxis** ejecutando: `./mvnw compile`

## Restricciones:
- Domain = Java PURO (sin anotaciones de framework)
- @Service va en `application/usecase/`, NO en `domain/`
- Entidades JPA van en `infrastructure/output/`, NO en `domain/`
- Usar constructor injection (no @Autowired).
- Todas las entidades deben tener @Version y timestamps (created_at, updated_at).
- Mapeo a BaseDatos.sql (fuente de verdad).