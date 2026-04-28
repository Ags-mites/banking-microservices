---
id: SPEC-###
status: DRAFT
feature: nombre-del-feature
created: YYYY-MM-DD
updated: YYYY-MM-DD
author: spec-generator
version: "1.0"
related-specs: []
---

# Spec: [Nombre de la Funcionalidad]

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción
Resumen de la funcionalidad en 2-3 oraciones. Qué hace, para quién y qué problema resuelve.

### Requerimiento de Negocio
El requerimiento original tal como fue proporcionado por el usuario (o copiado de `.github/requirements/<feature>.md`).

### Historias de Usuario

#### HU-01: [Título descriptivo corto]

```
Como:        [rol del usuario — ej. Usuario autenticado, Administrador]
Quiero:      [acción o funcionalidad concreta]
Para:        [valor o beneficio esperado por el negocio]

Prioridad:   Alta / Media / Baja
Estimación:  XS / S / M / L / XL
Dependencias: HU-X, HU-Y o Ninguna
Capa:        Backend
```

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: [nombre del escenario exitoso]
  Dado que:  [contexto inicial válido]
  Cuando:    [acción del usuario]
  Entonces:  [resultado esperado verificable]
```

**Error Path**
```gherkin
CRITERIO-1.2: [nombre del escenario de error]
  Dado que:  [contexto inicial]
  Cuando:    [acción inválida o datos incorrectos]
  Entonces:  [manejo del error esperado con código HTTP y mensaje]
```

**Edge Case** *(si aplica)*
```gherkin
CRITERIO-1.3: [nombre del caso borde]
  Dado que:  [contexto de borde]
  Cuando:    [acción en el límite]
  Entonces:  [resultado esperado en el límite]
```

### Reglas de Negocio
1. Regla de validación (ej. "el campo X es obligatorio y no puede superar 100 caracteres")
2. Regla de autorización (ej. "solo el Administrador puede eliminar")
3. Regla de integridad (ej. "el nombre debe ser único en la colección")

---

## 2. DISEÑO

### Modelos de Datos

#### Entidades afectadas
| Entidad | Almacén | Cambios | Descripción |
|---------|---------|---------|-------------|
| `FeatureEntity` | tabla/colección `features` | nueva / modificada | descripción del recurso |

#### Campos del modelo
| Campo | Tipo | Obligatorio | Validación | Descripción |
|-------|------|-------------|------------|-------------|
| `id` | string / UUID | sí | auto-generado | Identificador único |
| `name` | string | sí | max 100 chars | Nombre del recurso |
| `description` | string | no | max 500 chars | Descripción |
| `created_at` | datetime (UTC) | sí | auto-generado | Timestamp creación |
| `updated_at` | datetime (UTC) | sí | auto-generado | Timestamp actualización |

#### Índices / Constraints
- Listar índices necesarios con su justificación de uso (búsqueda frecuente, unicidad, etc.)

### API Endpoints

#### POST /api/v1/[features]
- **Descripción**: Crea un nuevo recurso
- **Auth requerida**: sí / no
- **Request Body**:
  ```json
  { "name": "string", "description": "string (opcional)" }
  ```
- **Response 201**:
  ```json
  { "uid": "uuid", "name": "string", "created_at": "iso8601", "updated_at": "iso8601" }
  ```
- **Response 400**: campo obligatorio faltante o inválido
- **Response 401**: token ausente o expirado
- **Response 409**: ya existe un recurso con ese nombre

#### GET /api/v1/[features]
- **Descripción**: Lista todos los recursos
- **Auth requerida**: sí
- **Response 200**:
  ```json
  [{ "uid": "uuid", "name": "string", ... }]
  ```

#### GET /api/v1/[features]/{uid}
- **Descripción**: Obtiene un recurso por uid
- **Auth requerida**: sí
- **Response 200**: recurso completo
- **Response 404**: no encontrado

#### PUT /api/v1/[features]/{uid}
- **Descripción**: Actualiza un recurso existente
- **Auth requerida**: sí
- **Request Body**: campos opcionales a actualizar
- **Response 200**: recurso actualizado
- **Response 404**: no encontrado

#### DELETE /api/v1/[features]/{uid}
- **Descripción**: Elimina un recurso
- **Auth requerida**: sí
- **Response 204**: eliminado exitosamente
- **Response 404**: no encontrado

### Arquitectura y Dependencias
- Paquetes nuevos requeridos: ninguno / listar si aplica
- Servicios externos: listar integraciones (auth, storage, third-party APIs)
- Impacto en punto de entrada de la app: registrar router/módulo si aplica

### Notas de Implementación
> Observaciones técnicas, decisiones de diseño o advertencias para los agentes de desarrollo.

---

## 3. LISTA DE TAREAS

> Checklist accionable para todos los agentes. Marcar cada ítem (`[x]`) al completarlo.
> **IMPORTANTE**: Los tests se generan según la matriz 3-2-1. NO crear pruebas adicionales.

### Backend

#### Implementación
- [ ] Crear modelos `[Feature]Create`, `[Feature]Update`, `[Feature]Response`, `[Feature]Document`
- [ ] Implementar `[Feature]Repository` — métodos CRUD
- [ ] Implementar `[Feature]Service` — lógica de negocio de HU-01
- [ ] Implementar router/controller `/api/v1/[features]` — endpoints CRUD
- [ ] Registrar en punto de entrada de la app

#### Tests Backend (Matriz 3-2-1)

**Regla: POR CADA MÉTODO = 3 tests | POR CADA ENDPOINT = 3 tests | POR CADA ADAPTER = 2 tests**

```
╔════════════════════════════════════════════════════════════════════════════════╗
║             MATRIZ DE PRUEBAS — CANTIDAD FIJA                    ║
╠════════════════════════════════════════════════════════════════════════════════╣
║                                                                ║
║  SERVICE (3 tests por método):                                      ║
║  ├─ happy path:    create_{name}_success                      ║
║  ├─ error:       create_{name}_throws_{Exception}             ║
║  └─ edge case:   create_{name}_with_empty_{field}_throws      ║
║                                                                ║
║  CONTROLLER (3 tests por endpoint):                             ║
║  ├─ 200/201:   {method}_{endpoint}_returns_200              ║
║  ├─ 400:      {method}_{endpoint}_returns_400              ║
║  └─ 404:      {method}_{endpoint}_returns_404              ║
║                                                                ║
║  ADAPTER (2 tests por método):                               ║
║  ├─ save:      save_returns_entity                        ║
║  └─ findById:  findById_returns_optional                 ║
║                                                                ║
╚════════════════════════════════════════════════════════════════════════════════╝
```

**Ejemplo concreto (Feature = Account):**

| Test Class | Cantidad | Tests a crear |
|-----------|---------|------------|
| `AccountServiceTests.java` | 3 por método | create: 3, getById: 3, deposit: 3, withdraw: 3 |
| `AccountControllerTests.java` | 3 por endpoint | POST: 3, GET: 3, PUT: 3, DELETE: 3 |
| `AccountRepositoryAdapterTests.java` | 2 por método | save: 2, findById: 2, findAll: 2 |

**No generar:**
- Tests de métodos no definidos en la spec
- Tests de integración (usan mocks)
- Tests de más de un método por test

### QA
- [ ] Ejecutar skill `/gherkin-case-generator` → criterios CRITERIO-1.1, 1.2, 1.3
- [ ] Ejecutar skill `/risk-identifier` → clasificación ASD de riesgos
- [ ] Revisar cobertura de tests contra criterios de aceptación
- [ ] Validar que todas las reglas de negocio están cubiertas
- [ ] Actualizar estado spec: `status: IMPLEMENTED`
