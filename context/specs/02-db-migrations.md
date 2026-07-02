# Spec 02 — Database Migrations (Flyway V1–V6, Oracle)

## Goal
Full schema from docs/database.md applied by Flyway on Oracle 23ai Free, verified by Testcontainers, with seed data for categories and an admin user.

## Design
Six migrations exactly as the plan table in docs/database.md, written in Oracle DDL: identity PKs, VARCHAR2(n CHAR), CLOB, native BOOLEAN and JSON, named CHECK constraints replacing enums, Oracle Text CONTEXT indexes (V2), Flashback Data Archive setup (V6). Seeds in V2 (categories); dev-only R__dev_seed.sql (admin user + sample products) guarded by profile. Provisioning script (run once as admin, outside Flyway) grants GLM_APP the CTXAPP role and FLASHBACK ARCHIVE ADMINISTER.

## Implementation
1. Write V1__users_auth.sql … V6__content_notify_audit.sql translating the table catalogue verbatim (types, FKs, indexes, UNIQUEs, named CHECKs, CONTEXT indexes, GLM_FDA).
2. JPA entities per feature package mapping the tables; status enums as `@Enumerated(STRING)` into VARCHAR2; BigDecimal money; Instant timestamps; Lob for CLOB fields. Entities only — no services yet.
3. Repository interfaces per aggregate root; one native `CONTAINS()` search query stub in ProductRepository.
4. Integration test: Flyway migrate on gvenzl/oracle-free:23-slim container (reuse enabled), assert seeded category count, assert UNIQUE violations raise (webhook idempotency_key, payout_items.order_id), assert products CHECK (price >= 1) rejects RM0.50.

## Dependencies
Spec 01.

## Pages
None.

## DB objects
All 18 tables + CONTEXT indexes + GLM_FDA + seeds (docs/database.md).

## API endpoints
None.

## Files Changed
backend/src/main/resources/db/migration/V1..V6, deploy/oracle-provision.sql, backend/src/main/java/com/glm/**/entity, **/repository, backend/src/test/java/.../MigrationIT.java

## Verify
- [ ] flyway validate + migrate clean on fresh Oracle container
- [ ] Duplicate webhook idempotency_key insert fails (test)
- [ ] Duplicate payout_items.order_id insert fails (test)
- [ ] `SELECT * FROM orders AS OF TIMESTAMP (SYSTIMESTAMP - INTERVAL '1' MINUTE)` works after an update (FDA active)
- [ ] `CONTAINS()` search returns a seeded product by a description word
