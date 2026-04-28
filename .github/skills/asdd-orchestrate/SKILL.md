---
name: asdd-orchestrate
description: Orquesta el flujo ASDD completo. Fase 1 (Spec) → Fase 2 (Backend ∥ Database) → Fase 3 (Tests) → Fase 4 (QA).
argument-hint: "<nombre-feature> | status"
---

# ASDD Orchestrate

## Flujo

```
[FASE 1 — SECUENCIAL]
  [[agents/spec-generator.agent.md]] → [[specs/<feature>.spec.md]]  (DRAFT → APPROVED)

[FASE 2 — PARALELO ∥]
  [[agents/backend-developer.agent.md]]  ∥  [[agents/database.agent.md]] (si hay modelos nuevos)

[FASE 3 —]
  [[agents/test-engineer-backend.agent.md]]

[FASE 4 — SECUENCIAL]
  [[agents/qa.agent.md]] → [[skills/gherkin-case-generator/SKILL.md]], [[skills/risk-identifier/SKILL.md]]
```

## Proceso
1. Busca [[specs/<feature>.spec.md]]
   - No existe → ejecuta [[skills/generate-spec/SKILL.md]] y espera
   - `DRAFT` → pide aprobación al usuario
   - `APPROVED` → actualiza a `IN_PROGRESS` y continúa
2. Lanza Fase 2 en paralelo (Task backend + Task database si aplica)
3. Cuando Fase 2 completa → lanza Fase 3
4. Cuando Fase 3 completa → lanza Fase 4 ([[agents/qa.agent.md]])
5. Actualiza spec a `IMPLEMENTED` y reporta estado final

## Comando status
Al recibir `status`: lista specs en [[specs/]] con su estado y próxima acción pendiente.

## Reglas
- Sin spec `APPROVED` → no hay código — sin excepciones
- No implementar directamente — solo coordinar y delegar
- Si una fase falla → detener el flujo y notificar al usuario con contexto
