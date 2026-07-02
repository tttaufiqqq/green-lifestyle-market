# Architecture — Green Lifestyle Market

## Style
Modular monolith: one Spring Boot application exposing a versioned REST API (`/api/v1`), one React SPA, one Oracle database. Server-authoritative state; SPA is a thin client.

## Components
```
+------------------+        HTTPS         +---------------------------+
|  React SPA       | <------------------> |  Spring Boot API          |
|  Vite build,     |   JSON /api/v1       |  Controller -> Service -> |
|  served by Nginx |   WS /ws (STOMP)     |  Repository (JPA)         |
+------------------+                      +------+---------+----------+
                                                 |         |
                                        JDBC     |         | HTTPS
                                                 v         v
                                          +-----------+  +--------------------+
                                          | Oracle    |  | ToyyibPay (dev)    |
                                          | 23ai Free |  | createBill,        |
                                          +-----------+  | getBillTransactions|
                                                         +--------------------+
```

## Layering (strict, one direction)
`Controller` (HTTP concerns only, thin orchestrator) → `Service` (business logic, transactions) → `Repository` (Spring Data JPA) → Oracle.
Cross-cutting: `SecurityConfig`, `GlobalExceptionHandler`, `NotificationPublisher`, `AuditLogger`.

## Key patterns
- **Order state machine** enforced in `OrderService` — a single `transition(order, event)` method guards all state changes (docs/domain-rules.md).
- **Idempotent webhook consumer**: ToyyibPay callbacks recorded in `webhook_events` with a unique idempotency key; INSERT-then-process inside one transaction.
- **Stock reservation**: pessimistic `SELECT ... FOR UPDATE` on product rows during checkout; reservation rows with TTL; scheduled job releases expired reservations.
- **Outbox-lite notifications**: notification row written in the same transaction as the domain change; WebSocket/Web Push delivery after commit.
- **Snapshotting**: order_items copy title/price at purchase time; later product edits never mutate history.

## NFR → component mapping (with tradeoffs)
| NFR | Component/decision | Tradeoff |
|---|---|---|
| Payment integrity | webhook_events idempotency + server-side `getBillTransactions` verification + DB transactions | Extra API call per callback; accepted for correctness |
| No oversell | Pessimistic row locks + reservation TTL | Slightly lower checkout throughput; fine at campus scale |
| Browse p95 < 500 ms | Indexed queries, pagination everywhere, images served by Nginx | No cache layer in v1; add Redis later if needed |
| Availability | Single-node deploy | No HA in v1; documented restore procedure instead |
| Security | Spring Security session + CSRF, BCrypt, parameterized JPA, OWASP Top 10 baseline | spring-session-jdbc adds +1 write per request but survives restarts |
| Auditability | audit_logs + Oracle Flashback Data Archive (GLM_FDA) on orders/payments/payouts | Storage growth; retention policy in financial-rules.md |

## Deployment (v1)
Nginx serves the SPA build and reverse-proxies `/api` and `/ws` to Spring Boot (same origin → cookie auth, no CORS in prod). Oracle 23ai Free container on the Proxmox db VM (>=4 GB RAM) via Tailscale, or same host. ToyyibPay callback URL must be publicly reachable (Cloudflare Tunnel in dev).
