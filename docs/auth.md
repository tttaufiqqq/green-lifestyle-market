# Auth & Access Control

## Mechanism (adr/0005)
Spring Security with session cookies persisted via spring-session-jdbc. Same-origin deployment (Nginx) → no CORS in prod; dev uses Vite proxy. Cookie: HttpOnly, Secure, SameSite=Lax, 7-day timeout. CSRF: CookieCsrfTokenRepository; SPA echoes `XSRF-TOKEN` cookie into `X-CSRF-TOKEN` header on mutations. Passwords: BCrypt (strength 12). Login throttle: 5 failures / 15 min per email (FR-A3), backed by a small in-memory cache.

## Roles & route matrix
| Route group | Guest | User | Admin |
|---|---|---|---|
| GET catalog, articles, /help | ✔ | ✔ | ✔ |
| /auth/** | ✔ | logout/me only | same |
| /me/** (profile, cart, orders, sales, listings, notifications, payouts) | ✖ | ✔ (ownership enforced in services) | ✔ |
| /checkout/**, /payments/{no} | ✖ | ✔ verified email | ✔ |
| /payments/toyyibpay/callback,/return | open (rate-limited, CSRF-exempt) | | |
| /admin/** | ✖ | ✖ | ✔ |
| /ws | ✖ | ✔ (handshake requires session) | ✔ |

## Middleware/filter chain order
1. Session resolution → 2. CSRF filter (exempt: callback/return) → 3. Authorization rules above → 4. Controller. Ownership is NOT a filter concern: services load the entity and compare owner id to principal id, throwing 403 E-AUTH-OWN otherwise.

## Verification gates
- Email unverified → cannot create listings (R03-E2) or checkout (FR-A2 gate at /checkout).
- Bank account unverified → payout creation blocked (R12-A1); selling itself is allowed.

## Audit
LOGIN, LOGOUT, LOGIN_LOCKED, PASSWORD_RESET, plus all admin and money mutations → audit_logs with user id, ip, entity refs.
