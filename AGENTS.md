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
        └── users/             ← users module
```

Modules planned but **not yet implemented**: `events`, `qr`, `assistance`. Do not create stubs for them unless asked.

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

- All create endpoints accept `multipart/form-data` (not JSON) so the photo file can travel in the same request. Form DTOs (`UserCreateForm`, `UserStaffCreateForm`, `UserPhotoForm`) carry `@RestForm` fields (`org.jboss.resteasy.reactive.RestForm`) + `FileUpload photo`. Resource methods bind these forms with `@BeanParam` (not the deprecated `@MultipartForm`).
- `UserCreateForm` / `UserStaffCreateForm` also carry a `password` field (`@NotBlank @Size(min = 8, max = 32)`), forwarded to `UserCreationSupport` for both the DB (hashed) and Cognito (permanent) password — see §3.6.
- Update endpoints (`PUT /users/me`) accept JSON.
- Service methods receive `byte[] photo, String contentType` — never `FileUpload` in the service layer. The controller converts multipart to bytes.
- Validation: `@Valid` on the form DTO; constraints declared on each `@FormParam` field.
- Auth: `@RolesAllowed("OWNER")` etc. on role-gated endpoints. Public endpoints (e.g. `POST /users`) have no role annotation. Token validation is automatic via quarkus-oidc.
- Errors: throw module exception (`UserException` / `AuthException`) — mapped to JSON `{ "error": "..." }` by the module's `ExceptionMapper`. **Do not** return JAX-RS `Response` from service methods.

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
  exception/AuthExceptionMapper.java maps to { "error": "..." } JSON
  service/AuthService.java           interface — currentEmail() (extracts "email" claim from JsonWebToken)
  service/AuthLoginService.java      interface — login(LoginRequest) → LoginResponse
  service/impl/AuthServiceImpl.java
  service/impl/AuthLoginServiceImpl.java  Cognito InitiateAuth (USER_PASSWORD_AUTH) + SECRET_HASH
```

`AuthService.currentEmail()` is the **only** method other modules should call to identify the authenticated user (used by `UserProfileService` via `UserResource`).

`AuthLoginServiceImpl` computes `SECRET_HASH = Base64(HMAC-SHA256(username + clientId, clientSecret))` because the backend app client has a `client-secret` (`ALLOW_USER_PASSWORD_AUTH` flow). Required for any `InitiateAuth` against this client.

---

## 5. The `users` module

### 5.1 Entity layer

| File | Notes |
|---|---|
| `entity/UserEntity.java` | `@Entity @Table(name = "user")`. Extends `PanacheEntity` but **redeclares** `id` with `@Id @GeneratedValue(strategy = IDENTITY)` to use MySQL `AUTO_INCREMENT` (not Hibernate's sequence emul-table). |
| `entity/StaffEntity.java` | Composite PK via `@IdClass(StaffEntity.StaffId)`. Fields: `idStaff`, `userId`, `eventId`. `idStaff` is `@Id` only — no `@GeneratedValue` (JPA doesn't support it inside `@IdClass`); app assigns via `findMaxIdStaff()+1` in `UserStaffServiceImpl`. |
| `entity/StaffEntity.StaffId` | inner class, `Serializable` + `equals` + `hashCode` over all 3 fields. |

### 5.2 DTOs

| File | Purpose |
|---|---|
| `dto/UserCreateDTO.java` | Plain DTO used internally by services (`email`, `phoneNumber`, `fullname`, `birthdate`, `dni`). Built from the form in the controller. |
| `dto/UserCreateForm.java` | `multipart/form-data` form for `POST /users` (CLIENT). |
| `dto/UserStaffCreateDTO.java` | Extends `UserCreateDTO`, adds `eventId`. |
| `dto/UserStaffCreateForm.java` | multipart form for `POST /users/staff`. |
| `dto/UserUpdateDTO.java` | JSON body for `PUT /users/me` (no `email` — identity is immutable for the lifetime of the token). |
| `dto/UserPhotoForm.java` | multipart form with a single `FileUpload photo` for `POST /users/me/photo`. |
| `dto/UserResponseDTO.java` | record returned by every user endpoint. |

### 5.3 Repository layer

- `repository/UserRepository.java` — `PanacheRepository<UserEntity>`. Methods: `findByEmail`, `findByDni`, `findByPhoneNumber`, `existsByEmail/Dni/PhoneNumber`. **Always parameterized** (positional `?1`); never concatenate user input.
- `repository/StaffRepository.java` — `PanacheRepository<StaffEntity>`. Methods: `findByUserAndEvent`, `deleteByUserAndEvent`, `deleteByUser`, `findMaxIdStaff`.

### 5.4 Service layer

- `service/CognitoUserService.java` + `impl/CognitoUserServiceImpl.java` — wrapper around `CognitoIdentityProviderClient` (`adminCreateUser`, `adminDeleteUser`). Translates `CognitoIdentityProviderException` → `UserException(502)`.
- `service/UserPhotoStorageService.java` + `impl/UserPhotoStorageServiceImpl.java` — S3 wrapper. Bucket name via `s3.bucket-name`, region via `quarkus.s3.aws.region`. Key prefix: `profile-pictures/{userId}/{uuid}.{ext}`. Public URL pattern: `https://{bucket}.s3.{region}.amazonaws.com/{key}`. jpg/jpeg/png only (`image/jpeg` → `.jpg`, `image/png` → `.png`).
- `service/UserCreationSupport.java` — shared `@Transactional` flow described in §3.6. Injected by the three create services.
- `service/UserClientService.java` / `UserStaffService.java` / `UserOwnerService.java` / `UserAdminService.java` / `UserProfileService.java` — role-specific services described in §3.4. All have `Impl` in `service/impl/`.

### 5.5 Resource layer

Split by role instead of one unified controller (see §3.4; a single `UserResource` mixing all 5 services was a code-review violation, now fixed):

| File | Path prefix | Endpoints |
|---|---|---|
| `controller/UserClientResource.java` | `/users` | `POST /users` (register CLIENT, public); `GET/PUT /users/me`, `POST/DELETE /users/me/photo` (self-service, any role) |
| `controller/UserStaffResource.java` | `/users` | `POST /users/staff` (OWNER); `DELETE /users/{userId}/staff/{eventId}` (OWNER, ADMIN) |
| `controller/UserOwnerResource.java` | `/users` | `POST /users/owner` (ADMIN) |
| `controller/UserAdminResource.java` | `/users` | `DELETE /users/{id}` (ADMIN) |

The shared self-service `/me` endpoints (any authenticated role, backed by `UserProfileService`) live in `UserClientResource` since `/users` is their natural base path — there is no 5th controller for them. Map form DTOs to internal DTOs in the controller (via the shared `utils/UserFormMapper` helper); never let a `FileUpload` reach the service layer.

### 5.6 Exception layer

`exception/UserException.java` (status + message) + `exception/UserExceptionMapper.java` (→ JSON `{ "error": "..." }`). Status codes used:

| Code | Meaning |
|---|---|
| 400 | malformed request / empty photo bytes |
| 404 | user not found by email/id; staff affiliation not found |
| 409 | email / dni / phone already in use; staff already affiliated |
| 415 | photo content-type not in {image/jpeg, image/png} |
| 502 | AWS SDK failure (Cognito or S3) — wraps the SDK message |

---

## 6. Endpoints (current state)

| Method | Path | Auth | Body | Service method |
|---|---|---|---|---|
| POST | `/auth/login` | public | JSON `{ email, password }` | `AuthLoginService.login` |
| POST | `/users` | public | multipart (form + optional photo) | `UserClientService.create` |
| POST | `/users/staff` | `OWNER` | multipart (+ `eventId`) | `UserStaffService.create` |
| POST | `/users/owner` | `ADMIN` | multipart | `UserOwnerService.create` |
| GET | `/users/me` | any | — | `UserProfileService.getMe` |
| PUT | `/users/me` | any | JSON | `UserProfileService.updateMe` |
| POST | `/users/me/photo` | any | multipart (single file) | `UserProfileService.uploadPhoto` |
| DELETE | `/users/me/photo` | any | — | `UserProfileService.deletePhoto` |
| DELETE | `/users/{id}` | `ADMIN` | — | `UserAdminService.deleteAccount` |
| DELETE | `/users/{userId}/staff/{eventId}` | `OWNER` or `ADMIN` | — | `UserStaffService.desaffiliate` |

---

## 7. Configuration (`src/main/resources/application.properties`)

Read these before touching anything that talks to AWS or Cognito.

### Cognito (always active, used by the SDK in dev AND prod)

```
cognito.user-pool-id=us-east-1_Y3AVPF7cE
cognito.client-id=6m496adbmi95ln3i1pta9tnj9s
cognito.client-secret=126hu4uafeurrdld6c1ofnu0tpjeiprmuasofdnvf6vd9ae72bsu
cognito.region=us-east-1
```

- `user-pool-id` → every Admin API call.
- `client-id` / `client-secret` → `SECRET_HASH` for `InitiateAuth` (the `auth/login` endpoint). Required because the client has a secret and the pool allows `USER_PASSWORD_AUTH`.
- `region` → builds the `CognitoIdentityProviderClient`.

### OIDC (token validation, currently active in all profiles)

```
quarkus.oidc.auth-server-url=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_Y3AVPF7cE
quarkus.oidc.client-id=6m496adbmi95ln3i1pta9tnj9s
quarkus.oidc.application-type=service
quarkus.oidc.token.issuer=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_Y3AVPF7cE
quarkus.oidc.roles.role-claim-path=cognito:groups
```

> ⚠️ `CORRECTION.MD` recommended scoping these under `%dev.` so the Lambda build doesn't double-validate (API Gateway's `COGNITO_USER_POOLS` authorizer handles it). This refactor was not applied — keep all five lines un-prefixed unless explicitly told to revert to `%dev.`.

> ⚠️ The access token issued by Cognito does **not** carry `cognito:groups`. Only the **ID token** does. For local testing the client must send the **ID token** in `Authorization: Bearer`, or every `@RolesAllowed` will fail silently (no groups, looks like a permissions bug, not an auth error).

### AWS credentials (currently STS temporary)

```
quarkus.s3.aws.region=us-east-1
quarkus.s3.aws.credentials.type=static
quarkus.s3.aws.credentials.static-provider.access-key-id=...
quarkus.s3.aws.credentials.static-provider.secret-access-key=...
quarkus.s3.aws.credentials.static-provider.session-token=...
aws.credentials.access-key-id=...
aws.credentials.secret-access-key=...
aws.credentials.session-token=...
s3.bucket-name=teleticket-lite-images
```

- The two blocks must be kept **in sync** — the quarkiverse S3 extension reads `quarkus.s3.aws.credentials.*`, while the raw SDK `CognitoIdentityProviderClient` (produced in `UserConfig`) reads `aws.credentials.*`.
- The current keys are **STS temporary** (`ASIA...` prefix). They will expire (currently `2026-07-07T14:56:14Z`). When they do, the app will fail with `400: The security token included in the request is invalid`. Ask the user to refresh them via `aws configure export-credentials` in CloudShell and paste the JSON; never guess the values.
- `UserConfig.java` builds `CognitoIdentityProviderClient` with `AwsSessionCredentials` (3-part: access + secret + session token). If the user later switches to long-lived IAM keys (`AKIA...` prefix, no session token), the implementation must change to `AwsBasicCredentials`.

### DB

```
quarkus.datasource.db-kind=mysql
quarkus.datasource.username=root
quarkus.datasource.password=password
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/teleticketlite
quarkus.hibernate-orm.log.sql=false
quarkus.hibernate-orm.database.generation=update
```

- `database.generation=update` — Hibernate adds missing tables/columns on startup. With `IDENTITY` ids in place it does **not** create the `user_SEQ` emul-table. Keep it for dev; replace with Flyway/Liquibase for prod.

---

## 8. Build & Verify (skill-mandated)

Before any change, run **`./mvnw.cmd clean compile`** (Windows) or **`./mvnw clean compile`** (Unix). After changes, run **`./mvnw.cmd clean verify`**. Both must end with `BUILD SUCCESS`.

There are no tests yet. `mvn verify` runs Quarkus augmentation (build-time CDI / OIDC / Hibernate validation) — a failure there is meaningful, not a no-op.

`mvn clean` will fail if a dev server holds `target/teleticketlite-dev.jar`. Stop the dev server first.

---

## 9. Known gotchas (do not trip on these again)

- **`@MultipartForm` was deprecated** in Quarkus 3.37.1 (marked for removal) and has been replaced project-wide. Form beans (`UserCreateForm`, `UserStaffCreateForm`, `UserPhotoForm`) use `@RestForm` (`org.jboss.resteasy.reactive.RestForm`) on each field, and resource methods bind them with `@BeanParam` instead of `@MultipartForm`. Do not reintroduce `@FormParam`/`@MultipartForm`.
- **Passwords**: DB stores a bcrypt hash (`io.quarkus.elytron.security.common.BcryptUtil.bcryptHash(...)`, computed in `UserMapper.toEntity`), never the plaintext. Cognito gets the same plaintext password set as **permanent** via `AdminSetUserPassword` inside `CognitoUserServiceImpl.adminCreateUser` (called right after `AdminAddUserToGroup`, still inside the same try/catch so a failure triggers the usual Cognito cleanup path). Requires the `quarkus-elytron-security-common` dependency (added to `pom.xml`) — do not add a second bcrypt library.
- **`UserException` vs `AuthException`** — they have identical shape but are separate classes per module. Do not unify; do not share a mapper.
- **`dni` field** is a domain term (Peruvian national ID), not a localization problem. Keep it.
- **Self-invocation of `@Transactional`** is forbidden. `UserCreationSupport` is the transactional unit; the three create services call into it (not `@Transactional` themselves for the create method). Don't add `@Transactional` to `UserClientService.create` etc.
- **Photo upload order**: `persist` + `flush` first (to get the generated `id`), then upload to S3, then set `photoUrl`/`photoKeyName` and `flush` again. S3 key includes the user id.
- **Staff `id_staff` is app-assigned** (`findMaxIdStaff() + 1`). Race-prone under concurrent staff creation, but JPA can't put `@GeneratedValue` inside `@IdClass`. Document if touched.
- **OWNER registration does NOT create an `event_owner` row.** The spec (PLAN §6) does not ask for it and provides no `ruc`. The events module will own that concern later.
- **Hardcoded STS creds in `application.properties`** are temporary. Never commit new ones without telling the user they expire. Never log them.

---

## 10. Postman collection

`plan/postman/teleticket-lite.postman_collection.json` (v2.1.0). Import in Postman. Variables: `{{baseUrl}}` (default `http://localhost:8080`) and `{{idToken}}` (auto-set by the login request's test script from `response.idToken`). Photo file fields have empty `src` — pick a file in the Postman UI before sending.

The ID token is what must be sent as `Authorization: Bearer {{idToken}}` for `@RolesAllowed` to find the user's groups (see §7 warning).

---

## 11. Module map for future work

| Module | Status | Touch these |
|---|---|---|
| `auth` | done (login + identity) | add refresh-token flow when needed |
| `users` | done | photo upload of arbitrary size, pagination, more roles |
| `events` | not started | create `events/` with `controller`, `entity/EventEntity`, `entity/EventCategoryEntity`, `entity/EventImageEntity`, services; do **not** touch `users/` unless you must |
| `qr` | not started | — |
| `assistance` | not started | — |

When adding a new module, follow §3 strictly: package layout, English names, interface+impl for every service, no Spanish identifiers, no new ad-hoc globals.
