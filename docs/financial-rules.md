# Financial Rules — Escrow, Reconciliation, Audit

## Ledger model (implicit ledger from state)
Money truth is derivable from tables, no separate ledger table in v1:
- **Inflows** = payments SUCCESS (verified_at not null).
- **Owed to sellers** = Σ seller_net of COMPLETED orders without a PAID payout_item.
- **Escrow held (buyers')** = Σ total of orders in PAID/CONFIRMED/SHIPPED/READY_FOR_MEETUP/REFUND_REQUESTED.
- **Outflows** = Σ payouts PAID + Σ refunds PROCESSED.
- **Platform earnings** = Σ platform_fee of COMPLETED orders (recognized at completion, not at payment).
Invariant (DR-3): ToyyibPay settled balance = escrow held + owed to sellers + unrecognized platform fees − pending settlements. The reconciliation job asserts this daily.

## Daily reconciliation job (03:30 MYT) + on-demand /admin/reconciliation
1. Pull ToyyibPay `getBillTransactions` for all billCodes with activity in the window.
2. Compare against local `payments`:
   - Gateway paid, local not SUCCESS → **missed callback**: run the standard verify path now.
   - Local SUCCESS, gateway not paid → **critical alert** (should be impossible with FR-P2).
   - Amount deltas → alert with both values.
3. Emit a report row per discrepancy; ADMIN_ALERT notification if count > 0. Reports stored 12 months.

## Payout integrity
- payout_items.order_id UNIQUE → an order is settled to a seller at most once (DB-enforced, not just app-enforced).
- Payout creation re-checks each order is COMPLETED with no open refund inside the transaction.
- Bank reference mandatory to mark PAID; payouts and refunds require an audit_log row with admin id (write both in one TX).

## Refund integrity
- Max one refund per order (UNIQUE order_id).
- PROCESSED requires bank_ref; sets order REFUNDED atomically.
- Blocked if the order already has a payout_item (E-REF-PAIDOUT) — that situation means auto-complete raced a dispute; resolve by clawback outside the system and record via admin note. Prevented in practice because REFUND_REQUESTED orders are excluded from auto-complete and payouts.

## Audit & retention
- payments, orders, payouts are tracked by Oracle Flashback Data Archive (GLM_FDA): any historical row state is queryable with `AS OF TIMESTAMP` / `VERSIONS BETWEEN`.
- audit_logs append-only; no UPDATE/DELETE grants for the app user on that table.
- Retention: GLM_FDA retention is set to 7 years; audit_logs kept ≥ 7 years (align with Malaysian record-keeping norms); uploads and notifications prunable after 12 months.

## Rounding
Half-up to sen on fee computation only; all other figures are exact sums. Property test: for random carts, Σ order totals = payment amount and subtotal − fee + shipping = seller_net for every order.
