# Page: Checkout (`/checkout`)
```
+----------------------------------------------------------------------+
| Checkout                                          Step 1 of 2         |
|----------------------------------------------------------------------|
| ORDER 1 — Seller: Nabila R. (2 items, RM27.00)                        |
|   Fulfilment: (•) Meetup — FTMK lobby   ( ) Shipping +RM6.00          |
|----------------------------------------------------------------------|
| ORDER 2 — Seller: Syafiqah L. (1 item, RM89.00)                       |
|   Fulfilment: ( ) Meetup   (•) Shipping +RM8.00                       |
|   Delivery address:                                                   |
|   Name [__________]  Phone [601________]                              |
|   Address 1 [______________________] Address 2 [___________]          |
|   Postcode [_____] City [________] State [Johor v]                    |
|----------------------------------------------------------------------|
| Summary                                                               |
|   Order 1 (meetup)                      RM 27.00                      |
|   Order 2 (shipping)          89.00+8.00 RM 97.00                     |
|   GRAND TOTAL                           RM 124.00                     |
|   You will pay securely via FPX (ToyyibPay).                          |
|                                   [ Pay RM124.00 ]                    |
+----------------------------------------------------------------------+
[Pay] -> POST /checkout -> redirect to ToyyibPay hosted page.
```
