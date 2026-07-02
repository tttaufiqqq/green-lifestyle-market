# Tech Stack

| Layer | Choice | Why |
|---|---|---|
| Language/runtime | Java 21 (LTS) | Virtual threads available, records/pattern matching, long support window |
| Framework | Spring Boot 3.x | Project goal is learning Spring; batteries included (Security, Data, Validation, Scheduling, WebSocket) |
| Persistence | Spring Data JPA (Hibernate) | Standard repository pattern; pairs with explicit SQL via Flyway for schema control |
| Migrations | Flyway | Versioned, reviewable SQL migrations; schema is code |
| Database | Oracle Database 23ai Free | See adr/0002 — aligns with 1Z0-006 cert study; CHECK-constraint enums, native BOOLEAN/JSON, Flashback Data Archive for audit, Oracle Text for search; Free caps (12 GB / 2 GB RAM / 2 threads) are ample here |
| API style | REST JSON `/api/v1`, springdoc-openapi for Swagger UI | Predictable, testable, matches SPA client |
| Auth | Spring Security + spring-session-jdbc (cookie session), BCrypt | See adr/0005 — same-origin SPA makes sessions simpler and safer than JWT |
| Real-time | Spring WebSocket + STOMP | Free, native; replaces Pusher. In-app notification feed |
| Push | Web Push (VAPID) via webpush-java lib | Free browser notifications when tab closed |
| Frontend | React 18 + Vite | Existing skill; fast dev server |
| UI | Tailwind CSS + shadcn/ui | House default; consistent tokens |
| Client state | Zustand (global) + TanStack Query (server cache) | Small, no boilerplate |
| Payments | ToyyibPay dev sandbox (FPX) | Malaysian gateway, free sandbox; escrow model per adr/0004 |
| Scheduling | Spring `@Scheduled` | Reservation expiry, auto-complete, reconciliation jobs — no external queue needed at this scale |
| Testing | JUnit 5, Mockito, Testcontainers (Oracle Free), Vitest + RTL | Integration tests hit real Oracle (gvenzl/oracle-free) in a container |
| Build/CI | Maven, GitHub Actions | Test + build on PR |
| Serving | Nginx (SPA static + reverse proxy `/api`, `/ws`) | Same-origin deploy, TLS termination |
| Dev tunnel | Cloudflare Tunnel | Public callback URL for ToyyibPay in dev |

Explicitly not used in v1: Redis, message queues, microservices, Docker Swarm/K8s — right-sized for campus scale; revisit only with measured need.
