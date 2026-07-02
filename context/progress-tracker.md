# Progress Tracker

Status keys: TODO / IN-PROGRESS / DONE / BLOCKED

## Planning
| Item | Status |
|---|---|
| Context files | DONE |
| docs/ suite | DONE |
| ADRs 0001–0005 | DONE |
| Diagrams (use-cases, activity, pages) | DONE |
| Specs 01–10 | DONE |

## Implementation (build in spec order)
| Spec | Unit | Status | Notes |
|---|---|---|---|
| 01 | Project setup & CI | DONE | pom.xml, common/error, SecurityConfig, HealthController, Vite scaffold, CI, nginx |
| 02 | DB migrations (Flyway V1–V6) | TODO | |
| 03 | Auth & accounts | TODO | |
| 04 | Categories & listings | TODO | |
| 05 | Browse, search, product detail | TODO | |
| 06 | Cart | TODO | |
| 07 | Checkout, orders, ToyyibPay | TODO | |
| 08 | Order lifecycle & fulfilment | TODO | |
| 09 | Refunds, payouts, admin, reconciliation | TODO | |
| 10 | Notifications, articles, user guide | TODO | |

## Decisions log (pointers)
- DB: Oracle 23ai Free — adr/0002 (supersedes initial MariaDB pick). Frontend: React SPA — adr/0003. Settlement: escrow — adr/0004. Auth: session — adr/0005.
