# Activity: End-to-End Checkout & Payment

```
BUYER                    SPA                    API/DB                        TOYYIBPAY
  |  open checkout        |                       |                              |
  |---------------------->| GET /cart             |                              |
  |                       |---------------------->| group by seller, revalidate  |
  |  pick fulfilment/addr |<----------------------|                              |
  |---------------------->| POST /checkout/preview|                              |
  |   review totals       |<----- totals ---------| fee=5% subtotal, per order   |
  |  confirm & pay        |                       |                              |
  |---------------------->| POST /checkout        |                              |
  |                       |---------------------->| TX{ lock products FOR UPDATE |
  |                       |                       |     check available          |
  |                       |                       |     payment PENDING (30m)    |
  |                       |                       |     orders PENDING_PAYMENT   |
  |                       |                       |     snapshots + reservations}|
  |                       |                       |---- createBill (sen) ------->|
  |                       |                       |<--- BillCode ----------------|
  |                       |<- {paymentUrl} -------|                              |
  |  redirected           |                       |                              |
  |------------------------------- FPX payment page --------------------------->|
  |   pays via bank       |                       |                              |
  |                       |                       |<===== POST callback ========|
  |                       |                       | insert webhook (idempotent)  |
  |                       |                       |---- getBillTransactions ---->|
  |                       |                       |<--- verified paid, amount ---|
  |                       |                       | TX{ payment SUCCESS          |
  |                       |                       |     orders PAID              |
  |                       |                       |     reservations CONSUMED    |
  |                       |                       |     stock -= qty }           |
  |                       |                       | notify buyer + sellers (WS)  |
  |<-- return redirect ------------------------------------------ 302 ----------|
  |                       | poll GET /payments/no |                              |
  |   "Payment successful"|<--- SUCCESS ----------|                              |
```
Failure/abandon branches: see docs/payments.md failure matrix and UC-08.
