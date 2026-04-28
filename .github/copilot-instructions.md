# Copilot Instructions — MOC (Map of Content) — ASDD

## ASDD Workflow (Agent Spec Software Development)

Este repositorio sigue el flujo **ASDD**: toda funcionalidad nueva se ejecuta en cuatro fases orquestadas por agentes especializados.

```
[[agents/orchestrator.agent.md]] → [[agents/spec-generator.agent.md]] → [[agents/backend-developer.agent.md]] ∥ [[agents/frontend-developer.agent.md]] ∥ [[agents/database.agent.md]] → [[agents/test-engineer-backend.agent.md]] ∥ [[agents/test-engineer-frontend.agent.md]] → [[agents/qa.agent.md]] → [[agents/documentation.agent.md]]
```

### Fases del flujo ASDD
1. **Spec**: El agente [[agents/spec-generator.agent.md]] genera la spec en [[specs/<feature>.spec.md]].
2. **Implementación (paralelo)**: [[agents/backend-developer.agent.md]] + [[agents/frontend-developer.agent.md]] + [[agents/database.agent.md]] (si hay cambios de DB).
3. **Tests (paralelo)**: [[agents/test-engineer-backend.agent.md]] + [[agents/test-engineer-frontend.agent.md]].
4. **QA**: [[agents/qa.agent.md]] genera estrategia, Gherkin, riesgos y análisis de performance.
5. **Doc (opcional)**: [[agents/documentation.agent.md]] genera README updates, API docs y ADRs.

### Skills disponibles (slash commands):
- [[skills/asdd-orchestrate/SKILL.md]] — orquesta el flujo completo ASDD o consulta estado
- [[skills/generate-spec/SKILL.md]] — genera spec técnica en [[specs/]]
- [[skills/implement-backend/SKILL.md]] — implementa feature completo en el backend
- [[skills/implement-frontend/SKILL.md]] — implementa feature completo en el frontend
- [[skills/unit-testing/SKILL.md]] — genera suite de tests (backend + frontend)
- [[skills/gherkin-case-generator/SKILL.md]] — casos Given-When-Then + datos de prueba
- [[skills/risk-identifier/SKILL.md]] — clasificación de riesgos ASD (Alto/Medio/Bajo)
- [[skills/automation-flow-proposer/SKILL.md]] — propuesta de automatización con ROI
- [[skills/performance-analyzer/SKILL.md]] — planificación de pruebas de performance

### Requerimientos y Specs
- Los requerimientos de negocio viven en [[requirements/]]. Son la entrada al pipeline ASDD.
- Las specs técnicas viven en [[specs/]]. Cada spec es la fuente de verdad para implementar.
- Antes de implementar cualquier desarrollo, debe existir una spec aprobada en [[specs/]].
- Flujo: [[requirements/<feature>.md]] → [[skills/generate-spec/SKILL.md]] → [[specs/<feature>.spec.md]] (APPROVED)

---

## Mapa de Archivos ASDD

### Agentes
| Agente | Fase | Ruta |
|---|---|---|
| Orchestrator | Entry point | [[agents/orchestrator.agent.md]] |
| Spec Generator | Fase 1 | [[agents/spec-generator.agent.md]] |
| Backend Developer | Fase 2 | [[agents/backend-developer.agent.md]] |
| Frontend Developer | Fase 2 | [[agents/frontend-developer.agent.md]] |
| Database Agent | Fase 2 | [[agents/database.agent.md]] |
| Test Engineer Backend | Fase 3 | [[agents/test-engineer-backend.agent.md]] |
| Test Engineer Frontend | Fase 3 | [[agents/test-engineer-frontend.agent.md]] |
| QA Agent | Fase 4 | [[agents/qa.agent.md]] |
| Documentation Agent | Fase 5 | [[agents/documentation.agent.md]] |

### Skills
| Skill | Agente | Ruta |
|---|---|---|
| `/asdd-orchestrate` | Orchestrator | [[skills/asdd-orchestrate/SKILL.md]] |
| `/generate-spec` | Spec Generator | [[skills/generate-spec/SKILL.md]] |
| `/implement-backend` | Backend Developer | [[skills/implement-backend/SKILL.md]] |
| `/implement-frontend` | Frontend Developer | [[skills/implement-frontend/SKILL.md]] |
| `/unit-testing` | Test Engineer Backend + Frontend | [[skills/unit-testing/SKILL.md]] |
| `/gherkin-case-generator` | QA Agent | [[skills/gherkin-case-generator/SKILL.md]] |
| `/risk-identifier` | QA Agent | [[skills/risk-identifier/SKILL.md]] |
| `/automation-flow-proposer` | QA Agent | [[skills/automation-flow-proposer/SKILL.md]] |
| `/performance-analyzer` | QA Agent | [[skills/performance-analyzer/SKILL.md]] |

### Instructions (path-scoped)
| Scope | Ruta | Se aplica a |
|---|---|---|
| Backend | [[instructions/backend.instructions.md]] | `src/main/java/**/*.java` |
| Frontend | [[instructions/frontend.instructions.md]] | `frontend/src/**/*.{js,jsx}` |
| Tests | [[instructions/tests.instructions.md]] | `src/test/java/**/*.java` · `frontend/src/__tests__/**` |

### Lineamientos y Contexto
| Documento | Ruta |
|---|---|
| Lineamientos de Desarrollo | [[docs/lineamientos/dev-guidelines.md]] |
| Lineamientos QA | [[docs/lineamientos/qa-guidelines.md]] |
| Stack + Arquitectura + Naming | [[instructions/backend.instructions.md]] |
| Stack Frontend + Naming | [[instructions/frontend.instructions.md]] |

### Lineamientos generales para todos los agentes
- **Reglas de Oro**: ver [[AGENTS.md]] — rigen TODAS las interacciones.
- **Specs activas**: [[specs/]] — consultar siempre antes de implementar.

---

## Reglas de Oro

> Principio rector: todas las contribuciones de la IA deben ser seguras, transparentes, con propósito definido y alineadas con las instrucciones explícitas del usuario.

### I. Integridad del Código y del Sistema
- **No código no autorizado**: no escribir, generar ni sugerir código nuevo a menos que el usuario lo solicite explícitamente.
- **No modificaciones no autorizadas**: no modificar, refactorizar ni eliminar código, archivos o estructuras existentes sin aprobación explícita.
- **Preservar la lógica existente**: respetar los patrones arquitectónicos, el estilo de codificación y la lógica operativa existentes del proyecto.

### II. Clarificación de Requisitos
- **Clarificación obligatoria**: si la solicitud es ambigua, incompleta o poco clara, detenerse y solicitar clarificación antes de proceder.
- **No realizar suposiciones**: basar todas las acciones estrictamente en información explícita provista por el usuario.

### III. Transparencia Operativa
- **Explicar antes de actuar**: antes de cualquier acción, explicar qué se hará y posibles implicaciones.
- **Detención ante la incertidumbre**: si surge inseguridad o conflicto con estas reglas, detenerse y consultar al usuario.
- **Acciones orientadas a un propósito**: cada acción debe ser directamente relevante para la solicitud explícita.

Consulta siempre [[AGENTS.md]] para las Reglas de Oro detalladas.

Consulta siempre [[AGENTS.md]] para las Reglas de Oro detalladas.

---

## Diccionario de Dominio

Términos canónicos a usar en specs (ver [[specs/]] en las secciones de requerimientos), código y mensajes. Este diccionario es parte del [[copilot-instructions.md#diccionario-de-dominio]]:

| Término | Definición | Sinónimos rechazados |
|---------|-----------|---------------------|
| **Usuario** (`user`) | Persona autenticada mediante Firebase | Persona, cliente |
| **Perfil** (`profile`) | Datos personales y configuración del Usuario | Cuenta, ficha |
| **UID** (`uid`) | Identificador único provisto por Firebase Auth | ID técnico, `_id` |
| **Pregunta Frecuente** (`faq`) | Par pregunta-respuesta publicado para consulta | Artículo de ayuda |
| **Pregunta** (`question`) | Texto de la pregunta dentro de una FAQ | Título |
| **Respuesta** (`answer`) | Texto de la respuesta dentro de una FAQ | Descripción, contenido |
| **Dashboard** | Pantalla principal con métricas (solo lectura) | Inicio |
| **Modo Oscuro** (`dark mode`) | Tema visual alternativo con colores oscuros | Modo noche |
| **Token** (`idToken`) | Token Firebase en header `Authorization: Bearer` | Contraseña, sesión |
| **Administrador** | Rol con permisos completos | Superusuario |
| `created_at` | Timestamp de creación en UTC | Fecha alta |
| `updated_at` | Timestamp de última actualización en UTC | Fecha modificación |

**Reglas:** `uid` siempre de Firebase. `FAQ` = par completo. Timestamps en snake_case. `Dashboard` es solo lectura.

**Consulta esto en:** [[agents/spec-generator.agent.md]] (Fase 1), [[docs/lineamientos/dev-guidelines.md]] (implementación).

---

## Project Overview

Ver [[README.md]] en la raíz del proyecto para stack, arquitectura y estructura de carpetas.
