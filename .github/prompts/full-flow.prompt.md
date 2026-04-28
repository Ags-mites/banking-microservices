---
description: 'Orquesta el flujo completo ASDD: Spec → [Backend ∥ DB] → Tests → QA. Requiere un requerimiento de negocio como input.'
agent: Orchestrator
---

Inicia el flujo completo ASDD.

**Feature**: ${input:featureName:nombre del feature en kebab-case}
**Requerimiento**: ${input:requirement:descripción funcional del feature}

**El @Orchestrator ejecuta automáticamente:**

1. **[FASE 1 — Secuencial]** `Spec Generator` → genera `.github/specs/${input:featureName}.spec.md`
2. **[FASE 2 — Paralelo]** al aprobar la spec:
   - `Backend Developer` → implementa el backend
   - `Database Agent` → si hay cambios de esquema en la spec
3. **[FASE 3]** al completar implementación:
   - `Test Engineer Backend` → genera tests
4. **[FASE 4]** `QA Agent` → estrategia, Gherkin, riesgos

**El requerimiento se puede buscar también en** `.github/requirements/`.