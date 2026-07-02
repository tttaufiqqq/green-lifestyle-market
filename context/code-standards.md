# Code Standards

## Java / Spring Boot
- Java 21, Spring Boot 3.x. Package root: `com.glm`.
- Package-by-feature: `com.glm.auth`, `com.glm.catalog`, `com.glm.cart`, `com.glm.order`, `com.glm.payment`, `com.glm.payout`, `com.glm.refund`, `com.glm.notification`, `com.glm.article`, `com.glm.admin`, `com.glm.common`.
- **SRP strictly. HARD RULE: every coding file must be under 200 lines** (Java, JS/JSX, SQL, config — everything that is code). Checkstyle (backend) and ESLint `max-lines` (frontend) enforce 200 in CI.
- **When a file would exceed 200 lines, use the orchestration pattern** — never "just one big file":
  - Keep/extract a thin **orchestrator** whose only job is sequencing: it calls collaborators in order, passes results along, owns the `@Transactional` boundary, and contains no business rules itself.
  - Move each responsibility into a small, named collaborator class/module (validator, calculator, mapper, gateway client, state guard, persister).
  - Example (backend): `CheckoutOrchestrator` → `CheckoutValidator` → `FeeCalculator` → `OrderFactory` → `ReservationWriter` → `ToyyibPayClient`. Each collaborator is independently unit-testable and well under 200 lines.
  - Example (frontend): `CheckoutPage` renders layout only → `useCheckout()` hook owns state/effects → `AddressForm`, `SellerGroupCard`, `TotalsSummary` are dumb components → `checkout.api.js` owns the HTTP calls.
  - Splitting by responsibility, not by line count: never create `ServiceHelper2.java` or `utils-more.js` files just to duck the limit.
- Controllers are thin orchestrators: validate → delegate to service → map to DTO. No business logic, no repository access.
- Business logic in `@Service` classes; one `@Transactional` boundary per use case, at the service method.
- DTOs via Java records; MapStruct or manual mappers; never expose JPA entities from controllers.
- Validation with `jakarta.validation` annotations on request records; custom rules in services.
- No field injection — constructor injection only.
- Money: `BigDecimal` with scale 2 in Java, `DECIMAL(10,2)` in DB, **sen (integer) only at the ToyyibPay boundary**.
- Time: `Instant` in UTC everywhere; convert to `Asia/Kuala_Lumpur` only in the frontend.
- Errors: throw domain exceptions mapped by `GlobalExceptionHandler` to the envelope in docs/error-catalogue.md. Fail loud — no silent catches.
- Migrations: Flyway, `V{n}__description.sql`, never edit an applied migration.

## Security baseline (OWASP Top 10)
- All inputs validated and sanitized; parameterized queries only (JPA/JPQL — never string-concatenated SQL).
- Auth check on every protected route via Spring Security config, not per-controller ad hoc checks.
- No hardcoded secrets — everything via environment (docs/environment.md).
- Ownership checks in services (a seller can only mutate their own listings/orders).
- File uploads: whitelist MIME (jpeg/png/webp), max 2 MB, re-encode server-side, store outside webroot, serve via Nginx alias.

## React
- Vite + React 18, JavaScript (TS optional later). Feature folders under `src/features/`.
- State: Zustand for global (auth, cart badge, notifications); React Query for server state.
- UI: Tailwind + shadcn/ui; components under 200 lines; when a component grows, orchestrate: page component composes feature components, logic lives in custom hooks, HTTP lives in the feature api module.
- API layer in `src/lib/api.js` (fetch wrapper with CSRF header + error envelope handling). Components never call fetch directly.

## Git
- Branches: `feat/`, `fix/`, `docs/` prefixes. Conventional, small commits.
- Commit format per CLAUDE.md rule 13.
