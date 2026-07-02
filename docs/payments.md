# Payments — ToyyibPay Integration (Dev Sandbox)

Gateway: ToyyibPay FPX. Dev base URL `https://dev.toyyibpay.com`, production `https://toyyibpay.com` — switched only by `TOYYIBPAY_BASE_URL`. Model: **platform escrow** (adr/0004): all buyer money lands in the single platform ToyyibPay account; sellers are settled by admin payouts.

## One-time setup (dev)
1. Register at dev.toyyibpay.com (sandbox accepts dummy bank details).
2. Create one Category in the dashboard → `TOYYIBPAY_CATEGORY_CODE`.
3. Copy the `userSecretKey` → `TOYYIBPAY_SECRET_KEY`.
4. Expose the backend publicly for callbacks (Cloudflare Tunnel) and set `APP_BASE_URL`.

## API surface used
### createBill — `POST {base}/index.php/api/createBill` (form-encoded)
| Field | Value |
|---|---|
| userSecretKey / categoryCode | from env |
| billName | `GLM {paymentNo}` (≤30 chars, alphanumeric+space) |
| billDescription | `Green Lifestyle Market order` |
| billPriceSetting | `1` (fixed amount) |
| billPayorInfo | `1` (collect payer info) |
| billAmount | **total in sen** (e.g., RM45.90 → `4590`) |
| billReturnUrl | `{APP_BASE_URL}/api/v1/payments/toyyibpay/return` |
| billCallbackUrl | `{APP_BASE_URL}/api/v1/payments/toyyibpay/callback` |
| billExternalReferenceNo | `paymentNo` — our join key |
| billTo / billEmail / billPhone | buyer name/email/phone |
| billPaymentChannel | `0` (FPX) |
| billExpiryDays | `1` (sandbox minimum; real expiry is our 30-min sweeper) |

Response: `[{"BillCode":"abc123"}]` → store on payment; payment URL = `{base}/{BillCode}`.

### Callback — server-to-server POST (form-encoded) to our callback URL
Fields: `refno`, `status` (`1` success, `2` pending, `3` fail), `reason`, `billcode`, `order_id` (= billExternalReferenceNo), `amount`, `transaction_time`.
**Trust policy: the callback is a hint, never the source of truth.**

### Return URL — browser GET redirect with `status_id`, `billcode`, `order_id`
Used only for UX routing; server re-verifies before showing success.

### getBillTransactions — `POST {base}/index.php/api/getBillTransactions`
Body: `billCode` (+ userSecretKey). Returns transaction list with `billpaymentStatus` and `billpaymentAmount`. This is the **verification source of truth** for FR-P2.

## Amount handling
`BigDecimal` RM everywhere internally; convert once at the boundary: `sen = amount.movePointRight(2).longValueExact()`. Verification compares sen-to-sen. Any mismatch → flag, never auto-complete.

## Callback processing (idempotent, verified)
```
ToyyibPay ── POST callback ──> /payments/toyyibpay/callback
   |                                 |
   |                                 v
   |                    INSERT webhook_events
   |                    key = billcode:refno:status
   |                        | dup? ──yes──> respond 200, STOP
   |                        no
   |                        v
   |            getBillTransactions(billcode)  <── verify status+amount
   |                        |
   |            verified success?
   |             |yes                |no(fail)         |error/mismatch
   |             v                   v                 v
   |   TX: payment=SUCCESS    TX: payment=FAILED   flag payment
   |       orders=PAID            orders=EXPIRED   REVIEW + notify admin
   |       reservations CONSUMED  reservations     (leave PENDING)
   |       stock -= qty           RELEASED
   |       notifications          notify buyer
   |             v
   +<── 200 "OK" (always)
```
Concurrency guards: payment row is `SELECT ... FOR UPDATE`d at the start of the transaction; a status check inside the transaction makes replays and expiry races no-ops (UC-07/UC-08).

## Failure matrix
| Scenario | Handling |
|---|---|
| Duplicate callback | UNIQUE webhook key → no-op, 200 |
| Callback lost (never arrives) | Return-URL handler triggers the same verify path; plus reconciliation job catches it next run |
| Callback after our 30-min expiry | If verified paid and stock restorable → un-expire in one TX; else flag → manual refund (UC-11) |
| createBill timeout | Payment FAILED, reservations released, buyer sees retryable error (R06-E2) |
| Verify API down | Payment stays PENDING + flagged; sweeper skips flagged; admin retries verify from dashboard |
| Amount mismatch | Flag REVIEW; never mark SUCCESS (possible tamper/misconfig) |

## Security notes
- Callback endpoint is CSRF-exempt, rate-limited, accepts only expected form fields, and does nothing state-changing without server-side verification.
- ToyyibPay provides no signed webhook in the sandbox — this is exactly why FR-P2 (server-side verify) is mandatory, not optional.
- `userSecretKey` only in env; never logged (mask in ToyyibPayClient logging).

## Refunds
ToyyibPay dev exposes no refund API → refunds are **manual bank transfers by admin**, recorded in the `refunds` table with a bank reference (UC-11). The reconciliation report (financial-rules.md) treats processed refunds as outflows.
