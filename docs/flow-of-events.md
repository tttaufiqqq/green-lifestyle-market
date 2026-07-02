# Flow of Events

Format per use case: ID, Name, Actor, Description, Pre-conditions, Post-conditions, Normal Flow (R[ID]-[N]), Alternative Flow (resumes at R[ID]-[N]), Exception Flow. Steps are imperative, actor-first.

---

## UC-01 Register Account
**Actor:** Guest. **Description:** Guest creates an account and verifies email.
**Pre-conditions:** 1. Guest is not authenticated. 2. Email is not already registered.
**Post-conditions:** 1. User row exists with ACTIVE status. 2. Verification email sent. 3. Audit log entry created.
**Normal Flow:**
- R01-1 Guest opens the Register page and submits name, email, password, affiliation, optional phone.
- R01-2 System validates inputs (email format, password ≥ 8 chars, phone format if given).
- R01-3 System hashes the password with BCrypt and creates the user with `email_verified_at = NULL`.
- R01-4 System creates an EMAIL_VERIFY token (24 h) and emails the verification link.
- R01-5 System displays "check your email" and redirects Guest to Login.
**Alternative Flow:**
- R01-A1 (at R01-2) Email already registered: System shows a generic "unable to register" message without confirming the email exists; flow ends.
**Exception Flow:**
- R01-E1 Email delivery fails: System still creates the user, logs the failure, and offers "resend verification" on the login page.

## UC-02 Login
**Actor:** User. **Description:** User authenticates and receives a session.
**Pre-conditions:** 1. User account exists with ACTIVE status.
**Post-conditions:** 1. Server session created (spring-session-jdbc). 2. Audit log LOGIN entry.
**Normal Flow:**
- R02-1 User submits email and password on the Login page.
- R02-2 System verifies credentials against the BCrypt hash.
- R02-3 System creates a session, sets the HttpOnly Secure cookie, and returns the user profile with unread counts.
- R02-4 System redirects User to the Home page.
**Alternative Flow:** —
**Exception Flow:**
- R02-E1 (at R02-2) Wrong credentials: System increments the failure counter and shows a generic error.
- R02-E2 (at R02-2) 5 failures within 15 minutes: System locks logins for that email for 15 minutes (429).
- R02-E3 (at R02-2) User status SUSPENDED: System denies login with "account suspended, contact support".

## UC-03 Create Listing
**Actor:** Seller (verified User). **Description:** Seller publishes a product for sale.
**Pre-conditions:** 1. Seller is logged in. 2. Seller's email is verified.
**Post-conditions:** 1. Product row exists (DRAFT or ACTIVE). 2. Images stored under UPLOAD_DIR.
**Normal Flow:**
- R03-1 Seller opens Create Listing and fills title, description, category, condition, price, quantity, sustainability note.
- R03-2 Seller enables meetup (with location) and/or shipping (with flat fee).
- R03-3 Seller uploads 1–5 images; System validates MIME/size and re-encodes each image.
- R03-4 Seller submits as ACTIVE; System validates price ≥ RM1.00 and at least one fulfilment option.
- R03-5 System creates the product with a unique slug and confirms with a link to the public page.
**Alternative Flow:**
- R03-A1 (at R03-4) Seller saves as DRAFT instead: System stores the listing hidden from catalog; flow resumes at R03-5 without a public link.
**Exception Flow:**
- R03-E1 (at R03-3) Image invalid: System rejects only that file with reason (type/size) and keeps the form state.
- R03-E2 (at R03-4) Email not verified: System blocks with 422 E-LIST-VERIFY and links to resend verification.

## UC-04 Browse and Search Products
**Actor:** Guest. **Description:** Guest finds products via search and filters.
**Pre-conditions:** —
**Post-conditions:** —
**Normal Flow:**
- R04-1 Guest opens Browse; System returns page 1 of ACTIVE products with quantity > 0, newest first.
- R04-2 Guest enters a keyword and/or applies filters (category, condition, price range, fulfilment).
- R04-3 System runs a fulltext + indexed filter query and returns the paginated result with total count.
- R04-4 Guest opens a product; System shows detail, images, condition badge, fulfilment options, seller public info.
**Alternative Flow:**
- R04-A1 (at R04-3) No results: System shows an empty state with a "clear filters" action; resumes at R04-2.
**Exception Flow:** —

## UC-05 Add to Cart
**Actor:** Buyer. **Description:** Buyer collects items before checkout.
**Pre-conditions:** 1. Buyer is logged in. 2. Product is ACTIVE with available stock.
**Post-conditions:** 1. cart_items row created/updated.
**Normal Flow:**
- R05-1 Buyer clicks Add to Cart on a product with a chosen quantity.
- R05-2 System verifies the product is not the Buyer's own listing and available quantity covers cart quantity.
- R05-3 System upserts the cart item and returns the new cart badge count.
**Alternative Flow:**
- R05-A1 (at R05-3) Item already in cart: System sums quantities capped at availability; resumes at R05-3.
**Exception Flow:**
- R05-E1 (at R05-2) Own product: System rejects with 422 E-CART-OWN.
- R05-E2 (at R05-2) Insufficient stock: System rejects with 409 E-CART-STOCK and shows remaining quantity.

## UC-06 Checkout and Pay (core flow)
**Actor:** Buyer; External: ToyyibPay. **Description:** Buyer converts the cart into orders and pays via FPX.
**Pre-conditions:** 1. Buyer logged in, email verified. 2. Cart has ≥ 1 item. 3. Grand total ≥ RM1.00.
**Post-conditions:** 1. One payment + one order per seller group exist. 2. Stock reservations HELD. 3. ToyyibPay bill created.
**Normal Flow:**
- R06-1 Buyer opens Checkout; System groups cart items by seller and re-validates prices and stock.
- R06-2 Buyer selects a fulfilment method per seller group and enters a shipping address where required.
- R06-3 Buyer requests a preview; System computes per-order subtotal, shipping, platform fee (5%), seller net, and the grand total.
- R06-4 Buyer confirms; System, in one DB transaction: locks product rows (`FOR UPDATE`), re-verifies stock, creates payment (PENDING, expires in 30 min), creates orders (PENDING_PAYMENT) with item snapshots, and inserts HELD reservations.
- R06-5 System calls ToyyibPay `createBill` (amount in sen, callback + return URLs) and stores the billCode.
- R06-6 System returns the payment URL; Buyer is redirected to ToyyibPay and completes FPX payment.
- R06-7 ToyyibPay POSTs the callback; System processes it per UC-07.
- R06-8 Buyer returns to the Payment Result page, which polls `GET /payments/{paymentNo}` until a terminal state and clears purchased items from the cart on SUCCESS.
**Alternative Flow:**
- R06-A1 (at R06-6) Buyer abandons the gateway page: payment stays PENDING until the sweeper expires it (UC-08); flow ends.
**Exception Flow:**
- R06-E1 (at R06-4) Stock changed since cart: System aborts the transaction, returns 409 E-CHK-STOCK listing the affected items, and resumes at R06-1.
- R06-E2 (at R06-5) createBill fails: System marks the payment FAILED, releases reservations, sets orders EXPIRED, and shows a retryable error.

## UC-07 Process Payment Callback (system)
**Actor:** System; External: ToyyibPay. **Description:** Idempotent, verified handling of the payment result.
**Pre-conditions:** 1. Payment exists with a billCode.
**Post-conditions:** 1. Payment in a terminal or flagged state. 2. Exactly-once side effects.
**Normal Flow:**
- R07-1 System receives the callback (refno, billcode, status, amount) and inserts a webhook_events row with key `billcode:refno:status`.
- R07-2 System responds 200 immediately after the insert (processing continues in the same request thread post-insert).
- R07-3 System calls ToyyibPay `getBillTransactions(billCode)` and confirms status = success and amount equals payment.amount (in sen).
- R07-4 System, in one transaction: payment → SUCCESS (paid_at, refNo, verified_at); each child order → PAID; reservations HELD → CONSUMED; product quantities decremented; SOLD_OUT applied at zero.
- R07-5 System writes notifications (buyer: payment received; each seller: new paid order) and pushes them via WebSocket/Web Push.
- R07-6 System marks the webhook event processed.
**Alternative Flow:**
- R07-A1 (at R07-1) Duplicate key: insert fails on UNIQUE; System returns 200 and stops (idempotency); flow ends.
- R07-A2 (at R07-3) Verified status = failed: System sets payment FAILED, orders EXPIRED, releases reservations; resumes at R07-6.
**Exception Flow:**
- R07-E1 (at R07-3) Amount mismatch or API error: System leaves the payment PENDING, flags it for admin review (notification to admin), logs the payload; sweeper will not expire flagged payments.
- R07-E2 Callback arrives after expiry already ran but verification says success: System reverses the expiry inside R07-4's transaction only if stock is still restorable; otherwise flags for admin (manual refund path).

## UC-08 Expire Unpaid Payment (system, scheduled)
**Actor:** System. **Description:** Every minute, release stock held by abandoned checkouts.
**Pre-conditions:** 1. Payments PENDING with `expires_at < now` and not admin-flagged.
**Post-conditions:** 1. Payment EXPIRED, orders EXPIRED, reservations RELEASED.
**Normal Flow:**
- R08-1 System selects expired PENDING payments in batches.
- R08-2 For each, in one transaction: payment → EXPIRED, child orders → EXPIRED, reservations → RELEASED.
- R08-3 System notifies the buyer that the checkout expired.
**Alternative Flow:** —
**Exception Flow:**
- R08-E1 Race with an in-flight callback: row-level lock on the payment serializes the two; whichever commits first wins and the other becomes a no-op (guarded by status check inside the transaction).

## UC-09 Seller Fulfils Order
**Actor:** Seller. **Description:** Seller confirms and ships or prepares meetup.
**Pre-conditions:** 1. Order status PAID (confirm) or CONFIRMED (fulfil). 2. Seller owns the order.
**Post-conditions:** 1. Order CONFIRMED, then SHIPPED or READY_FOR_MEETUP. 2. Buyer notified at each step.
**Normal Flow:**
- R09-1 Seller opens My Sales and views the PAID order detail.
- R09-2 Seller clicks Confirm; System transitions PAID → CONFIRMED and notifies the Buyer.
- R09-3 For SHIPPING: Seller enters courier + tracking number; System transitions CONFIRMED → SHIPPED, stamps shipped_at, notifies the Buyer.
- R09-4 For MEETUP: Seller enters a meetup note (time/place); System transitions CONFIRMED → READY_FOR_MEETUP and notifies the Buyer.
**Alternative Flow:**
- R09-A1 (at R09-2) Seller rejects with a reason: System transitions PAID → CANCELLED, restores stock, auto-creates a refund (REQUESTED), notifies Buyer and Admin; flow ends.
**Exception Flow:**
- R09-E1 Illegal transition (e.g., ship an unconfirmed order): System rejects with 409 E-ORD-STATE.

## UC-10 Buyer Completes or Cancels
**Actor:** Buyer. **Description:** Buyer receives goods, or cancels/asks refund.
**Pre-conditions:** 1. Buyer owns the order.
**Post-conditions:** 1. Order COMPLETED, CANCELLED, or REFUND_REQUESTED.
**Normal Flow:**
- R10-1 Buyer receives the item (courier or meetup) and opens the order detail.
- R10-2 Buyer clicks Confirm Receipt; System transitions SHIPPED/READY_FOR_MEETUP → COMPLETED, stamps completed_at, notifies the Seller, and the order becomes payout-eligible.
**Alternative Flow:**
- R10-A1 (at R10-1) Order still PENDING_PAYMENT: Buyer cancels; System expires it immediately (as UC-08 R08-2); flow ends.
- R10-A2 (at R10-1) Order PAID, seller not yet confirmed: Buyer cancels; System transitions to CANCELLED, restores stock, auto-creates refund REQUESTED; flow ends.
- R10-A3 (at R10-1) Problem with goods (CONFIRMED/SHIPPED/READY_FOR_MEETUP): Buyer submits a refund request with a reason; System transitions to REFUND_REQUESTED and notifies Admin and Seller; flow ends.
- R10-A4 (at R10-2) Buyer inactive: the daily auto-complete job transitions orders 7 days past shipped_at (no open refund) to COMPLETED; flow ends.
**Exception Flow:** —

## UC-11 Admin Resolves Refund
**Actor:** Admin. **Description:** Admin decides and settles a refund by manual bank transfer.
**Pre-conditions:** 1. Refund exists in REQUESTED.
**Post-conditions:** 1. Refund PROCESSED or REJECTED; order state consistent; audit logged.
**Normal Flow:**
- R11-1 Admin opens the refund queue and reviews the request, order timeline, and payment verification data.
- R11-2 Admin approves; System sets refund APPROVED and notifies the Buyer.
- R11-3 Admin transfers the amount to the Buyer's bank outside the system, then records the bank reference.
- R11-4 System sets refund PROCESSED, order → REFUNDED, restores stock if the item was never handed over (pre-SHIPPED/meetup), and notifies Buyer and Seller.
**Alternative Flow:**
- R11-A1 (at R11-2) Admin rejects with a note: System returns the order to its prior status (from the timeline) and notifies the Buyer; flow ends.
**Exception Flow:**
- R11-E1 (at R11-4) Order already in a payout: System blocks with 409 E-REF-PAIDOUT (invariant DR/inv-5) — resolve manually per financial-rules.md.

## UC-12 Admin Pays Out Seller
**Actor:** Admin. **Description:** Admin batches completed orders into a payout and settles it.
**Pre-conditions:** 1. Seller has COMPLETED orders absent from payout_items. 2. Seller bank account exists and is verified.
**Post-conditions:** 1. Payout PAID with bank reference; orders marked paid-out exactly once.
**Normal Flow:**
- R12-1 Admin opens Payouts → Eligible, grouped by seller with Σ seller_net.
- R12-2 Admin selects a seller and orders and creates the payout; System inserts payout (PENDING) + payout_items (UNIQUE order_id enforces exactly-once).
- R12-3 Admin transfers the net amount to the seller's bank, then records the bank reference.
- R12-4 System sets payout PAID, stamps paid_at, notifies the Seller, and audit-logs the action.
**Alternative Flow:**
- R12-A1 (at R12-2) Bank account unverified: System blocks creation with 422 E-PO-BANK and links to the verification task; flow ends.
**Exception Flow:**
- R12-E1 (at R12-2) Concurrent double-create: UNIQUE(order_id) in payout_items fails the second insert; System shows which orders were already assigned.
