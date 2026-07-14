# Environment Configuration

All secrets via environment variables (never committed). Spring profile `dev` for sandbox, `prod` for live.

## Backend (`backend/.env` → injected into Spring)
| Variable | Example (dev) | Notes |
|---|---|---|
| `DB_URL` | `jdbc:oracle:thin:@//100.118.110.114:1521/FREEPDB1` | Oracle 23ai Free on Proxmox db VM — `FREEPDB1` is the pluggable DB `GLM_APP` actually lives in; `FREE` is the CDB root and can't see it |
| `DB_USER` / `DB_PASSWORD` | `glm_app_dev` (dev) / `glm_app` (prod) / secret | Least-privilege app user, separate schema per environment — see `docs/01-oracle/glm-db-access.md` in the homelab repo |
| `APP_BASE_URL` | `https://glm.tttaufiqqq.com` | Used to build ToyyibPay return/callback URLs |
| `TOYYIBPAY_BASE_URL` | `https://dev.toyyibpay.com` | Prod: `https://toyyibpay.com` |
| `TOYYIBPAY_SECRET_KEY` | sandbox userSecretKey | From dev.toyyibpay.com account |
| `TOYYIBPAY_CATEGORY_CODE` | sandbox category code | Create category once in dashboard |
| `PLATFORM_FEE_PERCENT` | `5` | Commission on item subtotal |
| `PAYMENT_EXPIRY_MINUTES` | `30` | Reservation + order expiry window |
| `AUTO_COMPLETE_DAYS` | `7` | Auto-complete after shipped/meetup |
| `VAPID_PUBLIC_KEY` / `VAPID_PRIVATE_KEY` | generated | Web Push |
| `UPLOAD_DIR` | `/var/glm/uploads` | Outside webroot; Nginx alias `/media/` |
| `SESSION_TIMEOUT_MINUTES` | `10080` | 7 days |

## Frontend (`frontend/.env`)
| Variable | Example |
|---|---|
| `VITE_API_BASE` | `/api/v1` (same-origin) |
| `VITE_VAPID_PUBLIC_KEY` | same as backend public key |

## Dev-only
`cloudflared` tunnel maps public hostname → localhost:8080 so ToyyibPay callbacks reach dev. Never expose actuator endpoints publicly (`management.endpoints.web.exposure.include=health`).
