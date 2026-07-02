# Testing Notes

## Pyramid
1. **Unit (JUnit 5 + Mockito):** services in isolation — fee rounding, state machine legality table (every status × event), availability math, ownership checks.
2. **Integration (Testcontainers Oracle Free):** repositories + transactional flows against real Oracle — Flyway applies cleanly; checkout locking; webhook idempotency (insert duplicate key); expiry↔callback race (two threads, one payment); invariant assertions from database.md.
3. **API (MockMvc/WebTestClient):** security matrix from auth.md (each route × role), error envelope shape, pagination contract.
4. **Frontend (Vitest + RTL):** cart store math, checkout form validation, payment result polling states.
5. **E2E (manual script in dev sandbox, later Playwright):** the golden path — register→verify→list→cart→checkout→pay (sandbox FPX)→callback→confirm→ship→receipt→payout; and the abandon path (expiry).

## Money-critical test list (must exist before spec 07 merges)
- Duplicate callback → single side effect (FR-P1).
- Callback with tampered amount → payment flagged, orders remain PENDING (FR-P2).
- Expiry job vs late success callback race → deterministic outcome (UC-08 R08-E1 / UC-07 R07-E2).
- Property test: random carts hold Σ order.total = payment.amount and seller_net formula (financial-rules.md).
- payout_items UNIQUE prevents double payout under concurrent admin clicks (R12-E1).

## Conventions
- Tests assert requirement IDs in display names: `@DisplayName("FR-P1 duplicate callback is idempotent")` — this doubles as the RTM hook.
- Testcontainers image pinned `gvenzl/oracle-free:23-slim`; enable container reuse locally (slow cold start); no H2 anywhere (dialect drift hides bugs).
- Sandbox secrets via env in CI (GitHub Actions secrets); callback tests hit a local stub of ToyyibPay (WireMock), real sandbox only in the manual E2E.
