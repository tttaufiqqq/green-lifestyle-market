# Activity: Refund and Payout Money Movement

```
REFUND                                          PAYOUT
buyer/seller trigger                            order COMPLETED, no refund open
      |                                               |
[refund REQUESTED]                              admin opens /admin/payouts/eligible
      |  order -> REFUND_REQUESTED                    | grouped by seller, sum seller_net
admin reviews (order timeline,                        v
payment verification)                          create payout PENDING
      |                                        + payout_items (order_id UNIQUE)
  +---+--------+                                      |
approve      reject                            admin bank-transfers net amount
  |             |                                     |
[APPROVED]  [REJECTED]                         mark PAID + bank_ref
  |             | order resumes prior status          |
admin bank      v                              TX{ payout PAID, audit log }
transfer     notify buyer                             |
  |                                            notify seller (PAYOUT_PAID)
mark PROCESSED + bank_ref
  |
TX{ refund PROCESSED, order REFUNDED,
    restore stock if pre-handover, audit }
  |
notify buyer + seller

Escrow invariant (checked daily):
ToyyibPay balance = held buyer money + unpaid seller_net − processed outflows
```
