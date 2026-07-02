# Notifications

## Triggers → recipients
| Event (order state machine / jobs) | Notify | Type key |
|---|---|---|
| Payment SUCCESS verified | buyer | PAYMENT_SUCCESS |
| Payment FAILED / EXPIRED | buyer | PAYMENT_FAILED |
| Order PAID | seller | ORDER_NEW |
| Order CONFIRMED | buyer | ORDER_CONFIRMED |
| SHIPPED / READY_FOR_MEETUP | buyer | ORDER_SHIPPED / ORDER_MEETUP |
| CANCELLED (either party) | other party + admin | ORDER_CANCELLED |
| Refund REQUESTED | admin + seller | REFUND_REQUESTED |
| Refund APPROVED/REJECTED/PROCESSED | buyer (+seller on processed) | REFUND_* |
| Auto-complete | buyer + seller | ORDER_AUTOCOMPLETED |
| Payout PAID | seller | PAYOUT_PAID |
| Payment flagged REVIEW / reconciliation discrepancy | admin | ADMIN_ALERT |

## Delivery pipeline
1. Domain service writes the `notifications` row **in the same transaction** as the state change (outbox-lite).
2. After commit, `NotificationPublisher` sends: (a) STOMP to `/user/{id}/queue/notifications` for live sessions; (b) Web Push (VAPID) to all of the user's `push_subscriptions` — payload: title, body, deep-link URL.
3. Failures in (2) are logged, never break the domain transaction; dead subscriptions (410) are deleted.

## Client behaviour
- Bell badge = unread count from /auth/me, incremented live via WS.
- Notification list paginated; tapping deep-links (e.g., `/orders/ORD-...`) and marks read (optimistic — the one allowed optimistic update).
- Push permission requested only after first successful order, from a settings toggle — never on first page load.

## Email (minimal in v1)
Email used only for verification and password reset (Spring Mail + any SMTP). Order emails are out of scope v1 — in-app + push cover it.
