# Spec 01 — Project Setup & CI

## Goal
Runnable skeleton: Spring Boot app boots against Oracle 23ai Free, React SPA dev server proxies to it, CI runs tests on PR.

## Design
Maven multi-less layout (backend/, frontend/ per docs/folder-structure.md). Spring profiles dev/prod. Global error envelope + GlobalExceptionHandler from day one. springdoc-openapi at /swagger (dev only).

## Implementation
1. Spring Initializr: web, data-jpa, security, validation, flyway, oracle (ojdbc11 + flyway-database-oracle), session-jdbc, websocket, mail, actuator (health only).
2. application.yml with env-var placeholders per docs/environment.md; application-dev.yml enables swagger + relaxed logging.
3. common/: ApiError envelope, ErrorCode enum (seed from docs/error-catalogue.md), GlobalExceptionHandler, AuditLogger stub.
4. Vite React app: Tailwind + shadcn init, lib/api.js fetch wrapper (CSRF echo, envelope parsing), router shell with navbar per ui-context.md tokens.
5. GitHub Actions: mvn verify (Testcontainers) + npm test + npm run build.
6. deploy/nginx.conf draft (SPA + /api + /ws proxy).

## Dependencies
None (first spec).

## Pages
App shell only (navbar, footer, empty home).

## DB objects
None yet (Flyway configured, empty).

## API endpoints
GET /api/v1/health (wraps actuator) — proves envelope + wiring.

## Files Changed
backend/pom.xml, backend/src/main/java/com/glm/{GlmApplication,common/**}, backend/src/main/resources/application*.yml, frontend/** (scaffold), .github/workflows/ci.yml, deploy/nginx.conf

## Verify
- [ ] `mvn spring-boot:run` boots with local Oracle (gvenzl/oracle-free container) env vars
- [ ] `npm run dev` proxies /api to backend; /health renders in SPA
- [ ] Throwing a test exception returns the error envelope
- [ ] CI green on PR
