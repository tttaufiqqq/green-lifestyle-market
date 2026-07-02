# Use Case Diagram (ASCII)

```
                          GREEN LIFESTYLE MARKET
        +------------------------------------------------------------------+
        |                                                                  |
Guest --+--> (UC-01 Register)   (UC-04 Browse/Search)   (Read Articles)    |
  o     |         |                     ^   ^                 ^            |
 /|\    |         v                     |   |                 |            |
 / \    |   (UC-02 Login) <---------- User o|                 |            |
        |                              /|\  |                 |            |
        |    +-------------------------/-\--+-----------------+            |
        |    |            as Buyer          |        as Seller             |
        |    v                              v                              |
        | (UC-05 Add to Cart)          (UC-03 Create/Manage Listing)       |
        | (UC-06 Checkout & Pay)------(include)---> (UC-07 Process         |
        |        |                                   Payment Callback)     |
        | (UC-10 Confirm Receipt /         (UC-09 Confirm/Ship/            |
        |        Cancel / Refund Req)             Ready-Meetup)            |
        |    (Manage Profile & Bank)        (View Sales & Payouts)         |
        |                                                                  |
        |  Admin                                                           |
        |    o --> (UC-11 Resolve Refunds)  (UC-12 Create/Pay Payouts)     |
        |   /|\--> (Reconciliation Report)  (Moderate Users/Listings)      |
        |   / \--> (Manage Categories)      (Publish Articles)             |
        |                                                                  |
        |  System (scheduled)                     ToyyibPay (external)     |
        |    [UC-08 Expire Unpaid]  [Auto-complete]   o------------------- |
        |    [Daily Reconciliation]                  /|\  callback/verify  |
        +------------------------------------------------------------------+
```
Include/extend notes: UC-06 «include» UC-07; UC-10 cancel «extend» UC-11 (refund path); UC-09 reject «extend» UC-11.
