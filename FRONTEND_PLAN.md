# FRONTEND_PLAN.md — Plan autocontenido para construir el frontend web de Teleticket Lite

Este documento es **autosuficiente**: quien lo implemente (persona o agente de IA) **no tiene acceso al repositorio del backend** y no debe intentar leerlo, clonarlo ni inspeccionarlo. Todo lo necesario (endpoints, contratos de datos, reglas de negocio, validaciones) está definido aquí mismo. Si algo pareciera faltar, no se debe inventar: se implementa únicamente lo que este archivo especifica.

---

## 1. Objetivo

Construir una **Single Page Application (SPA) en React 19** que consuma una API REST ya desplegada y funcionando en internet, y que presente **3 interfaces distintas según el rol** del usuario autenticado:

- **CLIENT**: descubre eventos, se inscribe (genera un ticket con QR), revisa sus inscripciones.
- **OWNER**: gestiona sus propios eventos (crear/editar/cancelar/imágenes), gestiona su personal STAFF, ve quién se registró a sus eventos.
- **STAFF**: ve los eventos a los que fue afiliado, edita descripción/categoría de esos eventos, valida el QR de los asistentes en la puerta del evento.

El rol **ADMIN** queda **fuera de alcance**: no se construye ninguna pantalla para ese rol (ver §15).

---

## 2. Regla fundamental de autenticación: todo pasa por esta API

**No existe integración directa con AWS Cognito en el frontend.** No se usa AWS Amplify, no se usa ningún SDK de Cognito, no se necesita `clientId`, `userPoolId` ni ninguna credencial de AWS en el cliente. Cualquier plan previo que contemplara hablar con Cognito directamente desde el navegador queda **descartado**.

Toda la lógica de negocio —incluyendo login, registro, emisión y validación de tokens, y roles— la resuelve **exclusivamente** esta API REST. La única responsabilidad del frontend es:

1. Enviar credenciales a `POST /auth/login`.
2. Guardar los tokens que la API devuelve en la respuesta.
3. Reenviar el token correspondiente en el header `Authorization: Bearer <token>` en cada petición subsiguiente que lo requiera.
4. Registrar usuarios nuevos vía `POST /users` (esta ruta también la resuelve la API completa, incluyendo cualquier alta en el proveedor de identidad; el frontend no sabe ni le importa cómo).

---

## 3. URL base de la API (única, pública, sin alternativas locales)

```
https://ds5cqe1hk8.execute-api.us-east-1.amazonaws.com
```

Esta es la **única** URL base válida. Está desplegada y accesible por internet. **No** se debe configurar, sugerir ni dejar como opción ningún `localhost` o backend local — no aplica en este proyecto, sin importar si el frontend se corre en modo desarrollo o producción.

Configurar como variable de entorno (buena práctica, pero con un único valor posible):

`frontend/.env`:
```
VITE_API_BASE_URL=https://ds5cqe1hk8.execute-api.us-east-1.amazonaws.com
```

El código nunca hardcodea la URL inline; siempre lee `import.meta.env.VITE_API_BASE_URL`.

---

## 4. Formato de respuesta uniforme (envelope)

**Toda** respuesta de la API — éxito o error — tiene esta forma exacta:

```ts
interface ApiResponse<T> {
  status: "success" | "error";
  code: number;
  data: T | null;
  error: { message: string; stack: string | null } | null;
}
```

Reglas de consumo:
- Si `status === "success"`, usar `data` (tipado según el endpoint, ver §9).
- Si `status === "error"`, usar `error.message` para mostrar el problema al usuario. Nunca mostrar `error.stack` en la interfaz (es información de diagnóstico interno).
- Algunos endpoints devuelven `data: null` en caso de éxito (ej. eliminar una foto, afiliar/desafiliar staff) — esto es un éxito válido, no un error; no tratar `data === null` como fallo.

---

## 5. Autenticación y sesión

### 5.1 Flujo de login

`POST /auth/login` — body JSON `{ email: string; password: string }` → responde `LoginResponse` (ver interfaz en §9.1).

Al recibir la respuesta:
1. Guardar `accessToken`, `idToken` y `refreshToken` en `localStorage` (una única clave de sesión serializada).
2. Decodificar el `idToken` (JWT) para extraer:
   - el claim `email`.
   - el claim `cognito:groups` (array de strings) → tomar la primera posición como **rol activo**: `"CLIENT" | "STAFF" | "OWNER" | "ADMIN"`.
3. Redirigir según el rol activo: `CLIENT` → `/client/events`, `OWNER` → `/owner/events`, `STAFF` → `/staff/events`. Si el rol resultante es `ADMIN`, mostrar una pantalla informativa ("Esta aplicación no gestiona cuentas ADMIN") y no permitir el acceso a ninguna de las 3 interfaces construidas.

### 5.2 Qué token enviar en `Authorization`

Usar el **`accessToken`** como header `Authorization: Bearer <accessToken>` en toda petición autenticada.

### 5.3 Registro (no inicia sesión automáticamente)

`POST /users` crea la cuenta (siempre con rol `CLIENT`, es un registro público de autoservicio) pero **no devuelve tokens**. Tras un registro exitoso, mostrar un mensaje de éxito y **redirigir a la pantalla de login** para que el usuario inicie sesión manualmente con el email/contraseña recién creados.

### 5.4 Expiración de sesión

No hay un endpoint de "refrescar token" disponible todavía; `refreshToken` se guarda pero **no se usa** por ahora. Ante una respuesta `401` de cualquier endpoint, o si al decodificar el `idToken` su claim `exp` ya venció, cerrar la sesión localmente (borrar tokens guardados) y redirigir a `/login` con el mensaje "Tu sesión expiró, inicia sesión nuevamente".

---

## 6. Stack tecnológico (decisión de arquitectura)

| Capa | Elección | Motivo |
|---|---|---|
| Framework | **React 19** | requerido |
| Bundler/dev server | **Vite** | estándar actual para SPA React |
| Lenguaje | **TypeScript** | tipar los contratos de datos de §9 reduce errores de integración |
| Ruteo | **React Router v7** (`react-router-dom`) | ruteo declarativo + guards por rol |
| Estado de servidor / caché | **TanStack Query (`@tanstack/react-query`) v5** | caching, invalidación y estados de carga/error |
| Cliente HTTP | **Axios** | interceptors para token y manejo uniforme del envelope `ApiResponse` |
| Formularios + validación | **react-hook-form** + **zod** (`@hookform/resolvers`) | validación en cliente que replica las reglas de §11 |
| Estilos / responsive | **Tailwind CSS** (v4) | utilidades responsive sin necesitar imágenes estáticas |
| Iconografía | **lucide-react** | íconos como componentes (no archivos de imagen) |
| Decodificación de JWT | **jwt-decode** | leer claims del token sin llamar a la API |
| Fechas | **date-fns** | formatear los strings `LocalDateTime`/`LocalDate` que llegan de la API |
| Notificaciones | **sonner** (o un `Toast` propio en `shared/ui`) | feedback de éxito/error consistente |

**Regla de imágenes estáticas:** el proyecto nace vacío, sin assets. No referenciar rutas de imagen inventadas (nada de `/assets/hero.png` que no se cree en el mismo cambio). Usar:
1. Íconos de `lucide-react` (componentes, no archivos).
2. Las URLs reales que devuelve la API (`images[].url` de eventos, `photoUrl` de usuario, `qrUrl` de tickets) — son URLs de S3 reales y accesibles por internet.
3. SVGs propios creados como componentes React en `frontend/src/shared/assets/` si se necesita un logo — deben quedar creados en el mismo commit que los referencia.
4. Estados vacíos: composiciones CSS/SVG inline, no fotografías externas de bancos de imágenes.

---

## 7. Arquitectura de carpetas (modular por dominio)

Proyecto nuevo en una carpeta `frontend/` con su propio `package.json`, independiente de cualquier build de backend.

```
frontend/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.ts
├── .env                       # VITE_API_BASE_URL=https://ds5cqe1hk8.execute-api.us-east-1.amazonaws.com
└── src/
    ├── main.tsx                # bootstrap: QueryClientProvider + RouterProvider + AuthProvider
    ├── app/
    │   ├── router.tsx          # árbol de rutas completo (§17)
    │   ├── providers.tsx
    │   └── layouts/
    │       ├── AuthLayout.tsx        # /login, /register
    │       ├── ClientLayout.tsx
    │       ├── OwnerLayout.tsx
    │       └── StaffLayout.tsx
    │
    ├── modules/
    │   ├── auth/
    │   │   ├── api/authApi.ts         # login()
    │   │   ├── types.ts               # LoginRequest, LoginResponse
    │   │   ├── context/AuthContext.tsx
    │   │   └── pages/LoginPage.tsx
    │   │
    │   ├── users/
    │   │   ├── api/
    │   │   │   ├── userApi.ts          # register, getMe, updateMe, uploadPhoto, deletePhoto
    │   │   │   └── staffApi.ts         # createStaff, affiliateStaff, disaffiliateStaff
    │   │   ├── types.ts
    │   │   ├── components/
    │   │   │   ├── ProfileForm.tsx
    │   │   │   ├── ProfilePhotoUploader.tsx
    │   │   │   └── StaffForm.tsx
    │   │   └── pages/
    │   │       ├── RegisterPage.tsx
    │   │       ├── ProfilePage.tsx          # compartida CLIENT/STAFF/OWNER
    │   │       ├── StaffListPage.tsx        # OWNER
    │   │       └── StaffCreatePage.tsx      # OWNER
    │   │
    │   ├── events/
    │   │   ├── api/
    │   │   │   ├── eventClientApi.ts    # search, getById
    │   │   │   ├── eventOwnerApi.ts     # create, update, cancel, listOwn, replaceImages, deleteImages
    │   │   │   ├── eventStaffApi.ts     # listAffiliated, updateStaffFields
    │   │   │   └── eventCategoryApi.ts  # list
    │   │   ├── types.ts
    │   │   ├── components/
    │   │   │   ├── EventCard.tsx
    │   │   │   ├── EventFilters.tsx
    │   │   │   ├── EventGallery.tsx
    │   │   │   ├── EventForm.tsx
    │   │   │   ├── EventImageManager.tsx
    │   │   │   ├── EventStaffEditForm.tsx
    │   │   │   └── Pagination.tsx
    │   │   └── pages/
    │   │       ├── EventCatalogPage.tsx      # CLIENT
    │   │       ├── EventDetailPage.tsx       # CLIENT
    │   │       ├── OwnerEventsPage.tsx       # OWNER
    │   │       ├── OwnerEventCreatePage.tsx  # OWNER
    │   │       ├── OwnerEventEditPage.tsx    # OWNER
    │   │       ├── StaffEventsPage.tsx       # STAFF
    │   │       └── StaffEventEditPage.tsx    # STAFF
    │   │
    │   └── qr/
    │       ├── api/qrApi.ts             # registerForEvent, validateQr, listAssistants, listMyInscriptions
    │       ├── types.ts
    │       ├── components/
    │       │   ├── QrTicketCard.tsx
    │       │   ├── QrScannerUploader.tsx
    │       │   └── AssistantsTable.tsx
    │       └── pages/
    │           ├── MyInscriptionsPage.tsx    # CLIENT
    │           ├── ValidateQrPage.tsx        # STAFF/OWNER
    │           └── EventAssistantsPage.tsx   # OWNER
    │
    └── shared/
        ├── api/
        │   ├── httpClient.ts           # instancia axios + interceptors (§7.7)
        │   └── apiResponse.ts          # tipo ApiResponse<T> + helper unwrap()
        ├── auth/
        │   ├── RoleGuard.tsx
        │   └── jwt.ts                  # decode(token) → { email, groups }
        ├── ui/                         # Button, Input, Select, Modal, Badge, Spinner, Toast, EmptyState
        ├── layout/                     # Navbar, Sidebar, BottomNav
        ├── hooks/                      # useDebounce, usePagination
        ├── utils/                      # formatDate, validators
        └── assets/                     # SVGs propios, si se necesitan
```

Nombres de carpetas/archivos en **inglés**; el **texto visible al usuario** (labels, botones, mensajes) en **español** (dominio hispanohablante).

### 7.7 Cliente HTTP (`shared/api/httpClient.ts`) — guía de implementación

- Instancia de Axios con `baseURL = import.meta.env.VITE_API_BASE_URL`.
- Interceptor de request: agrega `Authorization: Bearer <accessToken>` (§5.2) si hay sesión activa. **No** lo agrega en `POST /auth/login`, `POST /users` (registro) ni `POST /qr/tickets/validate` (endpoint público).
- Interceptor de response:
  - Éxito → retorna `response.data.data` (desempaqueta el envelope).
  - Error → construye un error de aplicación a partir de `response.data.error.message`.
  - `401` → cierra sesión + redirige a `/login`.
- **Content-Type**: los endpoints que suben archivos (`POST /users`, `POST /users/staff`, `POST /users/me/photo`, `POST /events`, `PUT /events/{id}/images`, `POST /qr/tickets/validate`) van como `multipart/form-data` usando `FormData` nativo. El resto va como `application/json`.

---

## 8. Convenciones de UI/UX

- **Responsive obligatorio**, mobile-first (breakpoints Tailwind por defecto: `sm 640px`, `md 768px`, `lg 1024px`, `xl 1280px`).
- **Diseño visual libre** (colores, tipografía, composición), siempre respetando: (a) legibilidad, (b) jerarquía clara y diferenciada por rol (OWNER con panel de gestión tipo sidebar, CLIENT con navegación tipo catálogo, STAFF con panel operativo simple), (c) estados de carga (spinner/skeleton), vacío (`EmptyState`) y error (mensaje + botón reintentar) en toda vista que dependa de datos remotos.
- Navegación mobile: barra inferior o menú hamburguesa según el rol; en desktop, sidebar (OWNER/STAFF) o navbar superior (CLIENT).
- Errores de formulario: mostrar debajo de cada campo, no solo en un toast genérico.
- Imágenes de evento: mostrar en galería ordenada por el campo `index` (0 a 7), con miniaturas + vista ampliada.
- Ticket QR: mostrar la imagen (`qrUrl`) en tamaño legible para escaneo, botón de descarga, y un badge de estado (`Aplicado` / `Pendiente`) según `alreadyApplied`.

---

## 9. Interfaces TypeScript (contratos de datos completos)

Estas interfaces son la **única fuente de tipado**. Cuando dos endpoints devuelven exactamente la misma forma de datos, se reutiliza una sola interfaz (no se duplica). Cuando la forma difiere según el endpoint —aunque las claves JSON se llamen igual—, se definen interfaces **distintas y nombradas explícitamente**, señalado con comentarios.

### 9.1 Auth

```ts
interface LoginRequest {
  email: string;
  password: string;
}

interface LoginResponse {
  accessToken: string;
  idToken: string;
  refreshToken: string;
  expiresIn: number;    // segundos, p. ej. 3600
  tokenType: string;    // "Bearer"
}
```

### 9.2 Usuario (una sola interfaz, reutilizada en registro, perfil, foto)

Usada por: registro (`POST /users`), perfil propio (`GET/PUT /users/me`), foto de perfil (`POST /users/me/photo`), y también es la forma de respuesta al crear una cuenta STAFF (`POST /users/staff`).

```ts
interface UserResponseDTO {
  id: number;
  email: string;
  phoneNumber: string;
  fullname: string;
  birthdate: string;              // "2002-03-12" (fecha ISO sin hora)
  dni: string;                    // 8 dígitos
  photoUrl: string | null;
  role: "CLIENT" | "STAFF" | "OWNER" | "ADMIN";
}

// Cuerpo de POST /users (multipart/form-data)
interface UserCreateRequest {
  email: string;
  password: string;
  phoneNumber: string;
  fullname: string;
  birthdate: string;   // "YYYY-MM-DD"
  dni: string;
  photo?: File;         // opcional, image/jpeg o image/png
}

// Cuerpo de PUT /users/me (application/json) — no incluye email, es inmutable
interface UserUpdateRequest {
  fullname: string;
  phoneNumber: string;
  birthdate: string;
  dni: string;
}
```

### 9.3 Categorías de evento

```ts
interface EventCategoryResponseDTO {
  id: number;
  name: string;
  description: string | null;
}
```

### 9.4 Evento — ⚠️ DOS variantes distintas según quién consulta

Ambas variantes comparten la mayoría de campos, pero **`ownerFullName` y `categoryName` se comportan de forma opuesta** según el grupo de endpoints. Esto **no es un bug ni es opcional**: es el comportamiento real de la API. Se modelan como dos interfaces distintas para que el tipado obligue a tratarlas correctamente:

```ts
interface EventImageDTO {
  id: number;
  url: string;
  index: number;   // 0 a 7, orden de la galería
}

// Variante A — usada por endpoints de GESTIÓN (OWNER crear/editar/cancelar/listar propios/
// reemplazar imágenes/borrar imágenes, y STAFF listar afiliados/editar descripción-categoría).
// En esta variante, ownerFullName y categoryName SIEMPRE llegan en null.
interface EventManagementDTO {
  id: number;
  title: string;
  description: string;
  maxPeople: number;
  address: string;
  available: boolean;
  finished: boolean;
  startDate: string;      // "2026-08-01T20:00:00"
  finishDate: string;
  ownerId: number;
  categoryId: number;
  ownerFullName: null;
  categoryName: null;
  images: EventImageDTO[];
}

// Variante B — usada SOLO por los endpoints de CLIENT (buscar catálogo y ver detalle).
// En esta variante, ownerFullName y categoryName SIEMPRE llegan poblados (resueltos por la API).
interface EventPublicDTO {
  id: number;
  title: string;
  description: string;
  maxPeople: number;
  address: string;
  available: boolean;
  finished: boolean;
  startDate: string;
  finishDate: string;
  ownerId: number;
  categoryId: number;
  ownerFullName: string;   // p. ej. "Event Owner"
  categoryName: string;    // p. ej. "FIESTAS"
  images: EventImageDTO[];
}

interface PageResponse<T> {
  items: T[];
  total: number;
  page: number;         // zero-based
  pageSize: number;     // siempre 12
  totalPages: number;   // mínimo 1, incluso con 0 resultados
}
```

Cuerpos de request relacionados a eventos:

```ts
// POST /events (multipart/form-data)
interface EventCreateRequest {
  title: string;
  description: string;
  maxPeople: number;
  address: string;
  available: boolean;
  finished: boolean;
  startDate: string;     // "YYYY-MM-DDTHH:mm:ss"
  finishDate: string;
  categoryId: number;
  photos?: File[];       // máx 8, image/jpeg o image/png
}

// PUT /events/{id} (application/json) — sin fotos
interface EventUpdateRequest {
  title: string;
  description: string;
  maxPeople: number;
  address: string;
  available: boolean;
  finished: boolean;
  startDate: string;
  finishDate: string;
  categoryId: number;
}

// PUT /events/{id}/images (multipart/form-data) — REEMPLAZA todo el set de imágenes
interface EventImagesReplaceRequest {
  photos: File[];        // máx 8
}

// DELETE /events/{id}/images (application/json) — borra imágenes puntuales
interface EventImagesDeleteRequest {
  imagesId: number[];
}

// PUT /events/{id}/staff (application/json) — solo estos 2 campos
interface EventStaffUpdateRequest {
  description: string;
  categoryId: number;
}
```

### 9.5 Staff (creación y vínculo con eventos)

```ts
// POST /users/staff (multipart/form-data)
interface StaffCreateRequest {
  email: string;
  password: string;
  phoneNumber: string;
  fullname: string;
  birthdate: string;
  dni: string;
  eventId: number;
  photo?: File;
}
// Respuesta: UserResponseDTO (role: "STAFF")

// POST /users/staff/affiliate  y  DELETE /users/staff/disaffiliate (application/json)
// Mismo cuerpo para ambas operaciones.
interface StaffEventLinkRequest {
  userId: number;
  eventId: number;
}
// Respuesta de ambas: data = null
```

### 9.6 QR / inscripciones / asistentes

```ts
// Respuesta de POST /qr/events/{eventId}/tickets
interface QrTicketResponseDTO {
  id: number;
  userId: number;
  eventId: number;
  qrUrl: string;
  alreadyApplied: boolean;
}

// Respuesta de GET /qr/me/events (array)
interface MyInscriptionsDTO {
  qrUrl: string;
  alreadyApplied: boolean;
  eventName: string;
  eventAddress: string;
  startDate: string;
  endDate: string;
}

// Respuesta de GET /qr/events/{eventId}/assistants (array)
interface EventAssistantResponseDTO {
  id: number;
  userId: number;
  eventId: number;
  registerDate: string;   // "2026-07-13T23:44:47"
}

// Respuesta de POST /qr/tickets/validate
interface QrValidationResponseDTO {
  valid: boolean;
}

// Cuerpo de POST /qr/tickets/validate (multipart/form-data)
interface QrScanRequest {
  qr: File;   // imagen del QR a validar
}
```

---

## 10. Endpoints — GENERAL SIN AUTENTICACIÓN

No requieren `Authorization`. Accesibles antes de iniciar sesión.

| Método | Path | Request | Response (`data`) | Página |
|---|---|---|---|---|
| POST | `/auth/login` | `LoginRequest` | `LoginResponse` | `LoginPage` |
| POST | `/users` | `UserCreateRequest` (multipart) | `UserResponseDTO` (código `201`, `role` siempre `"CLIENT"`) | `RegisterPage`. No devuelve tokens (§5.3): tras el éxito, redirigir a `/login`. |

---

## 11. Endpoints — GENERAL CON AUTENTICACIÓN (cualquier rol: CLIENT, STAFF u OWNER)

Requieren `Authorization: Bearer <accessToken>`, pero cualquiera de los 3 roles puede usarlos.

| Método | Path | Request | Response (`data`) | Página |
|---|---|---|---|---|
| GET | `/events/categories` | — | `EventCategoryResponseDTO[]` | Poblar selects de categoría en filtros (CLIENT) y formularios de evento (OWNER/STAFF). No exige token en el backend, pero en la interfaz solo debe mostrarse/consultarse cuando el usuario ya inició sesión. |
| GET | `/users/me` | — | `UserResponseDTO` | `ProfilePage`, al entrar. |
| PUT | `/users/me` | `UserUpdateRequest` | `UserResponseDTO` | `ProfilePage`, editar datos. |
| POST | `/users/me/photo` | `{ photo: File }` (multipart) | `UserResponseDTO` | `ProfilePhotoUploader`, subir/reemplazar foto. |
| DELETE | `/users/me/photo` | — | `null` | `ProfilePhotoUploader`, quitar foto. |

---

## 12. Endpoints — OWNER

| Método | Path | Request | Response (`data`) | Página |
|---|---|---|---|---|
| POST | `/events` | `EventCreateRequest` (multipart) | `EventManagementDTO` (código `201`) | `OwnerEventCreatePage`. |
| PUT | `/events/{eventId}` | `EventUpdateRequest` | `EventManagementDTO` | `OwnerEventEditPage`, datos del evento. |
| PUT | `/events/{eventId}/images` | `EventImagesReplaceRequest` (multipart) | `EventManagementDTO` | `EventImageManager`. **Reemplaza todo** el set de imágenes existente (no es "agregar"). |
| PUT | `/events/{eventId}/cancel` | — | `EventManagementDTO` (`available: false`) | Botón "Cancelar evento" con confirmación. |
| GET | `/events/me` | — | `EventManagementDTO[]` | `OwnerEventsPage`, listado de eventos propios. |
| DELETE | `/events/{eventId}/images` | `EventImagesDeleteRequest` | `EventManagementDTO` | `EventImageManager`, borra imágenes puntuales por `id` (checkbox múltiple + botón "Eliminar seleccionadas"). |
| POST | `/qr/tickets/validate` | `QrScanRequest` (multipart) | `QrValidationResponseDTO` | `ValidateQrPage`. Endpoint sin restricción de rol a nivel de backend, pero su uso operativo en esta interfaz es para que el OWNER valide asistentes en la puerta. |
| POST | `/users/staff` | `StaffCreateRequest` (multipart) | `UserResponseDTO` (código `201`) | `StaffCreatePage`, crea la cuenta STAFF y la afilia al evento en una sola operación. |
| POST | `/users/staff/affiliate` | `StaffEventLinkRequest` | `null` | Afiliar un STAFF ya existente a otro evento. |
| DELETE | `/users/staff/disaffiliate` | `StaffEventLinkRequest` | `null` | `StaffListPage`, botón "Quitar de este evento". |
| GET | `/qr/events/{eventId}/assistants` | — | `EventAssistantResponseDTO[]` | `EventAssistantsPage`, tabla de asistentes registrados a un evento propio. |

Validaciones de `EventForm` (crear/editar evento):
- `title`: requerido, máximo 45 caracteres.
- `description`: requerido.
- `maxPeople`: requerido, entero positivo.
- `address`: requerido, máximo 65 caracteres.
- `available`, `finished`: booleanos.
- `startDate`, `finishDate`: requeridas, formato `YYYY-MM-DDTHH:mm:ss` (sin zona horaria).
- `categoryId`: requerido, entero positivo, debe existir (la API responde `404` si no).
- `photos`: máximo 8 archivos, cada uno `image/jpeg` o `image/png` (la API responde `400` si excede 8, `415` si el tipo no es válido).

Validaciones de `StaffForm` (crear STAFF): mismas reglas que `UserCreateRequest` (§9.2) más `eventId` (entero positivo, requerido).

---

## 13. Endpoints — STAFF

| Método | Path | Request | Response (`data`) | Página |
|---|---|---|---|---|
| GET | `/events/me/staff` | — | `EventManagementDTO[]` | `StaffEventsPage`, eventos a los que el STAFF está afiliado. |
| PUT | `/events/{eventId}/staff` | `EventStaffUpdateRequest` | `EventManagementDTO` | `EventStaffEditForm`. Solo puede editar `description` y `categoryId`; el formulario no debe mostrar título, fechas, aforo ni ningún otro campo del evento. |
| POST | `/qr/tickets/validate` | `QrScanRequest` (multipart) | `QrValidationResponseDTO` | `ValidateQrPage`. Mismo endpoint que en §12 (sin restricción de rol en backend); aquí el STAFF es quien opera la validación de QR en la puerta del evento. |

Validaciones de `updateStaffFields`: `description` requerida (no vacía); `categoryId` requerido y positivo, debe existir.

> El STAFF **no** tiene acceso al listado de asistentes (`GET /qr/events/{eventId}/assistants` es exclusivo de OWNER). No construir esa vista para STAFF.

---

## 14. Endpoints — CLIENT

| Método | Path | Request | Response (`data`) | Página |
|---|---|---|---|---|
| GET | `/events?title=&startDate=&finishDate=&categoryId=&page=` | query params opcionales salvo `page` (default `0`) | `PageResponse<EventPublicDTO>` | `EventCatalogPage` + `EventFilters` + `Pagination`. El backend fuerza `available=true` en estos resultados, no hace falta enviarlo. |
| GET | `/events/{eventId}` | — | `EventPublicDTO` | `EventDetailPage`. Si el evento no está disponible, la API responde `404`. |
| POST | `/qr/events/{eventId}/tickets` | — (sin body) | `QrTicketResponseDTO` (código `201`) | Botón "Inscribirme" en `EventDetailPage`. Puede fallar con `400` (evento no disponible/finalizado) o `409` (ya registrado / evento lleno) — mostrar `error.message` tal cual. |
| GET | `/qr/me/events` | — | `MyInscriptionsDTO[]` | `MyInscriptionsPage`, lista de tickets QR propios con su estado. |

Paginación: `page` es 0-based, tamaño fijo `12`. Pedir una página fuera de rango responde `400` — tratarlo como "no hay más resultados", no como un error genérico.

---

## 15. Endpoints excluidos de este frontend (uso exclusivo del rol ADMIN)

No construir pantallas para estos:

- `POST /users/owner` (crear cuenta OWNER)
- `POST /users/{id}/owner` (promover CLIENT a OWNER)
- `DELETE /users/{id}` (eliminar cuenta)
- `POST /events/categories` (crear categoría) — la **lectura** (`GET /events/categories`, §11) sí es de uso compartido; solo la creación es exclusiva de ADMIN.

---

## 16. Mapeo de errores

Mostrar siempre `error.message` de la respuesta. Guía de tratamiento en UI según el código HTTP:

| Código | Cuándo ocurre (ejemplos) | Tratamiento sugerido |
|---|---|---|
| 400 | evento no disponible/finalizado al inscribirse; página fuera de rango; más de 8 imágenes; imagen vacía | Mensaje inline en el formulario/acción específica |
| 403 | usuario intentando gestionar eventos que no le pertenecen; STAFF editando un evento al que no está afiliado; OWNER viendo asistentes de un evento ajeno | Ocultar la acción en la UI si el rol no debería verla; si igual ocurre, redirigir a "no autorizado" |
| 404 | evento/categoría/usuario no encontrado | Página o mensaje "no encontrado" |
| 409 | email/dni/teléfono duplicado; usuario ya registrado al evento; evento lleno; categoría duplicada | Mensaje inline, sin bloquear el resto del formulario |
| 415 | tipo de imagen no soportado (solo jpg/jpeg/png) | Validar el `accept` del `<input type="file">` en cliente antes de enviar, y repetir el mensaje si igual falla en el servidor |
| 500/502 | fallo interno del servidor | Mensaje genérico "Ocurrió un error, intenta de nuevo" + botón reintentar |

---

## 17. Rutas por rol (árbol de navegación)

```
/login                          (público)
/register                       (público)

/client/events                  (CLIENT) — catálogo + filtros + paginación
/client/events/:id               (CLIENT) — detalle + botón inscribirme
/client/my-tickets                (CLIENT) — mis inscripciones (QR)
/client/profile                   (CLIENT) — compartido

/owner/events                    (OWNER) — listado propio
/owner/events/new                (OWNER) — crear evento
/owner/events/:id                (OWNER) — editar, cancelar, imágenes
/owner/events/:id/assistants      (OWNER) — asistentes registrados
/owner/staff                     (OWNER) — listado + crear + afiliar/desafiliar STAFF
/owner/scan                       (OWNER) — validar QR en puerta
/owner/profile                    (OWNER) — compartido

/staff/events                    (STAFF) — eventos afiliados
/staff/events/:id                 (STAFF) — editar solo descripción/categoría
/staff/scan                       (STAFF) — validar QR en puerta
/staff/profile                    (STAFF) — compartido
```

---

## 18. Orden sugerido de implementación

1. Scaffold del proyecto (Vite + React 19 + TS), estructura de carpetas de §7, `httpClient` + tipo `ApiResponse<T>`.
2. `AuthContext` + `LoginPage` + `RegisterPage` + `RoleGuard` + layouts vacíos por rol.
3. Módulo de perfil compartido (get/update/foto) — primera pantalla útil tras login para validar todo el flujo de sesión.
4. Módulo de eventos para CLIENT (catálogo + detalle + paginación + filtros), usando `EventPublicDTO`.
5. Módulo QR para CLIENT (inscribirse + mis tickets).
6. Módulo de eventos para OWNER (crear/editar/cancelar/imágenes, usando `EventManagementDTO`) + módulo de staff + módulo QR (asistentes).
7. Módulo de eventos para STAFF + módulo QR (validar QR).
8. Pulido responsive, estados vacíos/error, accesibilidad básica (labels, contraste, foco visible).

---

## 19. Checklist de build/verificación

- `npm install`
- `npm run build` debe compilar sin errores de TypeScript antes de dar por terminada cualquier tarea.
- `npm run lint` sin errores.
- No hay infraestructura de testing definida en este plan; tampoco lo agregues, no se necesita ni se quiere.
