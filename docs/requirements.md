# Requirements

Style rules applied: exact actor named in every requirement; active voice; no nominalization; every NFR has a verifiable measure; IDs are stable references for specs, tests, and the RTM.

## 1. Functional Requirements

### FR-A: Accounts & Auth
- **FR-A1** A guest shall register with name, email, password, optional phone (`601XXXXXXXXX`), and affiliation (UTeM student / UTeM staff / public).
- **FR-A2** The system shall send an email verification link valid for 24 hours; an unverified user shall not create listings or check out.
- **FR-A3** A user shall log in with email and password; the system shall lock further attempts for 15 minutes after 5 consecutive failures for that email.
- **FR-A4** A user shall reset their password via a single-use emailed token valid for 1 hour.
- **FR-A5** A user shall maintain a profile (name, phone, affiliation) and one bank account (bank, account number, holder name) as their payout destination.
- **FR-A6** An admin shall suspend or reactivate any user; a suspended user shall not log in.

### FR-C: Catalog & Listings
- **FR-C1** A guest shall browse and search active products by keyword (Oracle Text on title/description), category, condition, price range, and fulfilment method, with pagination and sorting (newest, price asc/desc).
- **FR-C2** A verified user shall create a listing with title, description, category, condition (NEW/LIKE_NEW/GOOD/FAIR), price (≥ RM1.00), quantity (default 1), up to 5 images, sustainability note, and at least one fulfilment option (meetup with location, and/or shipping with flat fee).
- **FR-C3** A seller shall edit or soft-delete their own listing; the system shall block quantity reduction below currently held reservations and block deletion while open orders reference it.
- **FR-C4** The system shall automatically set a listing to SOLD_OUT when available quantity reaches 0 and back to ACTIVE when stock is restored by a cancellation.
- **FR-C5** An admin shall suspend a listing (removes it from public catalog) with an audit-logged reason.

### FR-K: Cart & Checkout
- **FR-K1** A user shall add products to a cart (not their own products), adjust quantities, and remove items; the cart shall group items by seller and re-validate price and availability on every view.
- **FR-K2** At checkout the user shall choose one fulfilment method per seller group; SHIPPING requires a Malaysian delivery address; MEETUP shows the seller's meetup location.
- **FR-K3** On checkout the system shall, in one transaction: lock product rows, verify availability, create one payment and one order per seller group with snapshotted items and computed totals (subtotal, shipping, 5% platform fee, seller net), and hold stock reservations that expire after 30 minutes.
- **FR-K4** The system shall create a ToyyibPay bill for the payment total and redirect the buyer to the hosted payment page.

### FR-P: Payments
- **FR-P1** The system shall receive ToyyibPay callbacks idempotently: duplicate callbacks (same billcode+refno+status) shall produce no additional side effects.
- **FR-P2** Before marking a payment SUCCESS, the system shall verify the transaction status and amount server-side via ToyyibPay `getBillTransactions`; a mismatch shall flag the payment for admin review instead of completing it.
- **FR-P3** On verified SUCCESS the system shall, in one transaction: set payment SUCCESS, set its orders PAID, consume reservations, decrement stock, and notify buyer and sellers.
- **FR-P4** On FAILED or expiry (30 min) the system shall set payment FAILED/EXPIRED, set orders EXPIRED, and release reservations; a scheduled sweeper shall enforce expiry every minute.
- **FR-P5** The buyer shall see a payment result page reflecting verified server state (never trusting URL parameters).

### FR-O: Order Lifecycle
- **FR-O1** A seller shall confirm a PAID order (→ CONFIRMED) or reject it with a reason (→ CANCELLED + automatic refund request).
- **FR-O2** A seller shall mark a CONFIRMED shipping order SHIPPED with courier and tracking number, or a meetup order READY_FOR_MEETUP with a meetup note.
- **FR-O3** A buyer shall confirm receipt (→ COMPLETED); the system shall auto-complete orders 7 days after SHIPPED/READY_FOR_MEETUP if the buyer takes no action and no refund is open.
- **FR-O4** A buyer shall cancel free of charge while PENDING_PAYMENT; a buyer cancelling a PAID (pre-CONFIRMED) order shall trigger an automatic full-refund request and stock restoration.
- **FR-O5** A buyer shall request a refund with a reason on CONFIRMED/SHIPPED/READY_FOR_MEETUP orders; the order shall enter REFUND_REQUESTED and be excluded from auto-complete and payouts until resolved.
- **FR-O6** Both parties shall view an order timeline showing every status change with timestamps.

### FR-M: Money Operations (Admin)
- **FR-M1** An admin shall approve or reject refund requests with a note; rejecting returns the order to its prior status.
- **FR-M2** An admin shall mark an approved refund PROCESSED with a bank transfer reference; the system shall set the order REFUNDED and restore stock if not yet fulfilled.
- **FR-M3** An admin shall create a payout for a seller from that seller's COMPLETED, not-yet-paid-out orders (amount = Σ seller_net) and later mark it PAID with a bank reference; the system shall guarantee each order is paid out at most once.
- **FR-M4** An admin shall run a reconciliation report for any date comparing ToyyibPay transactions to local payments and listing discrepancies (docs/financial-rules.md).

### FR-N: Notifications, Content, Help
- **FR-N1** The system shall create in-app notifications for: payment result, order PAID (seller), CONFIRMED/SHIPPED/READY_FOR_MEETUP (buyer), cancellation, refund decisions, payout PAID, auto-complete.
- **FR-N2** The system shall deliver notifications in real time over WebSocket to online users and via Web Push to subscribed browsers, marking read state per notification.
- **FR-N3** An admin shall create, edit, publish, and unpublish markdown articles with cover image; guests shall browse and read published articles.
- **FR-N4** The system shall serve a searchable in-app user guide at `/help` rendered from repository markdown.

## 2. Non-Functional Requirements

### Product
| ID | Requirement | Verifiable measure |
|---|---|---|
| NFR-P1 (performance) | Catalog and product-detail endpoints respond quickly under normal load | p95 < 500 ms at 50 concurrent users (k6 test, dev-sized VM) |
| NFR-P2 (performance) | Checkout end-to-end (excluding gateway page) completes fast | p95 < 2 s for POST /checkout |
| NFR-P3 (reliability) | Payment processing never loses or duplicates money state | Integration tests: duplicate callback, out-of-order callback, expiry race — all pass; invariants in database.md hold |
| NFR-P4 (reliability) | Recoverable after crash | Documented restore from nightly Data Pump export (`expdp`) completes in < 30 min, tested once |
| NFR-P5 (usability) | Core buyer journey is self-evident | New user completes browse→checkout in usability test without assistance, < 5 min |
| NFR-P6 (portability) | Runs on commodity Linux | Deploys on Ubuntu 24.04 with 2 vCPU / 4 GB (Proxmox VM verified) |

### Organisational
| ID | Requirement | Measure |
|---|---|---|
| NFR-O1 | Code follows context/code-standards.md (SRP, <200 lines/file with orchestration pattern, layering) | Enforced in code review; checkstyle/ESLint max-lines rule at 200 |
| NFR-O2 | Schema changes only through Flyway migrations | `flyway validate` in CI |
| NFR-O3 | CI runs tests on every PR | GitHub Actions green required to merge |

### External
| ID | Requirement | Measure |
|---|---|---|
| NFR-E1 (legal) | Complies with PDPA 2010: minimal personal data, passwords hashed, bank data visible only to owner and admin | Security review checklist signed off |
| NFR-E2 (interoperability) | ToyyibPay integration follows their documented createBill/callback/getBillTransactions contract, dev and prod switchable by env | Sandbox E2E test passes; base URL is configuration only |
| NFR-E3 (security) | OWASP Top 10 baseline: CSRF protection, session hardening, parameterized queries, upload restrictions, no secrets in repo | ZAP baseline scan: no High findings |

## 3. Domain Requirements
- **DR-1** Prices and fees are in Malaysian Ringgit with 2 decimal places; ToyyibPay amounts are transmitted in sen (integer); FPX minimum transaction is RM1.00.
- **DR-2** Platform commission is 5% of item subtotal, rounded half-up to the sen; shipping fees pass through to the seller in full.
- **DR-3** Escrow rule: money received from a buyer belongs to the buyer until the order is COMPLETED (then owed to seller) or REFUNDED (returned to buyer). At all times: ToyyibPay balance = unpaid seller_net of COMPLETED orders + totals of PAID/CONFIRMED/SHIPPED/READY_FOR_MEETUP/REFUND_REQUESTED orders − processed refunds not yet settled.
- **DR-4** A user may not buy their own listing.
- **DR-5** Phone numbers follow Malaysian format `601XXXXXXXX(X)`; addresses are Malaysian (state list fixed).
- **DR-6** Pre-owned goods are sold as-described; condition grading (NEW/LIKE_NEW/GOOD/FAIR) is mandatory and shown on every listing and order snapshot (implicit consumer-protection expectation made explicit).
