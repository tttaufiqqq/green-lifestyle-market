# Database Design — Oracle Database 23ai Free

Target: Oracle 23ai Free, PDB `FREEPDB1`, charset AL32UTF8. Managed by Flyway (Oracle-specific DDL). App connects as least-privilege schema user `GLM_APP`.

## Type & convention mapping (Oracle)
| Concept | Oracle implementation |
|---|---|
| Primary keys | `NUMBER GENERATED ALWAYS AS IDENTITY` (JPA: `GenerationType.SEQUENCE` on the backing sequence where insert batching matters) |
| Money (RM) | `NUMBER(10,2)`; sen (integer) only at the ToyyibPay boundary |
| Enum-like status | `VARCHAR2(20 CHAR)` + named `CHECK (col IN (...))` constraint |
| Boolean | `BOOLEAN` (native in 23ai) |
| Short text | `VARCHAR2(n CHAR)` (CHAR semantics for multibyte safety) |
| Long text (description, article body) | `CLOB` |
| JSON payloads | native `JSON` type (webhook payloads, notification data, audit meta) |
| Time | `TIMESTAMP` stored UTC (`Instant` in JPA) |
| Row history / audit | **Flashback Data Archive** `GLM_FDA` (7-year retention) on `payments`, `orders`, `payouts` — replaces triggers/manual history |
| Catalog search | **Oracle Text** `CONTEXT` index on products title+description (MULTI_COLUMN_DATASTORE), `SYNC (ON COMMIT)`, queried with `CONTAINS()` |
| Locking | `SELECT ... FOR UPDATE` on product/payment rows in checkout & callback transactions |

## ERD (crow's foot, ASCII)

```
 users ||--o{ products            users ||--o| carts           users ||--o{ notifications
 users ||--o| seller_bank_accounts users ||--o{ push_subscriptions
 users ||--o{ audit_logs          users ||--o{ articles (author, admin only)

 categories ||--o{ categories (parent)     categories ||--o{ products
 products  ||--o{ product_images           products  ||--o{ cart_items
 products  ||--o{ order_items              products  ||--o{ stock_reservations
 carts     ||--o{ cart_items

 users(buyer) ||--o{ payments      payments ||--o{ orders        payments ||--o{ webhook_events
 users(buyer) ||--o{ orders        users(seller) ||--o{ orders
 orders   ||--o{ order_items       orders ||--o{ stock_reservations
 orders   ||--o| refunds           orders ||--o| payout_items
 payouts  ||--o{ payout_items      users(seller) ||--o{ payouts

                       +-----------+          +------------+
        +------------->|  users    |<-------------------+  |
        |              +-----------+          |         |  |
        |               |1      |1            |         |  |
        |              *|      *|             |         |  |
  +------------+   +----------+ +---------+   |         |  |
  | categories |--<| products | | carts   |   |         |  |
  +------------+   +----------+ +---------+   |         |  |
        |1              |1  |1      |1        |         |  |
        |              *|  *|      *|         |         |  |
        |     +---------------+ +------------+|         |  |
        |     |product_images | | cart_items ||         |  |
        |     +---------------+ +------------+|         |  |
        |                                     |         |  |
  +----------+ 1    * +----------+ *      1 +----------+|  |
  | payments |-------<|  orders  |>---------|users(s/b)||  |
  +----------+        +----------+          +----------+|  |
       |1               |1   |1   |1                    |  |
      *|               *|   0..1| 0..1|                 |  |
  +---------------+ +-------------+ +---------+ +--------------+
  |webhook_events | | order_items | | refunds | | payout_items |
  +---------------+ +-------------+ +---------+ +--------------+
                                                       |*
                                                       |1
                                                  +---------+
                                                  | payouts |
                                                  +---------+
```

## Table catalogue (18 tables)

### 1. users
| Column | Type | Constraints | Notes |
|---|---|---|---|
| id | NUMBER identity | PK | |
| name | VARCHAR2(100 CHAR) | NOT NULL | |
| email | VARCHAR2(190 CHAR) | NOT NULL, UNIQUE | login identifier |
| password_hash | VARCHAR2(100 CHAR) | NOT NULL | BCrypt |
| phone | VARCHAR2(15 CHAR) | NULL | `601XXXXXXXXX`, validated app-side |
| role | VARCHAR2(10 CHAR) | CHECK IN ('USER','ADMIN'), DEFAULT 'USER' | sellers are USERs with listings |
| affiliation | VARCHAR2(15 CHAR) | CHECK IN ('UTEM_STUDENT','UTEM_STAFF','PUBLIC'), DEFAULT 'PUBLIC' | |
| status | VARCHAR2(10 CHAR) | CHECK IN ('ACTIVE','SUSPENDED'), DEFAULT 'ACTIVE' | |
| email_verified_at | TIMESTAMP | NULL | null = unverified (cannot sell/checkout) |
| created_at / updated_at | TIMESTAMP | NOT NULL | app-managed |

### 2. verification_tokens
id PK identity; user_id FK → users ON DELETE CASCADE; token_hash CHAR(64) UNIQUE (SHA-256; raw token only in the email link); purpose VARCHAR2 CHECK IN ('EMAIL_VERIFY','PASSWORD_RESET'); expires_at TIMESTAMP NOT NULL (24 h verify / 1 h reset); used_at TIMESTAMP NULL (single-use).

### 3. seller_bank_accounts
One per user (payout destination). user_id FK UNIQUE; bank_name VARCHAR2(50 CHAR); account_no VARCHAR2(20 CHAR); holder_name VARCHAR2(100 CHAR); verified BOOLEAN DEFAULT FALSE (admin verifies before first payout); timestamps.

### 4. categories
id PK identity; parent_id FK → categories NULL (one nesting level); name VARCHAR2(60 CHAR); slug VARCHAR2(80 CHAR) UNIQUE; is_active BOOLEAN DEFAULT TRUE; sort_order NUMBER(3). Seeded: Sustainable Stationery, Reusable Bottles & Containers, Organic Food, Eco Electronics, Sustainable Fashion, Home & Living, Renewable Energy, Books & Education, Others.

### 5. products
| Column | Type | Constraints / Notes |
|---|---|---|
| id | NUMBER identity PK | |
| seller_id | FK → users | |
| category_id | FK → categories | |
| title | VARCHAR2(120 CHAR) NOT NULL | |
| slug | VARCHAR2(140 CHAR) UNIQUE | title + id suffix |
| description | CLOB NOT NULL | |
| item_condition | VARCHAR2(10 CHAR) CHECK IN ('NEW','LIKE_NEW','GOOD','FAIR') | renamed from `condition` for cross-dialect cleanliness |
| price | NUMBER(10,2) NOT NULL CHECK (price >= 1.00) | ToyyibPay FPX minimum RM1 |
| quantity | NUMBER(5) NOT NULL DEFAULT 1 CHECK (quantity >= 0) | used items default 1 |
| allow_meetup | BOOLEAN DEFAULT TRUE | |
| allow_shipping | BOOLEAN DEFAULT FALSE | |
| shipping_fee | NUMBER(10,2) NULL | required if allow_shipping |
| meetup_location | VARCHAR2(120 CHAR) NULL | |
| sustainability_note | VARCHAR2(255 CHAR) NULL | |
| status | VARCHAR2(10 CHAR) CHECK IN ('DRAFT','ACTIVE','SOLD_OUT','SUSPENDED','DELETED') DEFAULT 'DRAFT' | DELETED = soft delete |
| created_at / updated_at | TIMESTAMP | |

Table CHECK: `(allow_meetup OR allow_shipping)`. Indexes: (status, category_id, created_at), (seller_id, status). Search: Oracle Text CONTEXT index `products_search_ctx` over title+description (MULTI_COLUMN_DATASTORE preference, SYNC ON COMMIT) → queries use `CONTAINS(title, :q, 1) > 0`.

### 6. product_images
id PK; product_id FK CASCADE; path VARCHAR2(255 CHAR); sort_order NUMBER(2) DEFAULT 0; is_primary BOOLEAN DEFAULT FALSE. Max 5 per product (app rule). Index (product_id, sort_order).

### 7. carts / 8. cart_items
carts: id PK; buyer_id FK UNIQUE (one active cart per user); timestamps.
cart_items: id PK; cart_id FK CASCADE; product_id FK CASCADE; quantity NUMBER(3) CHECK (quantity >= 1); UNIQUE(cart_id, product_id). Prices are NOT stored here — read live and re-validated at checkout.

### 9. payments  *(in GLM_FDA — Flashback history)*
One payment covers one checkout, which may split into several orders (one per seller).
| Column | Type | Notes |
|---|---|---|
| id | NUMBER identity PK | |
| payment_no | CHAR(15) UNIQUE | `PAY-YYMM-XXXXXX` |
| buyer_id | FK → users | |
| amount | NUMBER(10,2) NOT NULL | sum of child orders' totals |
| status | VARCHAR2(10 CHAR) CHECK IN ('PENDING','SUCCESS','FAILED','EXPIRED','REVIEW') DEFAULT 'PENDING' | REVIEW = flagged mismatch (admin) |
| toyyibpay_bill_code | VARCHAR2(20 CHAR) UNIQUE NULL | from createBill |
| toyyibpay_ref_no | VARCHAR2(30 CHAR) NULL | FPX ref |
| verified_at | TIMESTAMP NULL | when getBillTransactions confirmed |
| expires_at | TIMESTAMP NOT NULL | now + PAYMENT_EXPIRY_MINUTES |
| paid_at | TIMESTAMP NULL | |
| created_at / updated_at | TIMESTAMP | |

### 10. orders  *(in GLM_FDA)*
| Column | Type | Notes |
|---|---|---|
| id | NUMBER identity PK | |
| order_no | CHAR(15) UNIQUE | `ORD-YYMM-XXXXXX` |
| payment_id | FK → payments | |
| buyer_id / seller_id | FK → users | denormalized seller for fast queries |
| status | VARCHAR2(20 CHAR) CHECK IN ('PENDING_PAYMENT','PAID','CONFIRMED','SHIPPED','READY_FOR_MEETUP','COMPLETED','CANCELLED','EXPIRED','REFUND_REQUESTED','REFUNDED') | state machine in domain-rules.md |
| fulfilment_method | VARCHAR2(10 CHAR) CHECK IN ('MEETUP','SHIPPING') NOT NULL | per seller-group at checkout |
| ship_name/ship_phone/ship_address1/ship_address2/ship_postcode/ship_city/ship_state | VARCHAR2 | NULL unless SHIPPING |
| meetup_location | VARCHAR2(120 CHAR) NULL | |
| meetup_note | VARCHAR2(255 CHAR) NULL | seller fills at READY_FOR_MEETUP |
| tracking_no / courier | VARCHAR2(40 CHAR) NULL | seller fills at SHIPPED |
| subtotal / shipping_fee / total | NUMBER(10,2) | total = subtotal + shipping_fee |
| platform_fee | NUMBER(10,2) | 5% × subtotal, half-up to sen |
| seller_net | NUMBER(10,2) | subtotal − platform_fee + shipping_fee |
| cancelled_reason | VARCHAR2(255 CHAR) NULL | |
| confirmed_at/shipped_at/completed_at/cancelled_at | TIMESTAMP NULL | lifecycle stamps |
| created_at / updated_at | TIMESTAMP | |

Indexes: (buyer_id, status), (seller_id, status), (payment_id), (status, created_at) for scheduled jobs.

### 11. order_items
id PK; order_id FK CASCADE; product_id FK; title_snapshot VARCHAR2(120 CHAR); condition_snapshot VARCHAR2(10 CHAR); unit_price NUMBER(10,2); quantity NUMBER(3); line_total NUMBER(10,2). Snapshots make orders immutable history.

### 12. stock_reservations
Guards against oversell between "checkout created" and "payment result".
id PK; product_id FK; order_id FK CASCADE; quantity NUMBER(3); status VARCHAR2(10 CHAR) CHECK IN ('HELD','CONSUMED','RELEASED') DEFAULT 'HELD'; expires_at TIMESTAMP; created_at. Index (status, expires_at) for the sweeper. Invariant: `available = products.quantity − SUM(HELD)`; on SUCCESS, HELD → CONSUMED and quantity decremented in the same transaction.

### 13. webhook_events
Idempotency ledger for ToyyibPay. id PK; payment_id FK NULL (resolved after parse); source VARCHAR2 CHECK IN ('CALLBACK','RETURN','QUERY'); idempotency_key VARCHAR2(80 CHAR) **UNIQUE** — `billcode:refno:status`; raw_payload JSON NOT NULL; processed BOOLEAN DEFAULT FALSE; processed_at TIMESTAMP NULL; created_at. Duplicate callbacks violate the UNIQUE key → acknowledged and ignored.

### 14. refunds
id PK; order_id FK **UNIQUE** (max one per order); requested_by FK → users; reason VARCHAR2(500 CHAR); amount NUMBER(10,2) (= order.total; no partial refunds v1); status VARCHAR2 CHECK IN ('REQUESTED','APPROVED','REJECTED','PROCESSED') DEFAULT 'REQUESTED'; admin_id FK NULL; admin_note VARCHAR2(255 CHAR); bank_ref VARCHAR2(60 CHAR) NULL; processed_at TIMESTAMP NULL; timestamps.

### 15. payouts *(in GLM_FDA)* / 16. payout_items
payouts: id PK; payout_no CHAR(14) UNIQUE `PO-YYMM-XXXXXX`; seller_id FK; amount NUMBER(10,2) (sum of items); status VARCHAR2 CHECK IN ('PENDING','PAID','FAILED') DEFAULT 'PENDING'; admin_id FK NULL; bank_ref VARCHAR2(60 CHAR) NULL; paid_at TIMESTAMP NULL; timestamps.
payout_items: id PK; payout_id FK CASCADE; order_id FK **UNIQUE** (an order is paid out exactly once); amount NUMBER(10,2) (= order.seller_net).

### 17. articles
id PK; author_id FK → users (ADMIN); title VARCHAR2(150 CHAR); slug VARCHAR2(170 CHAR) UNIQUE; excerpt VARCHAR2(300 CHAR); body_md CLOB; cover_image VARCHAR2(255 CHAR) NULL; status VARCHAR2 CHECK IN ('DRAFT','PUBLISHED') DEFAULT 'DRAFT'; published_at TIMESTAMP NULL; timestamps. Oracle Text CONTEXT index on title+body_md.

### 18. notifications / push_subscriptions / audit_logs
notifications: id PK; user_id FK CASCADE; type VARCHAR2(40 CHAR); title VARCHAR2(120 CHAR); body VARCHAR2(255 CHAR); data JSON (deep-link ids); read_at TIMESTAMP NULL; created_at. Index (user_id, read_at).
push_subscriptions: id PK; user_id FK CASCADE; endpoint VARCHAR2(500 CHAR) UNIQUE; p256dh VARCHAR2(120 CHAR); auth VARCHAR2(40 CHAR); created_at.
audit_logs: id PK; user_id FK NULL; action VARCHAR2(60 CHAR); entity_type VARCHAR2(40 CHAR); entity_id NUMBER; meta JSON; ip VARCHAR2(45 CHAR); created_at. Append-only: `GLM_APP` gets INSERT/SELECT only (no UPDATE/DELETE grants).

## Derived invariants (service-enforced, test-asserted)
1. `payments.amount = Σ orders.total` for its child orders.
2. `orders.total = subtotal + shipping_fee`; `seller_net = subtotal − platform_fee + shipping_fee`.
3. An order reaches COMPLETED only from SHIPPED/READY_FOR_MEETUP; only COMPLETED orders may appear in payout_items.
4. `products.quantity >= 0` always (also DB CHECK); decrement happens exactly once per order (payment SUCCESS).
5. REFUNDED orders never appear in payout_items; an order is in at most one of {refunds PROCESSED, payout_items}.

## Flashback Data Archive setup (audit history)
```sql
CREATE FLASHBACK ARCHIVE glm_fda TABLESPACE users QUOTA 2G RETENTION 7 YEAR;
ALTER TABLE payments FLASHBACK ARCHIVE glm_fda;
ALTER TABLE orders   FLASHBACK ARCHIVE glm_fda;
ALTER TABLE payouts  FLASHBACK ARCHIVE glm_fda;
-- point-in-time audit query:
-- SELECT * FROM orders AS OF TIMESTAMP (SYSTIMESTAMP - INTERVAL '1' DAY) WHERE id = :id;
```

## Flyway migration plan
| Version | Content |
|---|---|
| V1__users_auth.sql | users, verification_tokens, seller_bank_accounts, audit_logs (+ audit grants) |
| V2__catalog.sql | categories (+seed), products, product_images, Oracle Text preferences + CONTEXT indexes |
| V3__cart.sql | carts, cart_items |
| V4__orders_payments.sql | payments, orders, order_items, stock_reservations, webhook_events |
| V5__money_ops.sql | refunds, payouts, payout_items |
| V6__content_notify_audit.sql | articles, notifications, push_subscriptions, GLM_FDA + ALTER ... FLASHBACK ARCHIVE |

Provisioning note: the schema owner needs `CTXAPP` role (Oracle Text DDL) and `FLASHBACK ARCHIVE ADMINISTER` granted once by an admin init script before V2/V6 run (documented in environment.md).

## Constraints in sentence form

### Entity integrity (primary keys)
Every table has a single-column numeric primary key that is system-generated (identity) and can never be null, duplicated, or changed. No business value (email, order number, bill code) is used as a primary key; business identifiers are enforced separately as unique keys.

### users
- Each user must have a name, an email address, and a password hash; none of these may be null.
- No two users may share the same email address (unique constraint), because email is the login identifier.
- A user's role must be either USER or ADMIN, and defaults to USER.
- A user's affiliation must be one of UTEM_STUDENT, UTEM_STAFF, or PUBLIC, and defaults to PUBLIC.
- A user's account status must be either ACTIVE or SUSPENDED, and defaults to ACTIVE.
- A user whose email_verified_at is null is treated as unverified and may not create listings or check out (application-enforced rule based on this column).

### verification_tokens
- Every token must belong to an existing user; if that user is deleted, their tokens are deleted with them (cascade).
- No two tokens may share the same token hash (unique constraint), so a token can never resolve to two users.
- A token's purpose must be either EMAIL_VERIFY or PASSWORD_RESET.
- Every token must carry an expiry timestamp; a token whose used_at is set may never be accepted again (single use, application-enforced).

### seller_bank_accounts
- Each bank account must belong to an existing user, and a user may have at most one bank account (unique foreign key).
- Bank name, account number, and holder name are all mandatory.
- The verified flag defaults to false; a payout may only be created for a seller whose bank account is verified (application-enforced).

### categories
- Every category must have a name and a slug, and no two categories may share the same slug (unique constraint).
- A category may optionally reference another category as its parent, and that parent must exist; only one level of nesting is allowed (application-enforced).
- The is_active flag defaults to true; inactive categories are hidden from the public catalog.

### products
- Every product must belong to an existing seller (user) and an existing category; neither reference may be null, and neither referenced row may be deleted while products point at it.
- Every product must have a title, a description, and a slug, and no two products may share the same slug (unique constraint).
- The item condition must be one of NEW, LIKE_NEW, GOOD, or FAIR.
- The price must be at least RM1.00 (check constraint), matching the FPX minimum transaction amount.
- The quantity can never be negative (check constraint) and defaults to 1.
- Every product must offer at least one fulfilment option: allow_meetup or allow_shipping must be true (table-level check constraint).
- If shipping is allowed, a shipping fee must be provided, and if meetup is allowed, a meetup location must be provided (application-enforced pair rules).
- The status must be one of DRAFT, ACTIVE, SOLD_OUT, SUSPENDED, or DELETED, and defaults to DRAFT; DELETED is a soft delete, so the row is never physically removed while history references it.

### product_images
- Every image must belong to an existing product; deleting the product deletes its images (cascade).
- The image path is mandatory, and a product may hold at most five images (application-enforced).

### carts and cart_items
- Each cart must belong to an existing user, and a user may have at most one cart (unique foreign key).
- Every cart item must belong to an existing cart and reference an existing product; deleting either the cart or the product removes the cart item (cascade).
- A given product may appear at most once per cart (composite unique constraint on cart and product); repeated adds merge into that single row.
- A cart item's quantity must be at least 1 (check constraint); prices are deliberately not stored in the cart, so the live product price is always the source of truth until checkout.

### payments
- Every payment must belong to an existing buyer (user).
- Every payment must have an amount and an expiry timestamp; the amount must equal the sum of the totals of its child orders (application-enforced invariant).
- No two payments may share the same payment number, and no two payments may share the same ToyyibPay bill code (unique constraints), so any gateway callback resolves to exactly one payment.
- The status must be one of PENDING, SUCCESS, FAILED, EXPIRED, or REVIEW, and defaults to PENDING; a payment may be marked SUCCESS only after server-side verification against the gateway (application-enforced, FR-P2).

### orders
- Every order must reference an existing payment, an existing buyer, and an existing seller; none of these references may be null.
- No two orders may share the same order number (unique constraint).
- The status must be one of the ten defined lifecycle states (PENDING_PAYMENT, PAID, CONFIRMED, SHIPPED, READY_FOR_MEETUP, COMPLETED, CANCELLED, EXPIRED, REFUND_REQUESTED, REFUNDED); every change of status must follow the state machine in domain-rules.md (application-enforced).
- The fulfilment method is mandatory and must be either MEETUP or SHIPPING; shipping address fields are required when and only when the method is SHIPPING (application-enforced).
- Subtotal, shipping fee, total, platform fee, and seller net are all mandatory, and must satisfy: total = subtotal + shipping fee, and seller net = subtotal − platform fee + shipping fee (application-enforced invariants, property-tested).

### order_items
- Every order item must belong to an existing order (deleted with it, cascade) and reference an existing product (which may not be hard-deleted while referenced).
- Title snapshot, condition snapshot, unit price, quantity, and line total are all mandatory, so the order's contents remain historically accurate even if the product is later edited.

### stock_reservations
- Every reservation must reference an existing product and an existing order; deleting the order removes its reservations (cascade).
- The status must be one of HELD, CONSUMED, or RELEASED, and defaults to HELD; every reservation must carry an expiry timestamp.
- At any moment, a product's available quantity equals its stored quantity minus the sum of its HELD reservations, and the stored quantity itself can never go below zero (check constraint plus application-enforced invariant).

### webhook_events
- Every event must record its source (CALLBACK, RETURN, or QUERY) and its raw JSON payload.
- No two events may share the same idempotency key, which is built as billcode:refno:status (unique constraint); this is the mechanism that makes duplicate gateway callbacks harmless, because the second insert fails and processing stops.

### refunds
- Every refund must reference an existing order, and an order may have at most one refund (unique foreign key).
- The requester must be an existing user, the reason and amount are mandatory, and the amount always equals the full order total in v1.
- The status must be one of REQUESTED, APPROVED, REJECTED, or PROCESSED, and defaults to REQUESTED; a refund may be marked PROCESSED only when a bank transfer reference has been recorded (application-enforced).
- A refund may not be processed for an order that already appears in a payout (application-enforced invariant, error E-REF-PAIDOUT).

### payouts and payout_items
- Every payout must belong to an existing seller, must have an amount, and no two payouts may share the same payout number (unique constraint).
- The payout status must be one of PENDING, PAID, or FAILED, and defaults to PENDING; marking a payout PAID requires a bank transfer reference (application-enforced).
- Every payout item must belong to an existing payout (deleted with it, cascade) and reference an existing order.
- An order may appear in at most one payout item across the entire system (unique constraint on order_id); this is the database-level guarantee that a seller is paid for an order exactly once, even under concurrent admin actions.
- Only orders that are COMPLETED, have no open refund, and belong to the payout's seller may be included in a payout (application-enforced at creation time inside the transaction).

### articles
- Every article must have an author who is an existing user (an admin), a title, a slug, and a body.
- No two articles may share the same slug (unique constraint).
- The status must be either DRAFT or PUBLISHED, and defaults to DRAFT; only PUBLISHED articles are publicly readable.

### notifications, push_subscriptions, audit_logs
- Every notification must belong to an existing user and carry a type, title, and body; deleting the user deletes their notifications (cascade).
- Every push subscription must belong to an existing user, and no two subscriptions may share the same endpoint URL (unique constraint); deleting the user deletes their subscriptions (cascade).
- Every audit log entry must record an action; the user reference is optional so that system-initiated actions can also be logged.
- The audit log is append-only: the application schema user holds only INSERT and SELECT privileges on it, so existing entries can never be updated or deleted by the application (privilege-level constraint).

### Cross-cutting referential rules
- All foreign keys point to existing rows; the only cascade deletions are child data that is meaningless without its parent (tokens, images, cart items, order items, reservations, payout items, notifications, subscriptions). Money-bearing rows (payments, orders, refunds, payouts) are never cascade-deleted, and users or products referenced by them cannot be removed, which preserves the financial audit trail.
- All timestamps are stored in UTC, and created_at/updated_at are mandatory on every business table.
- Historical states of payments, orders, and payouts are preserved automatically by the Flashback Data Archive for seven years, so past values remain queryable even after updates.
