---
name: generate-tests
description: Genera pruebas unitarias para backend Java basadas en la spec ASDD y el código implementado.
argument-hint: "<nombre-feature>"
agent: Orchestrator
tools:
  - edit/createFile
  - edit/editFiles
  - read/readFile
  - search/listDirectory
  - search
  - execute/runInTerminal
---

Genera pruebas unitarias completas para el feature especificado.

**Feature**: ${input:featureName:nombre del feature en kebab-case}

## Pasos obligatorios:

1. **Lee la spec** en `.github/specs/${input:featureName:nombre-feature}.spec.md`.
2. **Delega** a `Test Engineer Backend`:
   - Tests de servicio (lógica de negocio)
   - Tests de controller (endpoints)
   - Tests de repositorio (adaptador)
3. **Verifica** que los tests corren:
   - Backend: `./mvnw test`

## Cobertura obligatoria por test:
- ✅ Happy path (flujo exitoso)
- ❌ Error path (excepciones, errores de red, datos inválidos)
- 🔲 Edge cases (campos vacíos, duplicados, permisos)

## Restricciones:
- Cada test debe ser independiente (no compartir estado).
- Mockear SIEMPRE las dependencias externas (DB, APIs).
- Usar JUnit 5 + Mockito.
- Cobertura mínima ≥ 80%.