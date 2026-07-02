# Domain Rules

## Order state machine (single source of truth)
```
                 +------------------+
  checkout ----> | PENDING_PAYMENT  |----(30 min sweeper / buyer cancel)---> EXPIRED
                 +--------+---------+
                          | verified payment success
                          v
                 +--------+---------+   seller reject / buyer cancel
                 |      PAID        |-------------------------------+
                 +--------+---------+                               |
                          | seller confirm                          v
                          v                                   CANCELLED ──> refund REQUESTED
                 +--------+---------+   buyer refund request        ^
                 |    CONFIRMED     |------------------------+      |
                 +---+---------+----+                        |      |
        SHIPPING |             | MEETUP                      v      |
                 v             v                     REFUND_REQUESTED
            SHIPPED     READY_FOR_MEETUP                |         |
                 |             |                 admin reject     | admin process
                 |  buyer confirm / 7-day auto   (restore prior)  v
                 +------+------+-----------------------+      REFUNDED
                        v                              |
                   COMPLETED  <────(resume prior)──────+
```
Legal transitions ONLY via `OrderTransitionService.transition(order, event)`; anything else throws `IllegalOrderStateException` → 409 E-ORD-STATE. Every transition writes the timestamp column, an audit_log row, and the notification(s) for that event — atomically.

## Money rules
- Platform fee = round_half_up(subtotal × 5%) to sen. Shipping fee passes to seller untouched.
- seller_net = subtotal − platform_fee + shipping_fee. Computed once at checkout, snapshotted on the order, never recomputed.
- Refunds are always full order total in v1. No partial refunds.
- An order is payout-eligible iff status = COMPLETED AND no payout_items row AND no refund in REQUESTED/APPROVED.

## Stock rules
- available(product) = quantity − Σ reservations HELD.
- Add-to-cart and checkout validate against available, not raw quantity.
- Decrement quantity exactly once (payment SUCCESS); restore on: CANCELLED before handover, REFUNDED before handover, EXPIRED never decremented (reservation released only).
- Product auto-SOLD_OUT at quantity 0; auto-ACTIVE again if restoration brings quantity > 0 and status was SOLD_OUT.

## Listing rules
- At least one fulfilment option; shipping requires shipping_fee ≥ 0; meetup requires meetup_location.
- Seller cannot reduce quantity below Σ HELD reservations; cannot soft-delete with open orders (any non-terminal status).
- Suspended sellers' listings are hidden but their open orders continue to completion.

## Access/ownership rules
- Buyers see only their orders; sellers only their sales; ownership checked in the service layer on every read and mutation (never trust route params).
- A user cannot buy, cart, or checkout their own product (DR-4).
- Bank account data: visible to owner and admin only; never in any public payload.

## Cancellation & refund policy (user-facing summary lives in user-guide)
| When | Who | Result |
|---|---|---|
| PENDING_PAYMENT | buyer | immediate EXPIRED, no money moved |
| PAID (pre-confirm) | buyer or seller(reject) | CANCELLED + auto refund request, stock restored |
| CONFIRMED → before receipt | buyer | REFUND_REQUESTED, admin decides |
| After COMPLETED | — | closed; disputes handled off-platform in v1 |

## Auto jobs
| Job | Schedule | Action |
|---|---|---|
| payment sweeper | every 1 min | UC-08 expiry |
| auto-complete | daily 03:00 MYT | SHIPPED/READY_FOR_MEETUP older than 7 days, no open refund → COMPLETED |
| reconciliation | daily 03:30 MYT | financial-rules.md report; discrepancies notify admin |
