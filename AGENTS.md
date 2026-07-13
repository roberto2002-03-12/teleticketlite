# AGENTS.md — AI Agent Context for `teleticketlite`

This file is the canonical context for any AI coding agent (Cursor, opencode, Copilot, etc.) working on this project. Read it before making any change. Do not invent, lie, or guess; when something is missing or wrong, fix this file first.

---

## 1. Project Summary

**Teleticket Lite** — backend API for an event-ticketing platform. Users register, owners create events, staff run events, clients buy tickets. Single source of truth is this repo; `plan/` holds product specs.

- **Framework:** Quarkus 3.37.1 (Jakarta EE 10, MicroProfile 6, Java 21)
- **Build:** Maven (`./mvnw.cmd` on Windows, `./mvnw` elsewhere)
- **DB:** MySQL 8.0, schema `teleticketlite` (8 tables — see `plan/ARCHITECTURE.MD`)
- **AWS:** Cognito (auth + user management), S3 (profile pictures), Lambda target (deployment, not built here)
- **GroupId / package base:** `org.teleticketlite.project` / `com.app.teleticket` (note the mismatch — the Maven groupId is the team namespace, Java package base is the per-module root)

---

## 2. Source Tree

```
.
├── AGENTS.md                  ← this file
├── README.md                  ← Quarkus scaffold boilerplate, do not treat as design doc
├── pom.xml
├── plan/
│   ├── PLAN.md                ← product requirements (use cases, roles)
│   ├── ARCHITECTURE.MD        ← schema DDL + module structure
│   ├── AWS_RESOURCES.MD       ← Cognito user pool / app client config
│   ├── CORRECTION.MD          ← authoritative corrections to PLAN.md (read this too)
│   └── postman/
│       └── teleticket-lite.postman_collection.json
└── src/main/
    ├── docker/                ← Dockerfile variants (ignore unless asked)
    └── java/com/app/teleticket/
        ├── auth/              ← auth module
        ├── common/            ← shared DTOs and exception mappers (cross-cutting)
        ├── events/            ← events module
        └── users/             ← users module
```

> ⚠️ `plan/ARCHITECTURE.MD` still shows the original Spanish-named scaffolding (`Usuario*`) and an early package layout. It is out of date — use this file and the actual `src/main/java` tree as the authoritative source of structure.

Modules planned but **not yet implemented**: `qr`, `assistance`. Do not create stubs for them unless asked.

---

## 3. Conventions (must follow)

### 3.1 Language

**All code in English.** No Spanish class names, no Spanish comments, no Spanish identifiers. (Earlier iterations used `Usuario*` — fully renamed to `User*`.)

The single non-English identifier that remains is `dni` (Peruvian national-ID field name, used as-is in the DB column `user.dni CHAR(8)` and in the spec). Treat it as a domain term, not a localization issue.

### 3.2 Package layout per module

Every domain module follows this structure (see `plan/ARCHITECTURE.MD`):

```
<module>/
  controller/   JAX-RS resources (@Path, HTTP handling)
  dto/          request/response objects (DTOs are classes, form DTOs are classes)
  entity/       JPA entities (Panache)
  exception/    module-specific exceptions + ExceptionMappers
  repository/   PanacheRepository interfaces
  service/      service interfaces
  service/impl/ service implementations
  config/       module-scoped producers (e.g. SDK clients)
  utils/        mappers, helpers
```

### 3.3 Service pattern

- Every service is an **interface** in `service/`, with implementation in `service/impl/`.
- Class name = `<Domain><Role/Purpose>Service` + `Impl` suffix on the implementation.
- Concrete classes (no interface) in `service/` are a violation of project rules.

### 3.4 User-role split

The `users` module has **five role-specific services** (one per use-case group), all delegating to a shared `UserCreationSupport` bean for the Cognito + DB + compensation flow:

| Service | Responsibility | Endpoint callers |
|---|---|---|
| `UserClientService` | register a `CLIENT` | public (`POST /users`) |
| `UserStaffService` | register a `STAFF` + affiliate to event, desaffiliate | `OWNER` / `ADMIN` |
| `UserOwnerService` | register an `OWNER` | `ADMIN` only |
| `UserAdminService` | delete account | `ADMIN` only |
| `UserProfileService` | getMe / updateMe / photo upload + delete | any authenticated role |

Do not merge these. Each class is one role/purpose (per `CORRECTION.MD` §3).

### 3.5 HTTP boundary rules

- All create endpoints accept `multipart/form-data` (not JSON) so the photo file can travel in the same request. Form DTOs (`UserCreateForm`, `UserStaffCreateForm`, `UserOwnerCreateForm`, `UserPhotoForm`) carry `@RestForm` fields (`org.jboss.resteasy.reactive.RestForm`) + `FileUpload photo`. Resource methods bind these forms with `@BeanParam` (not the deprecated `@MultipartForm`).
- `UserCreateForm` / `UserStaffCreateForm` also carry a `password` field (`@NotBlank @Size(min = 8, max = 32)`), forwarded to `UserCreationSupport` for both the DB (hashed) and Cognito (permanent) password — see §3.6.
- Update endpoints (`PUT /users/me`) accept JSON.
- Service methods receive `byte[] photo, String contentType` — never `FileUpload` in the service layer. The controller converts multipart to bytes.
- Validation: `@Valid` on the form DTO; constraints declared on each `@RestForm` field only if for formdata forms, json forms skipt it.
- Auth: `@RolesAllowed("OWNER")` etc. on role-gated endpoints. Public endpoints (e.g. `POST /users`) have no role annotation. Token validation is automatic via quarkus-oidc.
- Errors: throw module exception (`UserException` / `AuthException`) — mapped by the module's `ExceptionMapper` to an `ApiResponse` error envelope (see §9). **Do not** return JAX-RS `Response` from service methods.

### 3.6 Cognito + DB compensation flow

`UserCreationSupport.create(dto, role, photo, contentType)` is the only place that creates a user. Order is:

1. Validate uniqueness (`email`, `dni`, `phoneNumber`) → `409` on conflict.
2. Call `CognitoUserService.adminCreateUser(email, phone, role, password)` (AdminCreateUser + AdminAddUserToGroup + AdminSetUserPassword permanent).
3. Persist `UserEntity` to DB, `flush()` to get the generated `id`.
4. If `photo != null && photo.length > 0`, upload to S3, set `photoUrl` / `photoKeyName`, `flush()` again.
5. **On any RuntimeException** in steps 3–4: catch, call `CognitoUserService.adminDeleteUser(email)`, rethrow. The DB transaction rolls back automatically.

Never call `cognito.adminCreateUser` outside `UserCreationSupport`. Never delete a Cognito user from anywhere except `CognitoUserService` / `UserAdminServiceImpl`.

### 3.7 Identifiers

- Class names: PascalCase (`UserClientServiceImpl`).
- Methods/fields: camelCase.
- Constants: `UPPER_SNAKE`.
- Tables/columns: snake_case mapped via `@Column(name = "...")` when the JPA field name differs.

---

## 4. The `auth` module

```
auth/
  controller/AuthResource.java       POST /auth/login
  dto/LoginRequest.java              { email, password } — @NotBlank both
  dto/LoginResponse.java             { accessToken, idToken, refreshToken, expiresIn, tokenType }
  exception/AuthException.java       status + message
  exception/AuthExceptionMapper.java maps to `ApiResponse` error envelope
  service/AuthService.java           interface — currentEmail() (extracts "email" claim from JsonWebToken)
  service/AuthLoginService.java      interface — login(LoginRequest) → LoginResponse
  service/impl/AuthServiceImpl.java
  service/impl/AuthLoginServiceImpl.java  Cognito InitiateAuth (USER_PASSWORD_AUTH) + SECRET_HASH
```

`AuthService.currentEmail()` is the **only** method other modules should call to identify the authenticated user (used by `UserProfileService` via `UserClientResource`).

`AuthLoginServiceImpl` computes `SECRET_HASH = Base64(HMAC-SHA256(username + clientId, clientSecret))` because the backend app client has a `client-secret` (`ALLOW_USER_PASSWORD_AUTH` flow). Required for any `InitiateAuth` against this client.

---

## 5. The `users` module

### 5.1 Entity layer

| File | Notes |
|---|---|
| `entity/UserEntity.java` | `@Entity @Table(name = "user")`. `id` uses `@Id @GeneratedValue(strategy = IDENTITY)` for MySQL `AUTO_INCREMENT`. |
| `entity/EventOwnerEntity.java` | `@Entity @Table(name = "event_owner")`. `idEventOwner` uses `IDENTITY`; stores `ruc`, `enabled`, `userId`. Created automatically when an OWNER account is registered and when an existing user is promoted to OWNER. |
| `entity/StaffEntity.java` | Composite PK via `@IdClass(StaffEntity.StaffId)`. Fields: `idStaff`, `userId`, `eventId`. `idStaff` has `@GeneratedValue(strategy = IDENTITY)` (Hibernate accepts this inside `@IdClass` with MySQL); `UserStaffServiceImpl` does **not** assign it manually. `StaffRepository.findMaxIdStaff()` exists but is currently unused. |
| `entity/StaffEntity.StaffId` | inner class, `Serializable` + `equals` + `hashCode` over all 3 fields. |

### 5.2 DTOs

| File | Purpose |
|---|---|
| `dto/UserCreateDTO.java` | Plain DTO used internally by services (`email`, `phoneNumber`, `fullname`, `birthdate`, `dni`, `password`). Built from the form in the controller. |
| `dto/UserCreateForm.java` | `multipart/form-data` form for `POST /users` (CLIENT). |
| `dto/UserStaffCreateDTO.java` | Extends `UserCreateDTO`, adds `eventId`. |
| `dto/UserStaffCreateForm.java` | multipart form for `POST /users/staff`. |
| `dto/UserOwnerCreateDTO.java` | Extends `UserCreateDTO`, adds `ruc`. |
| `dto/UserOwnerCreateForm.java` | multipart form for `POST /users/owner` (ADMIN only). |
| `dto/UserOwnerChangeDTO.java` | Plain DTO with `ruc` used when promoting an existing user to OWNER. |
| `dto/UserOwnerChangeForm.java` | JSON form for `POST /users/{id}/owner` (ADMIN only). |
| `dto/UserUpdateDTO.java` | JSON body for `PUT /users/me` (no `email` — identity is immutable for the lifetime of the token). |
| `dto/UserPhotoForm.java` | multipart form with a single `FileUpload photo` for `POST /users/me/photo`. |
| `dto/UserResponseDTO.java` | record returned by every user endpoint. |
| `dto/DisaffiliateStaffEventRequest.java` | JSON body for staff affiliate/disaffiliate endpoints (`userId`, `eventId`). |

### 5.3 Repository layer

- `repository/UserRepository.java` — `PanacheRepository<UserEntity>`. Methods: `findByEmail`, `findByDni`, `findByPhoneNumber`, `existsByEmail/Dni/PhoneNumber`. **Always parameterized**; never concatenate user input.
- `repository/StaffRepository.java` — `PanacheRepository<StaffEntity>`. Methods: `findByUserAndEvent`, `deleteByUserAndEvent`, `deleteByUser`, `findMaxIdStaff` (currently unused).
- `repository/EventOwnerRepository.java` — `PanacheRepository<EventOwnerEntity>`. Methods: `findByUserId`, `deleteByUserId`.

### 5.4 Service layer

- `service/CognitoUserService.java` + `impl/CognitoUserServiceImpl.java` — wrapper around `CognitoIdentityProviderClient` (`adminCreateUser`, `adminDeleteUser`, `addToGroup`). Translates `CognitoIdentityProviderException` → `UserException(502)`.
- `service/UserPhotoStorageService.java` + `impl/UserPhotoStorageServiceImpl.java` — S3 wrapper. Bucket name via `s3.bucket-name`, region via `quarkus.s3.aws.region`. Key prefix: `profile-pictures/{userId}/{uuid}.{ext}`. Public URL pattern: `https://{bucket}.s3.{region}.amazonaws.com/{key}`. jpg/jpeg/png only (`image/jpeg` → `.jpg`, `image/png` → `.png`).
- `service/UserCreationSupport.java` — shared `@Transactional` flow described in §3.6. Injected by the three create services.
- `service/UserClientService.java` / `UserStaffService.java` / `UserOwnerService.java` / `UserAdminService.java` / `UserProfileService.java` — role-specific services described in §3.4. All have `Impl` in `service/impl/`.
- `UserOwnerService` has two methods: `create(UserOwnerCreateDTO, photo, contentType)` registers a new OWNER and creates the `event_owner` row; `changeToOwner(Long userId, UserOwnerChangeDTO)` promotes an existing user to OWNER and creates the `event_owner` row.
- `UserStaffServiceImpl.create` and `UserOwnerServiceImpl.create` each add their own post-creation compensation: if the DB step after `UserCreationSupport.create` fails (staff affiliation or `event_owner` row), they delete the already-created Cognito user (and the S3 photo for staff) before rethrowing.

### 5.5 Resource layer

Split by role instead of one unified controller (see §3.4; a single `UserResource` mixing all 5 services was a code-review violation, now fixed):

| File | Path prefix | Endpoints |
|---|---|---|
| `controller/UserClientResource.java` | `/users` | `POST /users` (register CLIENT, public); `GET/PUT /users/me`, `POST/DELETE /users/me/photo` (self-service, CLIENT/STAFF/OWNER/ADMIN) |
| `controller/UserStaffResource.java` | `/users` | `POST /users/staff` (OWNER, ADMIN); `POST /users/staff/affiliate` (OWNER, ADMIN); `POST /users/staff/disaffiliate` (OWNER, ADMIN) |
| `controller/UserOwnerResource.java` | `/users` | `POST /users/owner` (ADMIN); `POST /users/{id}/owner` (ADMIN, promote existing user to OWNER) |
| `controller/UserAdminResource.java` | `/users` | `DELETE /users/{id}` (ADMIN) |

The shared self-service `/me` endpoints (any authenticated role, backed by `UserProfileService`) live in `UserClientResource` since `/users` is their natural base path — there is no 5th controller for them. Map form DTOs to internal DTOs in the controller (via the shared `utils/UserFormMapper` helper); never let a `FileUpload` reach the service layer.

### 5.6 Exception layer

`exception/UserException.java` (status + message) + `exception/UserExceptionMapper.java` (→ `ApiResponse` error envelope). Status codes used:

| Code | Meaning |
|---|---|
| 400 | malformed request / empty photo bytes |
| 404 | user not found by email/id; staff affiliation not found |
| 409 | email / dni / phone already in use; staff already affiliated |
| 415 | photo content-type not in {image/jpeg, image/png} |
| 502 | AWS SDK failure (Cognito or S3) — wraps the SDK message |

---

## 6. The `events` module

Implements `event`, `event_category`, `event_images` tables (see `plan/ARCHITECTURE.MD`). Roles: CLIENT read active events, OWNER/ADMIN full CRUD on owned events, STAFF view affiliated + edit only description/category, ADMIN also creates categories.

### 6.1 Entity layer

| File | Notes |
|---|---|
| `entity/EventEntity.java` | `@Entity @Table(name = "event")`. Does **NOT** extend `PanacheEntity` — plain JPA entity with private fields + getters/setters (matches the `users/entity/` style). `id` is `@Id @GeneratedValue(strategy = IDENTITY) int` (MySQL `AUTO_INCREMENT`). `description` mapped `columnDefinition = "TEXT"`. `ownerId` / `categoryId` are plain Integer FK columns (no JPA relations) to avoid N+1 and lazy-load pitfalls. All field access via getters/setters, never direct field access. |
| `entity/EventCategoryEntity.java` | Same style — plain `@Entity`, private fields + accessors. `id` is `int IDENTITY`. Fields: `name`, `description`. |
| `entity/EventImageEntity.java` | Same style. `id` is `int IDENTITY`. `keyName` mapped to column `` `key` `` (backtick-quoted because `key` is reserved in MySQL), `index` mapped to `` `index` `` (also reserved), `eventId` (Integer FK). All access via getters/setters. |

### 6.2 DTOs

| File | Purpose |
|---|---|
| `dto/EventCreateForm.java` | `multipart/form-data` form for `POST /events`. `@RestForm` fields + `List<FileUpload> photos` (up to 8, enforced in service). |
| `dto/EventCreateDTO.java` | Internal DTO built from the form in the controller. Carries `List<EventImageInput> photos`. No validation annotations (the form has them). |
| `dto/EventImageInput.java` | `record(byte[] bytes, String contentType)` — passes parsed photo bytes to the service so `FileUpload` stays in the controller. |
| `dto/EventUpdateDTO.java` | JSON body for `PUT /events/{id}` (OWNER/ADMIN, full event field update). |
| `dto/EventStaffUpdateDTO.java` | JSON body for `PUT /events/{id}/staff` (STAFF — only `description` + `categoryId`). |
| `dto/EventCategoryCreateDTO.java` | JSON body for `POST /events/categories` (ADMIN). |
| `dto/EventResponseDTO.java` | record with all event fields + embedded `List<EventImageResponseDTO> images`. |
| `dto/EventImageResponseDTO.java` | record `{ id, url, index }`. |
| `dto/EventCategoryResponseDTO.java` | record `{ id, name, description }`. |
| `dto/PageResponse.java` | generic `record<T>(List<T> items, long total, int page, int pageSize, int totalPages)` used for paginated CLIENT search results. |
| `dto/EventImageReplaceForm.java` | `multipart/form-data` form for `PUT /events/{id}/images`. Carries `List<FileUpload> photos` (up to 8, enforced in service). |
| `dto/EventImagesDeleteRequest.java` | JSON body for `DELETE /events/{id}/images`. Carries `List<Integer> imagesId`. |

### 6.3 Repository layer

- `repository/EventRepository.java` — `searchActive`/`countActive` (CLIENT search with filters: `title` LIKE, `startDate>=`, `finishDate<=`, `categoryId=`, always `available = true`); uses **`Map<String, Object>`** for parameterized queries (NOT the deprecated `io.quarkus.panache.common.Parameters` — that overload is deprecated since Quarkus 3.34); `findByOwnerId`; `findEventsForStaffUser(userId)` (cross-module JPQL through `StaffEntity` from the `users` module — works because both entities are in the same persistence unit); declares an overload `findById(Integer)` returning `Optional` so callers can use Integer path params — `PanacheRepository<Entity>` defaults the id type to `Long`, so you must add a custom `findById(Integer)` overload whenever the entity id is `int`/`Integer`.
- `repository/EventCategoryRepository.java` — `findByName` (uniqueness check on create); also declares `findById(Integer)` returning `Optional` (same Long-vs-Integer reason as above).
- `repository/EventImageRepository.java` — `findByEventId`, `findByEventIdAndIdIn`, `deleteByEventId`, `deleteByEventIdAndIdIn`.

### 6.4 Service layer

All services are interfaces in `service/` with impl in `service/impl/`, split by role/use-case group:

| Service | Responsibility | Endpoint callers |
|---|---|---|
| `EventOwnerService` | create (with images), update owned, cancel owned, list own, replace all images, delete selected images | `OWNER`, `OWNER`+`ADMIN` |
| `EventStaffService` | list affiliated events, update description+category only on affiliated events | `STAFF` |
| `EventClientService` | paginated search of active events (12/page, page out of range → 400), select one active event by id | `CLIENT` |
| `EventCategoryService` | create category (admin), list all categories | `ADMIN` create; any auth role list |
| `EventImageStorageService` | S3 wrapper: `upload(eventId, index, contentType, bytes)`, `delete(key)`, `deleteAll(keys)` for compensation. Key prefix: `event-images/{eventId}/{index}-{uuid}.{jpg|png}`. jpg/jpeg/png only. |

### 6.5 Resource layer

| File | Path prefix | Endpoints |
|---|---|---|
| `controller/EventResource.java` | `/events` | `POST /events` (OWNER, ADMIN, multipart); `PUT /events/{id}` (OWNER, ADMIN, JSON); `PUT /events/{id}/cancel` (OWNER, ADMIN); `GET /events/me` (OWNER, ADMIN); `GET /events/me/staff` (STAFF); `PUT /events/{id}/staff` (STAFF, JSON); `GET /events` (CLIENT, query params + pagination); `GET /events/{id}` (CLIENT); `PUT /events/{id}/images` (OWNER, ADMIN, multipart); `DELETE /events/{id}/images` (OWNER, ADMIN, JSON) |
| `controller/EventCategoryResource.java` | `/events/categories` | `POST /events/categories` (ADMIN, JSON); `GET /events/categories` (any auth role) |

### 6.6 Owner resolution

`owner_id` is **NOT** sent by the client. The controller calls `AuthService.currentEmail()`, passes it to `EventOwnerService`, which resolves:
1. `UserRepository.findByEmail(email)` → `UserEntity.id`
2. `EventOwnerRepository.findByUserId(userId)` → `EventOwnerEntity.idEventOwner`

If the caller has no `event_owner` row → `403 "Current user is not an event owner"` (via `EventException`, not `UserException`).

### 6.7 Image upload + compensation

`EventOwnerServiceImpl.create`:
1. Validate `photos.size() ≤ 8` and category exists.
2. Persist `EventEntity`, `flush()` to get the generated `id`.
3. For each photo (in received order, index 0..N-1): validate content-type, upload to S3, persist `EventImageEntity` with `index` and `eventId`. Track uploaded S3 keys.
4. If any photo step fails: delete already-uploaded S3 keys (best-effort), then rethrow — the `@Transactional` boundary rolls back the DB rows (event + image entities).

`EventOwnerServiceImpl.replaceImages`:
1. Validate `photos.size() ≤ 8` and that the event exists and belongs to the caller.
2. Upload every new photo to S3 first (validates content-type and non-empty bytes). Keep the list of newly uploaded S3 keys.
3. Inside the same `@Transactional` boundary: load all old `EventImageEntity` rows for the event, delete them from the DB, then persist the new rows and `flush()`.
4. Delete the old S3 keys.
5. If any RuntimeException happens during the DB work or old-key deletion, delete the newly uploaded S3 keys (best-effort) and rethrow — the transaction rolls back, restoring the old image rows.

`EventOwnerServiceImpl.deleteImages`:
1. Verify the event exists and belongs to the caller.
2. Load the requested image rows by `eventId` and `id in imagesId`; return `404` if any ID is missing.
3. Inside `@Transactional`: delete the selected rows from the DB, then delete their S3 keys. If the S3 call fails, the transaction rolls back and the rows remain.

### 6.8 Pagination rules

- 12 events per page (constant `PAGE_SIZE` in `EventClientServiceImpl`).
- `available = true` is forced server-side for all CLIENT search/list calls.
- Page index is zero-based via `@QueryParam("page") @DefaultValue("0")`.
- Out-of-range pages → `400 "Page N is out of range (0..M)"`. A search returning zero events reports `totalPages = 1` so `page = 0` is still valid.

### 6.9 Exception layer

`exception/EventException.java` (status + message) + `exception/EventExceptionMapper.java` → `ApiResponse` envelope. Separately declared from `UserException`/`AuthException` — do not unify (matches the per-module rule).

Status codes used:

| Code | Meaning |
|---|---|
| 400 | too many images (>8); empty image bytes; page out of range |
| 403 | non-owner trying to manage an event they don't own; non-staff trying to edit an event they're not affiliated to; caller has no `event_owner` row |
| 404 | event / category not found |
| 409 | category name already in use |
| 415 | photo content-type not in {image/jpeg, image/png} |
| 502 | S3 SDK failure |

---

## 7. Endpoints (current state)

| Method | Path | Auth | Body | Service method |
|---|---|---|---|---|
| POST | `/auth/login` | public | JSON `{ email, password }` | `AuthLoginService.login` |
| POST | `/users` | public | multipart (form + optional photo) | `UserClientService.create` |
| POST | `/users/staff` | `OWNER` or `ADMIN` | multipart (+ `eventId`) | `UserStaffService.create` |
| POST | `/users/staff/affiliate` | `OWNER` or `ADMIN` | JSON `{ userId, eventId }` | `UserStaffService.affiliate` |
| POST | `/users/staff/disaffiliate` | `OWNER` or `ADMIN` | JSON `{ userId, eventId }` | `UserStaffService.desaffiliate` |
| POST | `/users/owner` | `ADMIN` | multipart (+ `ruc`) | `UserOwnerService.create` |
| POST | `/users/{id}/owner` | `ADMIN` | JSON `{ ruc }` | `UserOwnerService.changeToOwner` |
| GET | `/users/me` | CLIENT / STAFF / OWNER / ADMIN | — | `UserProfileService.getMe` |
| PUT | `/users/me` | CLIENT / STAFF / OWNER / ADMIN | JSON | `UserProfileService.updateMe` |
| POST | `/users/me/photo` | CLIENT / STAFF / OWNER / ADMIN | multipart (single file) | `UserProfileService.uploadPhoto` |
| DELETE | `/users/me/photo` | CLIENT / STAFF / OWNER / ADMIN | — | `UserProfileService.deletePhoto` |
| DELETE | `/users/{id}` | `ADMIN` | — | `UserAdminService.deleteAccount` |
| POST | `/events` | `OWNER` or `ADMIN` | multipart (form + up to 8 photos) | `EventOwnerService.create` |
| PUT | `/events/{id}` | `OWNER` or `ADMIN` | JSON | `EventOwnerService.update` |
| PUT | `/events/{id}/cancel` | `OWNER` or `ADMIN` | — | `EventOwnerService.cancel` |
| GET | `/events/me` | `OWNER` or `ADMIN` | — | `EventOwnerService.listOwn` |
| GET | `/events/me/staff` | `STAFF` | — | `EventStaffService.listAffiliated` |
| PUT | `/events/{id}/staff` | `STAFF` | JSON | `EventStaffService.updateStaffFields` |
| GET | `/events` | `CLIENT` | query params + pagination | `EventClientService.search` |
| GET | `/events/{id}` | `CLIENT` | — | `EventClientService.getActiveById` |
| PUT | `/events/{id}/images` | `OWNER` or `ADMIN` | multipart (up to 8 photos) | `EventOwnerService.replaceImages` |
| DELETE | `/events/{id}/images` | `OWNER` or `ADMIN` | JSON `{ imagesId }` | `EventOwnerService.deleteImages` |
| POST | `/events/categories` | `ADMIN` | JSON | `EventCategoryService.create` |
| GET | `/events/categories` | any authenticated role | — | `EventCategoryService.list` |

---

## 8. Configuration (`src/main/resources/application.properties`)

Read these before touching anything that talks to AWS or Cognito.

### Cognito (always active, used by the SDK in dev AND prod)

```
cognito.user-pool-id=us-east-1_s33irHMjI
cognito.client-id=ffamd2su02og450pl37jm17q8
cognito.client-secret=1t897uoild09is4e1ps87s9l3e6tescfss01nkp92vsb9ks65kka
cognito.region=us-east-1
```

- `user-pool-id` → every Admin API call.
- `client-id` / `client-secret` → `SECRET_HASH` for `InitiateAuth` (the `auth/login` endpoint). Required because the client has a secret and the pool allows `USER_PASSWORD_AUTH`.
- `region` → builds the `CognitoIdentityProviderClient`.

### OIDC (token validation, currently active in all profiles)

```
quarkus.oidc.auth-server-url=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_s33irHMjI
quarkus.oidc.client-id=ffamd2su02og450pl37jm17q8
quarkus.oidc.application-type=service
quarkus.oidc.token.issuer=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_s33irHMjI
quarkus.oidc.roles.role-claim-path=cognito:groups
```

> ⚠️ `CORRECTION.MD` recommended scoping these under `%dev.` so the Lambda build doesn't double-validate (API Gateway's `COGNITO_USER_POOLS` authorizer handles it). This refactor was not applied — keep all five lines un-prefixed unless explicitly told to revert to `%dev.`.

> ⚠️ The access token issued by Cognito does **not** carry `cognito:groups`. Only the **ID token** does. For local testing the client must send the **ID token** in `Authorization: Bearer`, or every `@RolesAllowed` will fail silently (no groups, looks like a permissions bug, not an auth error).

### AWS credentials (currently long-lived IAM)

```
quarkus.s3.aws.region=us-east-1
quarkus.s3.devservices.enabled=false
quarkus.s3.aws.credentials.type=static
quarkus.s3.aws.credentials.static-provider.access-key-id=...
quarkus.s3.aws.credentials.static-provider.secret-access-key=...
aws.credentials.access-key-id=...
aws.credentials.secret-access-key=...
s3.bucket-name=teleticket-lite-images-561547870320
```

- The two blocks must be kept **in sync** — the quarkiverse S3 extension reads `quarkus.s3.aws.credentials.*`, while the raw SDK `CognitoIdentityProviderClient` (produced in `UserConfig`) reads `aws.credentials.*`.
- The current keys are **long-lived IAM keys** (`AKIA...` prefix, no session token). `UserConfig.java` builds `CognitoIdentityProviderClient` with `AwsBasicCredentials`. If the user later switches to STS temporary credentials (`ASIA...` prefix + session token), the implementation must change to `AwsSessionCredentials` and add the `aws.credentials.session-token` property.
- `quarkus.s3.devservices.enabled=false` disables the local S3 dev service so the app talks to the real S3 bucket.

### DB

```
quarkus.datasource.db-kind=mysql
quarkus.datasource.username=root
quarkus.datasource.password=password
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/teleticketlite
quarkus.hibernate-orm.log.sql=false
```

- There is no `quarkus.hibernate-orm.database.generation` setting in the current properties. Hibernate defaults apply; add `quarkus.hibernate-orm.database.generation=update` for dev if you want schema auto-creation, but replace with Flyway/Liquibase for prod.

---

## 9. Build & Verify (skill-mandated)

Before any change, run **`./mvnw.cmd clean compile`** (Windows) or **`./mvnw clean compile`** (Unix). After changes, run **`./mvnw.cmd clean verify`**. Both must end with `BUILD SUCCESS`.

There are no tests yet. `mvn verify` runs Quarkus augmentation (build-time CDI / OIDC / Hibernate validation) — a failure there is meaningful, not a no-op.

`mvn clean` will fail if a dev server holds `target/teleticketlite-dev.jar`. Stop the dev server first.

---

## 10. Known gotchas (do not trip on these again)

- **`@MultipartForm` was deprecated** in Quarkus 3.37.1 (marked for removal) and has been replaced project-wide. Form beans (`UserCreateForm`, `UserStaffCreateForm`, `UserOwnerCreateForm`, `UserPhotoForm`) use `@RestForm` (`org.jboss.resteasy.reactive.RestForm`) on each field, and resource methods bind them with `@BeanParam` instead of `@MultipartForm`. Do not reintroduce `@FormParam`/`@MultipartForm`.
- **Passwords**: DB stores a bcrypt hash (`io.quarkus.elytron.security.common.BcryptUtil.bcryptHash(...)`, computed in `UserMapper.toEntity`), never the plaintext. Cognito gets the same plaintext password set as **permanent** via `AdminSetUserPassword` inside `CognitoUserServiceImpl.adminCreateUser` (called right after `AdminAddUserToGroup`, still inside the same try/catch so a failure triggers the usual Cognito cleanup path). Requires the `quarkus-elytron-security-common` dependency (added to `pom.xml`) — do not add a second bcrypt library.
- **`UserException` vs `AuthException`** — they have identical shape but are separate classes per module. Do not unify; do not share a mapper.
- **Error response format** — module mappers now return `com.app.teleticket.common.dto.ApiResponse` envelopes: `{ "status": "error", "code": N, "error": { "message": "...", "stack": null }, "data": null }`. Do not expect a flat `{ "error": "..." }` response.
- **`dni` field** is a domain term (Peruvian national ID), not a localization problem. Keep it.
- **Self-invocation of `@Transactional`** is forbidden. `UserCreationSupport` is the transactional unit; the three create services call into it (not `@Transactional` themselves for the create method). Don't add `@Transactional` to `UserClientService.create` etc.
- **Photo upload order**: `persist` + `flush` first (to get the generated `id`), then upload to S3, then set `photoUrl`/`photoKeyName` and `flush` again. S3 key includes the user id.
- **Staff `id_staff` is auto-generated** — `StaffEntity.idStaff` has `@GeneratedValue(strategy = IDENTITY)` inside the `@IdClass`. `UserStaffServiceImpl` does not assign it manually. `StaffRepository.findMaxIdStaff()` is dead code.
- **OWNER registration creates an `event_owner` row.** `UserOwnerServiceImpl.create` persists an `EventOwnerEntity(ruc, enabled=true, userId)` after `UserCreationSupport.create`. `UserOwnerServiceImpl.changeToOwner` also creates the row when promoting an existing user. `UserAdminServiceImpl.deleteAccount` deletes the row before deleting the user.
- **Hardcoded IAM keys in `application.properties`** are long-lived (`AKIA...`). They do not expire like STS tokens, but they should still be rotated and never logged or committed elsewhere.
- **Spanish comments remain in `users/config/UserConfig.java`** (`"USAR CUANDO PASES A LABROLE"`). This violates §3.1; replace with English comments if you edit that file.
- **Entities must NOT extend `PanacheEntity`** and must NOT use public fields. Every entity is a plain `@Entity` with private fields + getters/setters (see `users/entity/` for the canonical style). Repositories `implements PanacheRepository<Entity>` still work with plain entities — `PanacheRepository` doesn't require the entity to extend `PanacheEntity`. Access all entity state via getters/setters, never direct field access.
- **No deprecated Panache APIs.** `find(String, Parameters)`, `count(String, Parameters)`, `list(String, Parameters)`, `stream(String, Parameters)` are deprecated since Quarkus 3.34. Use `find(String, Map<String, Object>)` / `count(String, Map<String, Object>)` instead. Build the map with `new HashMap<>()` and `params.put(...)`.
- **PanacheRepository defaults the id type to Long.** When an entity's `@Id` is `int`/`Integer` (MySQL `INT AUTO_INCREMENT`), the inherited `findById(Long)` won't accept an Integer. Each repository for such entities must declare its own `Optional<Entity> findById(Integer id)` overload (using `find("id", id).firstResultOptional()`).
- **No deprecated packages at all.** Before using any Quarkus/Panache API, check whether it is marked `@Deprecated` in the current Quarkus version. If so, find the replacement. Deprecated APIs are forbidden.

---

## 11. Postman collection

`plan/postman/teleticket-lite.postman_collection.json` (v2.1.0). Import in Postman. Variables: `{{baseUrl}}` (default `http://localhost:8080`) and `{{idToken}}` (auto-set by the login request's test script from `response.idToken`). Photo file fields have empty `src` — pick a file in the Postman UI before sending.

The ID token is what must be sent as `Authorization: Bearer {{idToken}}` for `@RolesAllowed` to find the user's groups (see §7 warning).

---

## 12. Module map for future work

| Module | Status | Touch these |
|---|---|---|
| `auth` | done (login + identity) | add refresh-token flow when needed |
| `users` | done | photo upload of arbitrary size, pagination, more roles |
| `events` | done | owner resolution + image upload + compensation; category management; STAFF description+category edit; CLIENT search + pagination |
| `qr` | not started | — |
| `assistance` | not started | — |

When adding a new module, follow §3 strictly: package layout, English names, interface+impl for every service, no Spanish identifiers, no new ad-hoc globals.
| `qr` | not started | — |
| `assistance` | not started | — |

When adding a new module, follow §3 strictly: package layout, English names, interface+impl for every service, no Spanish identifiers, no new ad-hoc globals.
