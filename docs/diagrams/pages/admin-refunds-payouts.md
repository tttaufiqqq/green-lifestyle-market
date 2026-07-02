# Page: Admin Refunds (`/admin/refunds`) & Payouts (`/admin/payouts`)
```
Refund queue:
+----------------------------------------------------------------------+
| [REQUESTED (2)] [APPROVED] [PROCESSED] [REJECTED]                     |
| RF-31 ORD-...124 RM97.00 buyer:Amin "item not as described" [Review]  |
+----------------------------------------------------------------------+
Review modal: order timeline + payment verify data
  [Approve] [Reject + note]  -> APPROVED: [Record transfer: bank ref __]
  -> PROCESSED (order REFUNDED)

Payouts:
+----------------------------------------------------------------------+
| Eligible by seller:                                                   |
|  Nabila R.  3 orders  Σ net RM 68.40   [Create payout]                |
|  Mei Ling   1 order   Σ net RM 25.65   [Create payout]                |
| Pending payouts:                                                      |
|  PO-...046  Nabila R. RM68.40 PENDING  [Mark paid: bank ref ____]     |
+----------------------------------------------------------------------+
Reconciliation page: date picker -> table gateway vs local, diff rows red.
```
