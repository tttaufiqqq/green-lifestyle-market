# ADR-0004: Platform Escrow with Manual Seller Payouts

## Context
ToyyibPay settles to a single merchant account. Options: (a) every seller registers their own ToyyibPay account and bills, (b) platform escrow + admin payouts, (c) no online payment (COD only).

## Decision
Escrow: all payments to the platform account; 5% commission on item subtotal; admin pays sellers (bank transfer) after order completion; refunds also manual transfers. Payouts/refunds recorded with bank references, DB-enforced exactly-once.

## Consequences
+ Zero onboarding friction for student sellers (no merchant KYC); buyer protection falls out of the order lifecycle; platform has a revenue model.
+ Rich, realistic engineering: idempotent webhooks, state machine, reconciliation, payout batching, audit — the actual learning goals.
− Admin operational load (manual transfers) and custody of user funds — fine for a workshop/dev context; a real launch would need e-money licensing review (BNM) and would likely move to a PSP with split payments.
− ToyyibPay dev lacks refund API → refunds are operationally manual by design.
