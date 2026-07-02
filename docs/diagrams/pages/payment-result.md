# Page: Payment Result (`/payment/result/{paymentNo}`)
```
+----------------------------------------------+
|        [spinner]  Verifying payment...       |   (polls GET /payments/{no})
+----------------------------------------------+
SUCCESS:                          FAILED/EXPIRED:
+----------------------------+    +------------------------------+
|   ✔ Payment successful     |    |  ✖ Payment unsuccessful      |
|   PAY-2607-000123          |    |  Nothing was charged? If you |
|   2 orders created:        |    |  were charged, it will show  |
|   ORD-... Nabila (meetup)  |    |  here after verification.    |
|   ORD-... Syafiqah (ship)  |    |  [ Back to Cart ] [Retry]    |
|   [ View My Orders ]       |    +------------------------------+
+----------------------------+
Never reads status from URL params — server state only.
```
