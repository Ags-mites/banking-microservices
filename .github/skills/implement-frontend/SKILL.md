---
name: implement-frontend
description: Implementa un feature completo en el frontend. Requiere spec con status APPROVED en .github/specs/.
argument-hint: "<nombre-feature>"
---

# Implement Frontend

## Prerequisitos
1. Leer spec: [[specs/<feature>.spec.md]] — sección 2.3 (componentes, páginas, hooks)
2. Leer stack: [[instructions/frontend.instructions.md]]
3. Leer arquitectura: [[instructions/frontend.instructions.md]]
4. Revisar disponibilidad de componentes shadcn/ui en `src/components/ui/` — si falta alguno, instalarlo con `npx shadcn@latest add <component>`

## Orden de implementación
```
services → hooks/state → components (shadcn/ui) → pages/views → registrar ruta → refinamiento visual en [[skills/awesome-design/SKILL.md]]
```

| Capa | Responsabilidad |
|------|-----------------|
| **Services** | Llamadas HTTP al backend — sin estado, sin lógica de negocio |
| **Hooks / State** | Estado local, efectos, acciones — consume services |
| **Components** | UI reutilizable con shadcn/ui — recibe props, emite eventos, funcionalidad 100% operativa |
| **Pages / Views** | Composición final — layout + rutas + componentes shadcn/ui |
| **Awesome Design** | Refinamiento visual — variables CSS (HSL) y clases Tailwind — ver [[skills/awesome-design/SKILL.md]] |

## Patrones obligatorios
- Variables de entorno: URL del API siempre desde variables de entorno con prefijo `VITE_`, nunca hardcodeada
- **Componentes shadcn/ui**: OBLIGATORIO usar componentes base de `src/components/ui/`. Asegurar que funcionalidad esté 100% operativa (Button dispara eventos, Input está vinculado a estado, Dialog abre/cierra, etc.)
- **Sin estilos manuales**: NO crear CSS Modules ni estilos inline. El refinamiento visual es responsabilidad de [[skills/awesome-design/SKILL.md]].

## Componentes shadcn/ui — Instalación bajo demanda
Si durante la implementación se necesita un componente que no existe en `src/components/ui/`:
1. Sugerir al usuario (en comentario ASDD o directamente): `npx shadcn@latest add <component>`
2. Esperar a que esté disponible en `src/components/ui/`.
3. Continuar importándolo normalmente: `import { Dialog } from "@/components/ui/dialog"`

Ejemplos:
- Componentes comunes ya incluidos: `Button`, `Input`, `Label`, `Checkbox`
- Componentes bajo demanda: `npx shadcn@latest add card`, `npx shadcn@latest add tabs`, `npx shadcn@latest add dialog`

Ver patrones específicos en [[docs/lineamientos/dev-guidelines.md]] y [[instructions/frontend.instructions.md]].

## Restricciones
- Solo directorio de frontend del proyecto. No tocar backend.
- No generar tests (responsabilidad de [[agents/test-engineer-frontend.agent.md]]).
