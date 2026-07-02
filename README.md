# Green Lifestyle Market (GLM) — v2 Rebuild

A campus-and-community C2C marketplace for eco-friendly, pre-owned, and sustainable products. Rebuild of the DITU3934 workshop project on a modern stack.

## Stack
- **Backend:** Spring Boot 3.x (Java 21), Spring Data JPA, Spring Security (session), Spring WebSocket (STOMP), Flyway
- **Database:** Oracle Database 23ai Free (see `docs/adr/0002-oracle-database.md`)
- **Frontend:** React 18 + Vite + Tailwind CSS + shadcn/ui + Zustand
- **Payments:** ToyyibPay (dev sandbox: `https://dev.toyyibpay.com`), platform escrow model
- **Notifications:** In-app (WebSocket) + browser Web Push

## Core capabilities
Buy and sell new/used eco-friendly items, cart and multi-seller checkout, FPX payment via ToyyibPay, order lifecycle (meetup or shipping), refunds, admin seller payouts with reconciliation, educational articles, in-app help.

## Repository layout
See `docs/folder-structure.md`. Planning docs live in `context/` and `docs/`; build order lives in `context/specs/`.

## Status
Planning phase complete. Implementation not started. See `context/progress-tracker.md`.
