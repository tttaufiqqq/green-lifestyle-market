# Spec 07 — Checkout, Orders, ToyyibPay

## Goal
FR-K2..K4, FR-P1..P5: preview → atomic checkout (payment + per-seller orders + reservations) → ToyyibPay bill → idempotent verified callback → PAID orders; expiry sweeper. The money-critical spec.

## Design
Per docs/payments.md and UC-06/07/08. CheckoutService (one @Transactional method: lock products FOR UPDATE → validate → create payment/orders/snapshots/reservations) is separate from ToyyibPayClient (createBill, getBillTransactions; RestClient with timeouts, secret masked in logs) and from PaymentCallbackService (webhook insert → verify → settle TX). PaymentSweeper @Scheduled(fixedDelay=60s) expires PENDING past expires_at, skipping REVIEW. Fee math in a pure FeeCalculator (half-up to sen) — property-tested.

## Implementation
1. CheckoutController (POST /checkout/preview, POST /checkout) with verified-email gate and RM1 minimum (E-CHK-MIN).
2. ToyyibPayClient with sen conversion at the boundary (`movePointRight(2).longValueExact()`).
3. Callback endpoint (CSRF-exempt, rate-limited) + return handler redirecting to SPA result page; both funnel into PaymentCallbackService.
4. Settle/expire transitions via OrderTransitionService stub (full machine in spec 08); notifications written in-TX (outbox rows), publisher stub logs.
5. Frontend: checkout page per diagrams/pages/checkout.md, payment result page with polling per payment-result.md, cart clearing on SUCCESS.
6. WireMock stub of ToyyibPay for integration tests; manual E2E script against dev.toyyibpay.com documented in testing-notes.

## Dependencies
Spec 06. Blocks 08/09.

## Pages
Checkout, Payment Result.

## DB objects
payments, orders, order_items, stock_reservations, webhook_events.

## API endpoints
"Checkout & payments" section of docs/api-endpoints.md.

## Files Changed
backend com.glm.order/checkout/**, com.glm.payment/**; frontend features/checkout/**

## Verify (money-critical list from testing-notes.md)
- [ ] Duplicate callback → exactly one settlement (FR-P1)
- [ ] Tampered amount → payment REVIEW, orders untouched, admin notified (FR-P2)
- [ ] Sweeper vs late callback race deterministic under 2 threads (R08-E1/R07-E2)
- [ ] Property test: Σ order.total = payment.amount; seller_net formula holds
- [ ] Concurrent checkouts on last unit → exactly one succeeds (FOR UPDATE)
- [ ] Sandbox golden path paid end-to-end via Cloudflare Tunnel
