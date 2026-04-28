---
name: awesome-design
description: Refina y pulimenta la UI usando shadcn/ui. Trabaja con Design System de referencia ([[design-system.md]] o similar) y aplica valores en src/app/globals.css + tailwind.config.ts. Los componentes shadcn se adaptan automáticamente.
argument-hint: "<componente-o-pagina>"
---

# Awesome Design — Refinamiento Visual con shadcn/ui

## Propósito

Después de que el Backend Developer y Frontend Developer completaron la implementación funcional usando componentes shadcn/ui, esta skill refina la experiencia visual **sin reescribir lógica ni cambiar componentes**. 

El enfoque es exclusivamente **actualizar valores visuales** en:
- `src/app/globals.css` (variables CSS de shadcn)
- `tailwind.config.ts` (configuración de Tailwind)

Los componentes shadcn/ui usan esos valores automáticamente. **Sin cambios en JSX.**

**Entrada**: componentes shadcn/ui ya funcionales.
**Salida**: UI refinada visualmente, los componentes se verán diferentes pero funcionan igual.

**Design System de Referencia**: [[design-system.md]]

## Cómo Funciona

### La Cadena de Valores

```
design-system.md    ← QUÉ VALORES APLICAR (referencia)
                ↓
        src/app/globals.css               ← VARIABLES CSS reales
        tailwind.config.ts                ← CONFIG DE TAILWIND
                ↓
    src/components/ui/*                   ← Componentes shadcn USAN esos valores
        (Button, Card, Input, etc.)       ← Se ven diferentes sin cambiar código
```

**Nunca modificas los componentes shadcn.** Solo actualizas variables y config.

### Ejemplo Real

**Cambio de Design System: Shopify (neon green) → Dark Purple**

```diff
# 1. [[design-system.md]]
  Accent: #9D4EDD (royal purple)

# 2. src/app/globals.css
- --accent: 240 84% 60%;       /* neon green */
+ --accent: 290 61% 55%;       /* royal purple */

# Resultado: 
  - Todos los focus rings: violeta
  - Todos los highlights: violeta
  - Links, badges, accents: violeta
  - CERO cambios en JSX
  - Componentes siguen funcionando exactamente igual
```

## Principios de Awesome Design

1. **Sistema de Diseño Centralizado**: [[design-system.md]] es la única verdad
2. **Updatear Valores, No Código**: cambios en CSS/Tailwind config, NO en componentes
3. **Consistencia Global**: todas las decisiones visuales se aplican en 1-2 archivos
4. **No Repetición**: NUNCA CSS Modules ni estilos inline — siempre variables/config
5. **Propósito**: mejorar diseño sin impactar funcionalidad

## Estructura del Sistema de Diseño

El sistema de diseño está documentado completamente en: **[[design-system.md]]**

Esa referencia incluye:
- ✅ Paleta de colores exacta (hex, HSL, roles)
- ✅ Tipografía y jerarquía (familia, tamaño, peso, line-height)
- ✅ Componentes estilados (Buttons, Cards, Inputs, Navigation)
- ✅ Espaciado, border-radius, sombras de elevación
- ✅ Layout principles, grid, whitespace
- ✅ Responsive behavior y breakpoints
- ✅ Do's and Don'ts

**Uso**: Consulta ese archivo ANTES de hacer cambios. Es tu referencia única.
- Revisar la implementación en `src/components/` y `src/pages/` — funciona sin errores
- **Consultar [[design-system.md]]** — es tu fuente única de verdad
- Identificar qué necesita refinamiento visual: espaciado inconsistente, tipografía que no sigue jerarquía, colores que no alinean, etc.

### 2. Mapear Cambios contra el Design System
Para cada elemento que necesita refinamiento:
1. **Identifica el rol**: ¿Es un heading? ¿Un button? ¿Un card? ¿Un input?
2. **Busca en el Design System**: [[design-system.md]] → sección "Component Stylings"
3. **Anota los valores**: colores exactos (hex), tamaño de fuente, weight, padding, radius, shadow
4. **No improvises**: si no está en el Design System, propón agregarlo PRIMERO antes de implementar

### 3. Editar Variables CSS Globales (src/app/globals.css)
Si se necesita cambiar **la paleta global, tipografía o espaciado de todo el proyecto**:
- Traducir los valores del Design System a variables CSS HSL o Tailwind config
- Ejemplo de Design System → CSS:
  ```
  Design System: "Shopify White (#FFFFFF)"
  ↓
  CSS Variable: --color-primary: 0 0% 100%;
  ↓
  Utilidad Tailwind: bg-primary = "rgb(255, 255, 255)"
  ```
- Verificar que los cambios apliquen globalmente en todos los componentes

### 4. Ajustar Clases Tailwind en Componentes
Para refinamientos locales (espaciado un componente específico, radios, etc.):
- Editar el componente JSX y ajustar clases Tailwind
- SIEMPRE referenciar [[design-system.md]] para valores exactos
- Ejemplo correcto:
  ```jsx
  // ✅ Clases Tailwind que siguen el Design System
  <div className="bg-deep-teal border border-dark-card-border rounded-lg p-6 shadow-card">
    <h1 className="text-96 font-300 text-white">Heading</h1>
    <p className="text-18 font-400 text-muted-text">Body</p>
  </div>
  
  // ❌ INCORRECTO — valores no documentados
  <div className="bg-blue-900 p-8 rounded-3xl shadow-2xl">
  ```

### 5. Validar sin Quebrar Funcionalidad
Después de cada ajuste:
1. Revisar en navegador — elementos alineados, colores correctos, sombras visibles
2. Verifica que funcionalidad sigue intacta — botones disparan eventos, inputs aceptan entrada, etc.
3. Prueba en modo claro/oscuro si aplica (clases `dark:`)
4. Valida en responsive (mobile/tablet/desktop) — ver [[design-system.md]]#8

## Casos de Uso

### Caso 1: Cambiar Paleta de Colores Global
**Solicitud**: "El cliente quiere colores más cálidos en lugar de Shopify's neon green."

**Cómo hacerlo:**
1. Abre [[design-system.md]] → anota nuevos colores
2. Abre `src/app/globals.css` → actualiza variables CSS
   ```css
   :root {
     -- accent: 40 80% 55%;  /* cambio de neon green a warm orange */
   }
   ```
3. Abre navegador → todos los focus rings, badges, highlights → ahora son naranjas
4. **Sin cambiar componentes shadcn**

### Caso 2: Cambiar Tipografía
**Solicitud**: "Cambiar de Inter a Lato en todo el sitio."

**Cómo hacerlo:**
1. Abre `tailwind.config.ts` → actualiza fontFamily
   ```typescript
   theme: {
     fontFamily: {
       sans: ['Lato', 'sans-serif'],  // cambio de Inter
     }
   }
   ```
2. Ejecuta `npm run dev` (rebuild)
3. Todo el sitio usa Lato automáticamente
4. **Sin cambiar componentes shadcn**

### Caso 3: Cambiar Sistema de Diseño Completamente

**Antes**: Shopify-inspired (neon green, ultra-light typography, dark theme)
**Ahora**: Material Design 3 (color system diferente, tipografía diferente)

**Proceso:**
1. Crear `.github/skills/awesome-design/material-design-3.md` con nuevas especificaciones
2. Actualizar `src/app/globals.css` con nuevas variables CSS (colores, radios)
3. Actualizar `tailwind.config.ts` con nuevas escalas (tipografía, espaciado)
4. Los componentes shadcn automáticamente lucen diferentes
5. **Sin tocar ni un solo componente JSX**

Este es el poder: **los componentes son agnósticos al diseño. Solo ven variables.**

### Caso 4: Ajustar Espaciado Solo en un Componente
**Solicitud**: "Este card necesita más padding interior."

**Cómo hacerlo:**
1. Abre el componente en `src/pages/` o `src/components/`
2. Cambia la clase Tailwind:
   ```jsx
   {/* ANTES */}
   <Card className="p-4">Content</Card>
   
   {/* DESPUÉS */}
   <Card className="p-8">Content</Card>
   ```
3. Listo. El card tiene más aire.
4. Otros cards no cambian — solo este.

**Nota**: Si MUCHOS cards necesitan el mismo cambio, es mejor actualizar la variable global en `tailwind.config.ts`.

### Caso 3: Mejorar Espaciado en Cards
**Solicitud**: "Los cards están apretados, necesitan más aire"
**Cómo hacerlo:**
1. Abre `tailwind.config.ts` → aumenta la escala de padding
   ```typescript
   theme: {
     spacing: {
       '4': '1rem',    // antes
       '6': '1.5rem',  // nuevo
     }
   }
   ```
2. O cambia localmente en el componente (ver Caso 4)

## Archivos que Modificas en AWESOME DESIGN

**SOLO estos dos archivos — nunca modificas componentes JSX:**

| Archivo | Qué Cambiar | Ejemplo |
|---------|-------------|---------|
| `src/app/globals.css` | Variables CSS (colores, radios) | `--accent: 290 61% 55%;` |
| `tailwind.config.ts` | Escalas (tipografía, espaciado) | `fontFamily: { sans: ['Lato'] }` |
| Componentes JSX | ⚠️ NUNCA modificar excepto clases Tailwind locales | `<Card className="p-8">` OK, estilos NO |

## Herramientas y Referencia Rápida

### Antes de Empezar
1. **Prepara tu workspace**:
   - Navegador con preview del componente
   - DevTools inspector abierto
   - Editor con `globals.css` y `tailwind.config.ts` lado a lado
   - [[design-system.md]] en otra pestaña

2. **Flujo Rápido**:
   - Identifica qué necesita cambiar (color, tipografía, espaciado, sombra, radio)
   - Busca en [[design-system.md]] el valor exacto
   - Aplica en CSS global o clase Tailwind local
   - Valida en navegador

### Extensiones de VS Code Recomendadas
- **Tailwind CSS IntelliSense**: autocompletar clases Tailwind al escribir
- **DevTools**: inspector de estilos en tiempo real

### Checklist de Validación
- [ ] El elemento visual coincide exactamente con [[design-system.md]]
- [ ] No hay CSS Modules, estilos inline, ni modificaciones en componentes shadcn
- [ ] Toda la funcionalidad sigue intacta
- [ ] Se probó en mobile, tablet, desktop
- [ ] Se probó en modo claro y oscuro (si aplica)

---

## Integración con ASDD

### Posición en el Pipeline

```
Fase 2 (Paralelo):
  Backend Developer ∥ Frontend Developer (shadcn/ui funcional) ∥ Database Agent
  ↓
Fase 3 (Paralelo):
  Test Engineer Backend ∥ Test Engineer Frontend
  ↓
→ AWESOME DESIGN ← (refinamiento visual: globals.css + tailwind.config.ts)
  ↓
Fase 4:
  QA Agent (validar diseño + funcionalidad)
```

- **Entrada**: componentes shadcn/ui funcionales de [[agents/frontend-developer.agent.md]]
- **Referencia**: spec en [[specs/<feature>.spec.md]] + [[design-system.md]]
- **Trabajo**: Actualizar `src/app/globals.css` y `tailwind.config.ts`
- **Resultado**: componentes shadcn lucen diferentes, pero son los mismos, funcionan igual
- **Salida**: UI refinada, lista para QA

### Cambiar de Design System

Supongamos que tu cliente quiere cambiar de **Shopify-inspired → Dark Purple Theme**:

**Pasos:**
1. Crear `skills/awesome-design/dark-purple.md` (nueva referencia visual)
2. Actualizar `src/app/globals.css`:
   ```css
   :root {
     /* Nuevos valores del dark-purple system */
     --primary: 290 61% 60%;
     --accent: 270 80% 50%;
     --radius: 0.75rem;
     /* etc. */
   }
   ```
3. Actualizar `tailwind.config.ts` (si tipografía o espaciado cambió)
4. **Resultado**: todos los componentes shadcn usan el nuevo tema automáticamente

**No tocas:**
- Componentes shadcn/ui (Button, Card, Input, etc.)
- La estructura JSX
- La funcionalidad

**Lo que pasa:**
- Botones: siguen siendo buttons, pero colores nuevos
- Cards: siguen siendo cards, pero tema nuevo
- Todo funciona exactamente igual
