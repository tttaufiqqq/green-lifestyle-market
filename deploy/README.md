# Deploy

- `nginx.conf` — SPA static files + reverse proxy `/api` and `/ws` to Spring Boot
- `oracle-provision.sql` — one-time admin grants for GLM_APP (CTXAPP, FLASHBACK ARCHIVE ADMINISTER)
- `cloudflared.yml` — Cloudflare Tunnel config for local dev (ToyyibPay callback URL)
- Systemd unit files for backend service

See docs/environment.md for all environment variables.
