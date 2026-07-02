# Spec 08 — Order Lifecycle & Fulfilment

## Goal
FR-O1..O6: full state machine (confirm/reject, ship/ready-meetup, confirm receipt, cancellations, refund request entry) + auto-complete job + order timelines for both parties.

## Design
OrderTransitionService completes the machine from docs/domain-rules.md: transition table (status × event → new status) as data, guards (ownership, fulfilment method), effects (timestamps, stock restore on pre-handover cancel, refund auto-create, notifications) executed atomically. AutoCompleteJob @Scheduled(cron 03:00 MYT) completes SHIPPED/READY_FOR_MEETUP older than AUTO_COMPLETE_DAYS with no open refund. Timeline endpoint reads lifecycle stamps (+ FDA versions later if wanted).

## Implementation
1. Buyer endpoints: cancel, confirm-receipt, refund-request. Seller endpoints: confirm, reject, ship, ready-meetup (docs/api-endpoints.md "Orders").
2. Refund entity creation on: buyer cancel of PAID, seller reject, buyer refund request (status REQUESTED — resolution is spec 09).
3. Frontend: My Orders + detail (timeline, contextual action buttons, refund modal) per my-orders.md; My Sales + detail (confirm/reject, ship modal with courier+tracking, meetup modal) per my-sales.md; buyer phone revealed to seller only post-CONFIRMED.

## Dependencies
Spec 07.

## Pages
My Orders, Order Detail, My Sales, Sale Detail.

## DB objects
orders (transitions), refunds (creation), stock_reservations/products (restore).

## API endpoints
"Orders — buyer" and "Orders — seller" sections.

## Files Changed
backend com.glm.order/lifecycle/**, com.glm.refund/entity+create; frontend features/orders/**, features/sales/**

## Verify
- [ ] Every illegal transition in the status×event table → 409 E-ORD-STATE (exhaustive unit test)
- [ ] Buyer cancel of PAID restores stock and creates refund REQUESTED (FR-O4)
- [ ] SOLD_OUT product returns to ACTIVE after cancellation restore (FR-C4)
- [ ] Auto-complete skips orders with open refunds (FR-O3/O5)
- [ ] Non-owner actions → 403 on every lifecycle endpoint
