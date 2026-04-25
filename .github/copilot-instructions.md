# Copilot Instructions

## Project Overview

OtsukaiList is a no-auth shared shopping list app. Lists are accessed via UUID-based URLs — the UUID _is_ the access control. The backend exposes a REST API (Spring Boot) and the frontend is a Vue 3 SPA.

## Commands

### Backend

```bash
./gradlew bootRun                          # Start dev server (port 8080)
./gradlew test                             # Run all tests (requires test DB running)
./gradlew test --tests 'ClassName'         # Run a single test class
./gradlew test --tests 'ClassName.method'  # Run a single test method
./gradlew startTestDB                      # Start test DB on port 5433 (required before tests)
./gradlew stopTestDB                       # Stop test DB
./gradlew staticAnalysis                   # Checkstyle + PMD + SpotBugs
./gradlew spotlessApply                    # Auto-format with Google Java Format
./gradlew fixCodeStyle                     # Fix tabs, trailing whitespace, newlines
```

### Frontend

```bash
npm run dev                # Vite dev server (port 5173)
npm run build              # Type-check + build
npm run test               # Vitest watch mode
npm run test:run           # Vitest single run (CI)
npm run lint               # ESLint check
npm run lint:fix            # ESLint auto-fix
npm run format             # Prettier format
```

### Database

```bash
./env.sh dev up            # Start dev DB (port 5432, persistent volume)
./env.sh dev down          # Stop dev DB
./env.sh [dev|test|ci] [up|down|logs|status]
```

## Architecture

### Backend (`backend/src/main/java/com/atoook/otsukailist/`)

CQRS-inspired split: separate controllers and services for reads and writes.

```
api/controller/   # REST controllers — thin, delegate to services
api/advice/       # @RestControllerAdvice global exception handler
service/          # Business logic; *CommandService (writes), *QueryService (reads)
model/            # JPA @Entity: ItemList, Item, Member
repository/       # JpaRepository interfaces
dto/              # Request/Response DTOs (14 total)
mapper/           # @UtilityClass static mappers (Entity ↔ DTO)
exception/        # BadRequestException (400), ResourceNotFoundException (404)
```

Core data flow: Controller → Service → Repository → Mapper → DTO response.

All mutations return `MutationResponse<T>` which wraps the response data with a `revision` number (used for future real-time sync).

### Frontend (`frontend/src/`)

```
api/          # One file per resource: item.ts, list.ts, member.ts
lib/          # http.ts: Axios instance + error interceptor → ApiError
stores/       # Pinia store: list.ts (Options API style)
pages/        # Route-level components
components/   # Reusable UI components
composables/  # useMutation.ts: loading/error state for API calls
types/        # TypeScript types (api.ts, item.ts, member.ts, item-list.ts)
```

Initial load uses a single snapshot endpoint (`GET /api/lists/{id}/snapshot`) that returns the full list state. All subsequent mutations update the Pinia store directly from the `MutationResponse`.

### Database

No migration tool (no Flyway/Liquibase). Schema lives in `/db/init/01_create_tables.sql`. To reset: `./env.sh dev down && ./env.sh dev up`. Test DB uses `create-drop` JPA strategy; dev/prod uses `validate`.

## Implementation Quality Policy

When proposing or applying fixes:

1. Do not use "minimal number of changed lines" as the primary decision criterion.
2. Prioritize best-practice, correctness-first implementations over quick warning-suppression patches.
3. For static-analysis findings, verify the underlying design risk is addressed, not only the tool warning output.
4. If multiple fixes are possible, choose the one that is maintainable, type-safe, and robust under concurrent/real-world usage.
5. In reviews/explanations, explicitly state why the chosen fix is best-practice compliant.

### Backend

**Adding an endpoint:**

1. Add DTO(s) in `dto/` — use Lombok `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`; add `@NotBlank`/`@Size` validation on request DTOs
2. Add method to `*CommandService` or `*QueryService` with `@Transactional` on writes
3. Add controller method returning `ResponseEntity<MutationResponse<T>>`; use `@Valid @RequestBody`
4. Add mapper method in `mapper/` if a new entity↔DTO conversion is needed

**Mappers** are `@UtilityClass` classes with static methods only. They handle trimming/normalizing values. Services handle business rules.

**Exceptions:** throw `ResourceNotFoundException` (→ 404) or `BadRequestException` (→ 400). The `ApiExceptionHandler` also maps `DataIntegrityViolationException` → 409.

**Controller annotations:** `@RestController`, `@RequiredArgsConstructor`, `@RequestMapping("/api/...")`. Use constructor injection for services (via `final` fields + `@RequiredArgsConstructor`).

**DTO naming:** `Create[Entity]Request`, `Update[Entity]Request`, `[Entity]Response`.

### Frontend

**Vue components** use the **Options API** (`data()`, `computed`, `watch`, `methods`) — not `<script setup>` or Composition API.

**Pinia store** uses the **Options API style** (`defineStore('id', { state, getters, actions })`). Actions use `this`.

**API functions** (`src/api/*.ts`): one exported async function per endpoint, typed with generics (`http.get<T>(url)`), returning `res.data`.

**Error handling** is centralized in `lib/http.ts` — the Axios interceptor converts all errors to `ApiError`. Don't add per-call try/catch for HTTP errors; use `useMutation()` composable for mutations.

**TypeScript types:** `UUID` is `type UUID = string`. All IDs are `UUID`. Use `MutationResponse<T>` as the return type for any write operation. Use discriminated unions for variant types.

**Tailwind classes** are merged via `twMerge` from `tailwind-merge` in components that conditionally combine classes.

## Environment

**Backend** reads from `.env` via `spring-dotenv`. Copy `backend/.env.example` → `backend/.env`.  
Key vars: `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `APP_CORS_ALLOWED_ORIGINS`.

**Frontend** uses Vite env vars:

- `VITE_API_BASE_URL` — backend base URL (defaults to `/api`)
- `VITE_BASE_URL` — frontend origin for share links (defaults to `window.location.origin`)

Test profile (`@ActiveProfiles("test")`) uses `application-test.properties` which points to port 5433.

## Issue Automation Workflow

When creating a parent issue + multiple child issues from chat:

1. Prefer MCP GitHub issue tools when available; avoid interactive shell issue creation when MCP can perform the same action.
2. Default to a single issue when work can be completed as one coherent task with one acceptance scope.
3. Use orchestration only when work must be split into independent/sequential child tasks.
4. Use issue templates in `.github/ISSUE_TEMPLATE/`:
   - single issue: `single-issue.md`
   - parent-child orchestration: `orchestration-parent.md`, `orchestration-child-task.md`
5. Use MCP issue operations for both single issues and orchestration updates (parent + child link updates).
6. If running `gh` commands manually, avoid long heredoc chains in the shared terminal session.
7. If the shell prompt shows `heredoc>`, recover by closing with `EOF` or interrupting (`Ctrl+C`) before continuing.
8. Run issue creation sequentially when terminal state is unstable; avoid parallel `gh issue edit` commands in one shared session.

Use MCP issue tooling and the templates above as the canonical process.
