# Spec 09 — Refunds, Payouts, Admin, Reconciliation

## Goal
FR-M1..M4 + FR-A6/C5 admin surface: refund resolution, payout batching with exactly-once settlement, reconciliation report, user/listing moderation, admin dashboard.

## Design
RefundService: approve/reject (reject restores prior status from stamps), process (bank_ref mandatory → order REFUNDED + conditional stock restore + audit, one TX; blocked by E-REF-PAIDOUT if paid out). PayoutService: eligibility query (COMPLETED, no payout_item, no open refund, verified bank), create (payout PENDING + items; UNIQUE order_id absorbs races), mark-paid (bank_ref + audit in one TX). ReconciliationService: per docs/financial-rules.md — pull getBillTransactions for active billCodes, diff against payments, persist report, ADMIN_ALERT on discrepancies; @Scheduled 03:30 MYT + on-demand endpoint. Dashboard aggregates via read-only queries.

## Implementation
1. Admin controllers per docs/api-endpoints.md "Admin" (dashboard, users, products moderation, categories CRUD, orders, refunds, payouts, reconciliation) — all behind ROLE_ADMIN, all mutations audit-logged.
2. Bank account verification toggle (unblocks R12-A1).
3. Frontend: admin layout (sidebar per ui-context.md), dashboard per admin-dashboard.md, refunds queue + review modal, payouts eligible/pending per admin-refunds-payouts.md, reconciliation date-picker table, users/listings/categories tables.
4. Seller-facing payouts page (read-only) per payouts-bank.md.

## Dependencies
Spec 08.

## Pages
Admin Dashboard, Admin Users/Listings/Categories/Orders/Refunds/Payouts/Reconciliation, Seller Payouts.

## DB objects
refunds (resolution), payouts, payout_items, audit_logs; reads across all money tables.

## API endpoints
"Admin" section + GET /me/payouts.

## Files Changed
backend com.glm.refund/**, com.glm.payout/**, com.glm.admin/**; frontend features/admin/**, features/payouts/**

## Verify
- [ ] Concurrent payout creation for same orders → second fails cleanly listing conflicts (R12-E1)
- [ ] Refund PROCESSED blocked when order already paid out (E-REF-PAIDOUT)
- [ ] Reject returns order to exact prior status (R11-A1)
- [ ] Reconciliation flags a locally-missing gateway payment and triggers the verify path
- [ ] Every admin mutation produces an audit_logs row with admin id
