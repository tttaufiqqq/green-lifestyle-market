# Spec 03 — Auth & Accounts

## Goal
FR-A1..A6 working: register with email verification, session login/logout with lockout, password reset, profile + bank account management.

## Design
Per docs/auth.md: Spring Security filter chain, session-jdbc, CookieCsrfTokenRepository, BCrypt(12). Tokens hashed (SHA-256) in verification_tokens. Login throttle in-memory (Caffeine). AuditLogger records auth events.

## Implementation
1. SecurityConfig with route matrix from docs/auth.md; AuthController (register/login/logout/me/verify/forgot/reset); AccountController (/me/profile, /me/password, /me/bank-account).
2. UserService, TokenService (create/consume single-use), MailService (verify + reset templates).
3. Frontend: auth store (zustand), login/register/verify/reset pages per diagrams/pages/auth-pages.md; route guards; profile page.

## Dependencies
Spec 02.

## Pages
Login, Register, Verify-email landing, Forgot/Reset, Profile (+bank card).

## DB objects
Uses users, verification_tokens, seller_bank_accounts, audit_logs.

## API endpoints
All under "Auth & account" in docs/api-endpoints.md.

## Files Changed
backend com.glm.auth/**, com.glm.user/**, SecurityConfig; frontend features/auth/**, stores/auth.js

## Verify
- [ ] Register → email token → verify → can log in (FR-A1/A2)
- [ ] 5 wrong passwords → 429 lock 15 min (FR-A3)
- [ ] Reset token single-use, 1 h expiry (FR-A4)
- [ ] PUT bank account resets verified=false (FR-A5)
- [ ] Security matrix API tests pass (auth.md)
