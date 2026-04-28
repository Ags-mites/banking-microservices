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

## Pasos obligatorios:

1. **Lee la spec** en `.github/specs/${input:featureName:nombre-feature}.spec.md` — si no existe, detente e informa al usuario.
2. **Revisa el código existente** en `src/main/java/com/example/<service>/` para entender patrones actuales.
3. **Implementa en orden** (Arquitectura Hexagonal):
   - `domain/model/` — entidades JPA
   - `domain/ports/in/` — interfaces de casos de uso
   - `domain/ports/out/` — interfaces de repositorio
   - `domain/service/` — casos de uso
   - `application/dto/` — DTOs request/response
   - `application/mapper/` — MapStruct mappers
   - `infrastructure/output/` — repositorio JPA
   - `infrastructure/input/` — controlador REST
4. **Verifica sintaxis** ejecutando: `./mvnw compile`

## Restricciones:
- Seguir arquitectura hexagonal con puertos y adaptadores.
- Usar constructor injection (no @Autowired).
- Todas las entidades deben tener @Version y timestamps (created_at, updated_at).
- Mapeo a BaseDatos.sql (fuente de verdad).