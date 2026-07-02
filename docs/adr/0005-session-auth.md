# ADR-0005: Cookie Sessions over JWT

## Context
SPA and API are deployed same-origin behind Nginx. Auth options: stateless JWT (localStorage or cookie) vs server sessions.

## Decision
Spring Security sessions persisted with spring-session-jdbc; HttpOnly Secure SameSite=Lax cookie; CSRF via CookieCsrfTokenRepository; BCrypt(12).

## Consequences
+ Immediate revocation (logout, suspension, password change kills sessions); no token-in-JS exposure (XSS-resistant); simplest correct choice for same-origin; sessions survive restarts via DB.
+ Matches the house preference of sessions for monorepo-style apps.
− Each request touches the session table (minor at this scale); mobile/native clients would need a token scheme later — add a separate token endpoint then, don't pre-build it.
