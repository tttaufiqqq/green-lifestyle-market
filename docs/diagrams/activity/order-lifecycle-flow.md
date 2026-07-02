# Activity: Order Fulfilment Lifecycle (post-payment)

```
        [order PAID]
             |
   +---------+-----------+
   | seller confirms     | seller rejects / buyer cancels
   v                     v
[CONFIRMED]         [CANCELLED]--->(refund REQUESTED)---> UC-11 admin
   |                     ^
   | fulfilment method   | buyer refund request (problem with goods)
   +----------+          |
SHIPPING      MEETUP     |
   |             |       |
   v             v       |
[SHIPPED]  [READY_FOR_MEETUP]
   | courier+tracking | meetup note
   +---------+--------+
             |
   +---------+----------------------------+
   | buyer confirms receipt               | 7 days pass, no refund open
   v                                      v
[COMPLETED] <-----------------------------+   (auto-complete job)
   |
   v
payout-eligible ---> UC-12 admin payout ---> [payout PAID] -> seller notified
```
