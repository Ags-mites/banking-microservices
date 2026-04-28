---
applyTo: "frontend/src/**/*.{js,jsx}"
---

> **Scope**: Se aplica a proyectos con capa frontend. Si el proyecto es backend-only, este archivo no tiene efecto. Si el frontend usa otro framework (Vue, Angular, Svelte, etc.), adaptar las convenciones de componentes, rutas y estado al stack real.

# Instrucciones para Archivos de Frontend (React/Vite)

## Stack Técnico Obligatorio

- **Framework**: React 18+ + Vite
- **Componentes**: shadcn/ui (no MUI, Ant Design, Chakra, ni otro framework de componentes)
- **Estilos**: Tailwind CSS + variables CSS HSL en `src/app/globals.css` + CSS Modules para lógica local
- **Estado Global**: hooks personalizados (NO Redux, Zustand, Jotai — usar Context API si es necesario)
- **Validación de Datos**: zod + React Hook Form
- **Llamadas HTTP**: Axios (no fetch, no Apollo, no SWR)
- **Autenticación**: JWT desde backend (NO Firebase)

## Prohibición Absoluta — Zero Firebase

**BAJO NINGUNA CIRCUNSTANCIA se permite**:

❌ Importar Firebase en cualquier archivo  
❌ Usar `useAuth()`, `getAuth()`, o hooks de autenticación de Firebase  
❌ Configurar Firebase desde frontend  
❌ Almacenar tokens de Firebase en localStorage  
❌ Usar Firestore, Realtime Database o cualquier servicio de Firebase  

**Autenticación = Responsabilidad del Backend**:
1. Backend genera JWT al login
2. Frontend almacena JWT en localStorage
3. Frontend envía JWT en header `Authorization: Bearer <token>`
4. Backend valida JWT en middleware

**Ejemplo Correcto**:
```js
// services/authService.js
import axios from 'axios';

export async function login(email, password) {
  const res = await axios.post(`${VITE_API_URL}/api/auth/login`, {
    email,
    password
  });
  
  const { token } = res.data.data;  // RFC 9457 envelope
  localStorage.setItem('token', token);  // Solo JWT del backend
  return token;
}

export function getAuthHeader() {
  const token = localStorage.getItem('token');
  return {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  };
}
```

**Ejemplo INCORRECTO (PROHIBIDO)**:
```js
// ❌ NUNCA
import { getAuth, signInWithEmail } from 'firebase/auth';
import { auth } from './firebase-config';

// ❌ NUNCA
const user = await signInWithEmail(auth, email, password);
```
- **Autenticación**: JWT desde backend (NO Firebase)

## Prohibición Absoluta — Zero Firebase

**BAJO NINGUNA CIRCUNSTANCIA se permite**:

❌ Importar Firebase en cualquier archivo  
❌ Usar `useAuth()`, `getAuth()`, o hooks de autenticación de Firebase  
❌ Configurar Firebase desde frontend  
❌ Almacenar tokens de Firebase en localStorage  
❌ Usar Firestore, Realtime Database o cualquier servicio de Firebase  

**Autenticación = Responsabilidad del Backend**:
1. Backend genera JWT al login
2. Frontend almacena JWT en localStorage
3. Frontend envía JWT en header `Authorization: Bearer <token>`
4. Backend valida JWT en middleware

**Ejemplo Correcto**:
```js
// services/authService.js
import axios from 'axios';

export async function login(email, password) {
  const res = await axios.post(`${VITE_API_URL}/api/auth/login`, {
    email,
    password
  });
  
  const { token } = res.data.data;  // RFC 9457 envelope
  localStorage.setItem('token', token);  // Solo JWT del backend
  return token;
}

export function getAuthHeader() {
  const token = localStorage.getItem('token');
  return {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  };
}
```

**Ejemplo INCORRECTO (PROHIBIDO)**:
```js
// ❌ NUNCA
import { getAuth, signInWithEmail } from 'firebase/auth';
import { auth } from './firebase-config';

// ❌ NUNCA
const user = await signInWithEmail(auth, email, password);
```

## Estructura de Archivos

```
src/
  services/featureService.js   ← llamadas HTTP al backend
  hooks/useFeatures.js         ← estado local, efectos, acciones
  components/
    ui/                        ← componentes shadcn/ui (base funcional)
      button.jsx
      input.jsx
      dialog.jsx
      etc.
    PrincipalComponent.jsx     ← componentes personalizados
  pages/                        ← PageName.jsx (composición + shadcn/ui)
  app/
    App.jsx                    ← punto de entrada, rutas
    globals.css                ← variables CSS de shadcn/ui (colores HSL, radios, bordes)
```

**Nota**: Las variables CSS de shadcn (colores, radios, bordes, etc.) residen en `src/app/globals.css`. El pulido visual se realiza editando estas variables y ajustando clases Tailwind en los componentes — ver [[skills/awesome-design/SKILL.md]].

## Llamadas a la API Backend

Usar siempre **Axios** (no `fetch`). Las llamadas van en `services/`, nunca directamente en componentes o páginas.

```js
// services/featureService.js
import axios from 'axios';
const API_BASE = import.meta.env.VITE_API_URL;

export async function getFeatures() {
  const res = await axios.get(`${API_BASE}/api/v1/features`);
  return res.data;
}

export async function createFeature(data) {
  const res = await axios.post(`${API_BASE}/api/v1/features`, data);
  return res.data;
}
```

Las variables de entorno deben estar prefijadas con `VITE_`:
```js
const API_BASE = import.meta.env.VITE_API_URL;  // ej: http://localhost:3000
```

## Rutas (React Router v6)

Las rutas se registran en `src/App.jsx`:
```jsx
<Route path="/nueva-ruta" element={<NuevaPagina />} />
```

## Estilos: Tailwind Global + CSS Modules Locales

**Arquitectura de Estilos**:

1. **`src/app/globals.css`** — Variables HSL globales (shadcn/ui)
   - Define colores: `--primary`, `--accent`, `--foreground`, etc.
   - Define espaciado: `gap-4`, `p-6`, etc.
   - Define radios: `rounded-lg`, `rounded-full`, etc.
   - ✅ Usado por: Clases Tailwind en JSX

2. **Clases Tailwind en JSX** — Composición flexible
   - ✅ `<div className="flex gap-4 bg-primary p-6">`
   - ❌ NO: `className="ml-12 pl-24"` (espaciado local hardcoded)

3. **CSS Modules** — SOLO para lógica de posicionamiento local
   - ✅ `QuoteGrid.module.css` (grid, flex layout)
   - ❌ NO: `QuoteGrid.module.css` para colores o tipografía

**Ejemplo de Arquitectura Correcta**:

```jsx
// components/QuoteGrid.jsx
import { Button } from '@/components/ui/button';
import styles from './QuoteGrid.module.css';

export function QuoteGrid({ items }) {
  return (
    <div className={styles.gridContainer}>
      {items.map(item => (
        <div key={item.id} className="bg-card rounded-lg p-6 shadow-card">
          <h2 className="text-xl font-semibold text-foreground">{item.name}</h2>
          <Button className="mt-4">Edit</Button>
        </div>
      ))}
    </div>
  );
}
```

```css
/* QuoteGrid.module.css — SOLO posicionamiento */
.gridContainer {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;  /* Define gap más complejo que Tailwind puede manejar */
}
```

❌ **PROHIBIDO**:
- Bootstrap, MUI, Chakra (frameworks globales)
- Styled-Components, Emotion (CSS-in-JS)
- Inline `style={{}}` props
- Archivos CSS globales fuera de `globals.css`
- Espaciado local hardcoded en Tailwind (`pl-24`, `ml-12`)

✅ **PERMITIDO**:
- Variables CSS en `globals.css`
- Clases Tailwind para composición
- CSS Modules para grid/flex layout complejo

## Composición de Componentes

- Un componente por archivo.
- Props tipadas con JSDoc si son complejas.
- **CERO lógica de negocio en componentes** — delegar a hooks/servicios.

### Componentes shadcn/ui
- Usar componentes de `src/components/ui/` (Button, Input, Dialog, Card, etc.)
- Si falta el componente: `npx shadcn@latest add <component>`
- **Nunca modificar** componentes shadcn internos
- Si necesitas refinamiento visual: [[skills/awesome-design/SKILL.md]]

**Ejemplo correcto**:
```jsx
import { Button } from "@/components/ui/button"

export function MyButton() {
  return <Button variant="outline" size="lg">Click me</Button>
}
```

## Prohibiciones Arquitectónicas

❌ Lógica de negocio en componentes (mover a hooks)  
❌ Llamadas HTTP directas en componentes (usar servicios)  
❌ Frameworks CSS globales (Bootstrap, MUI, Chakra, etc.)  
❌ Inline `style={{}}` props (usar Tailwind)  
❌ Estilos en locales que deberían ser globales  
❌ Dependencias circulares entre servicios/hooks  
❌ `console.log()` en producción
