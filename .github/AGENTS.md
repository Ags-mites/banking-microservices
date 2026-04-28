# AGENTS.md — ASDD Project

> Canonical shared version: this file is the source of truth for shared agent guidelines.

This file defines general guidance for all AI agents working in this repository, following the **ASDD (Agent Spec Software Development)** workflow.

## Project Summary

> Ver `README.md` en la raíz del proyecto para stack, arquitectura y estructura de carpetas del proyecto actual.
> Ver `.github/README.md` para la estructura completa del framework ASDD.

## ASDD Workflow

**Every new feature must follow this pipeline:**

```
[FASE 1 — Secuencial]
[[agents/spec-generator.agent.md]]    → [[skills/generate-spec/SKILL.md]]      → [[specs/<feature>.spec.md]]

[FASE 2 — Paralelo ∥]
[[agents/database.agent.md]]    → modelos, migrations, seeders  (si hay cambios de DB)
[[agents/backend-developer.agent.md]] → capas del proyecto (routes/services/repos/models)
[[agents/frontend-developer.agent.md]]→ páginas / componentes / hooks / servicios

[FASE 3 — Paralelo ∥]
[[agents/test-engineer-backend.agent.md]]  → src/test/java/
[[agents/test-engineer-frontend.agent.md]] → frontend/src/__tests__/

[FASE 4 — Secuencial]
[[agents/qa.agent.md]]          → [[skills/gherkin-case-generator/SKILL.md]], [[skills/risk-identifier/SKILL.md]], …

[FASE 5 — Opcional]
[[agents/documentation.agent.md]] → README, API docs, ADRs
```

## Agent Skills (slash commands)

Skills are portable instruction sets invokable as `/command` in Copilot Chat. They work across VS Code, GitHub Copilot CLI, and Copilot coding agent.

### ASDD Core
| Skill | Slash Command | Descripción |
|-------|---------------|-------------|
| [[skills/asdd-orchestrate/SKILL.md]] | `/asdd-orchestrate` | Orquesta el flujo completo ASDD o consulta estado |
| [[skills/generate-spec/SKILL.md]] | `/generate-spec` | Genera spec técnica en [[specs/]] |
| [[skills/implement-backend/SKILL.md]] | `/implement-backend` | Implementa feature completo en el backend |
| [[skills/implement-frontend/SKILL.md]] | `/implement-frontend` | Implementa feature completo en el frontend |
| [[skills/unit-testing/SKILL.md]] | `/unit-testing` | Genera suite de tests (backend + frontend) |

### QA
| Skill | Slash Command | Descripción |
|-------|---------------|-------------|
| [[skills/gherkin-case-generator/SKILL.md]] | `/gherkin-case-generator` | Genera casos Given-When-Then + datos de prueba |
| [[skills/risk-identifier/SKILL.md]] | `/risk-identifier` | Clasifica riesgos con Regla ASD (Alto/Medio/Bajo) |
| [[skills/automation-flow-proposer/SKILL.md]] | `/automation-flow-proposer` | Propone flujos a automatizar y framework |
| [[skills/performance-analyzer/SKILL.md]] | `/performance-analyzer` | Planifica y analiza pruebas de performance |

## Lineamientos y Contexto

Los agentes deben cargar estos archivos como **primer paso** antes de generar cualquier código:

| Documento | Ruta | Agentes que lo cargan |
|---|---|---|
| Lineamientos de Desarrollo | [[docs/lineamientos/dev-guidelines.md]] | [[agents/backend-developer.agent.md]], [[agents/frontend-developer.agent.md]], [[agents/database.agent.md]] |
| Lineamientos QA | [[docs/lineamientos/qa-guidelines.md]] | [[agents/test-engineer-backend.agent.md]], [[agents/test-engineer-frontend.agent.md]], [[agents/qa.agent.md]] |
| Reglas de Oro | [[AGENTS.md]] | Todos (siempre activas) |
| Definition of Done | [[copilot-instructions.md]] | [[agents/test-engineer-backend.agent.md]], [[agents/test-engineer-frontend.agent.md]], [[agents/qa.agent.md]], [[agents/orchestrator.agent.md]] |
| Definition of Ready | [[copilot-instructions.md]] | [[agents/spec-generator.agent.md]], [[agents/orchestrator.agent.md]] |
| Stack y restricciones | [[instructions/backend.instructions.md]] | [[agents/backend-developer.agent.md]], [[agents/frontend-developer.agent.md]], [[agents/database.agent.md]], [[agents/spec-generator.agent.md]] |
| Arquitectura | [[instructions/backend.instructions.md]] | [[agents/backend-developer.agent.md]], [[agents/frontend-developer.agent.md]], [[agents/spec-generator.agent.md]] |

---

## Reglas de Oro

> Principio rector: todas las contribuciones de la IA deben ser seguras, transparentes, con propósito definido y alineadas con las instrucciones explícitas del usuario.

### I. Integridad del Código y del Sistema
- **No código no autorizado**: no escribir, generar ni sugerir código nuevo a menos que el usuario lo solicite explícitamente.
- **No modificaciones no autorizadas**: no modificar, refactorizar ni eliminar código, archivos o estructuras existentes sin aprobación explícita del usuario.
- **Preservar la lógica existente**: respetar patrones arquitectónicos, estilo de codificación y lógica operativa del proyecto.

### II. Clarificación de Requisitos
- **Clarificación obligatoria**: si la solicitud es ambigua, incompleta o poco clara, detenerse y solicitar clarificación antes de proceder.
- **No realizar suposiciones**: basar todas las acciones estrictamente en información explícita proporcionada por el usuario.

### III. Transparencia Operativa
- **Explicar antes de actuar**: antes de cualquier acción, explicar qué se va a hacer y posibles implicaciones.
- **Detención ante la incertidumbre**: si surge inseguridad o un conflicto con estas reglas, detenerse y consultar al usuario.
- **Acciones orientadas a un propósito**: cada acción debe ser directamente relevante para la solicitud explícita.

---

## Entradas al Pipeline ASDD

| Tipo | Directorio | Descripción |
|------|-----------|-------------|
| Requerimientos de negocio | [[requirements/]] | Input: descripción funcional del feature |
| Especificaciones técnicas | [[specs/]] | Output de [[agents/spec-generator.agent.md]], fuente de verdad para implementación |

## Critical Rules for All Agents

1. **No implementation without a spec.** Always check [[specs/]] first.
2. **Backend architecture is layered** — follow the pattern defined in [[instructions/backend.instructions.md]]. Never bypass layers.
3. **Dependency wiring happens at the entry layer** (controller/router) — inject dependencies downward, never upward.
4. **UI state follows the project architecture** — use a single authoritative source of truth; no parallel state sources.
5. **I/O operations follow the project concurrency model** — sync or async as defined in [[instructions/backend.instructions.md]].
6. **Never commit secrets or credentials** — `.env`, credential files and API keys must be in `.gitignore`.

## Development Commands & Integration Notes

> Ver `README.md` en la raíz del proyecto.
