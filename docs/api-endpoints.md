# API Endpoints — REST `/api/v1`

Conventions: JSON only. Auth via session cookie + `X-CSRF-TOKEN` header on mutations. Errors use the envelope in `docs/error-catalogue.md`. Lists are paginated: `?page=0&size=12` → `{ content, page, size, totalElements, totalPages }`. Roles: `G` guest, `U` user, `A` admin, `O` owner-only (resource must belong to caller).

## Auth & account
| Method | Path | Role | Purpose |
|---|---|---|---|
| POST | /auth/register | G | body: name, email, password, phone?, affiliation. Sends verify email. 201 |
| POST | /auth/login | G | body: email, password → sets session cookie, returns `MeResponse` |
| POST | /auth/logout | U | invalidates session |
| GET | /auth/me | U | current user + unread notification count + cart count |
| POST | /auth/verify-email | G | body: token → marks verified |
| POST | /auth/forgot-password | G | body: email (always 200, no enumeration) |
| POST | /auth/reset-password | G | body: token, newPassword |
| GET/PUT | /me/profile | U | name, phone, affiliation |
| PUT | /me/password | U | currentPassword, newPassword |
| GET/PUT | /me/bank-account | U | payout destination; PUT resets `verified=false` |

## Catalog (public)
| Method | Path | Role | Purpose |
|---|---|---|---|
| GET | /categories | G | active category tree |
| GET | /products | G | filters: `q` (fulltext), `categoryId`, `condition`, `minPrice`, `maxPrice`, `fulfilment=MEETUP\|SHIPPING`, `sort=newest\|price_asc\|price_desc`. Only ACTIVE with quantity>0 |
| GET | /products/{slug} | G | detail + images + seller public info (name, join date, active listing count) |

## Seller listings
| Method | Path | Role | Purpose |
|---|---|---|---|
| GET | /me/listings | U | own listings, all statuses, filter by status |
| POST | /me/listings | U | create (DRAFT or ACTIVE). Requires verified email. Validates fulfilment flags/shipping_fee/meetup_location coherence |
| PUT | /me/listings/{id} | U,O | edit; blocked fields (price/qty down) validated against HELD reservations |
| POST | /me/listings/{id}/images | U,O | multipart, max 5 images, 2 MB each |
| DELETE | /me/listings/{id}/images/{imgId} | U,O | |
| PATCH | /me/listings/{id}/status | U,O | ACTIVE ⇄ DRAFT; DELETED (soft) only if no open orders |

## Cart
| Method | Path | Role | Purpose |
|---|---|---|---|
| GET | /cart | U | items grouped by seller, live prices, availability warnings |
| POST | /cart/items | U | body: productId, quantity. Rejects own products, inactive, insufficient stock |
| PATCH | /cart/items/{id} | U | quantity |
| DELETE | /cart/items/{id} | U | |

## Checkout & payments
| Method | Path | Role | Purpose |
|---|---|---|---|
| POST | /checkout/preview | U | body: fulfilment choice per sellerGroup + address if shipping → totals per order, grand total. No side effects |
| POST | /checkout | U | validates + locks stock, creates payment(PENDING) + orders(PENDING_PAYMENT) + reservations(HELD), calls ToyyibPay createBill → 201 `{ paymentNo, billCode, paymentUrl }` |
| GET | /payments/{paymentNo} | U,O | status polling for the result page |
| POST | /payments/toyyibpay/callback | ToyyibPay | form-encoded server-to-server callback. Always 200 "OK". See payments.md |
| GET | /payments/toyyibpay/return | G | browser return URL → verifies then 302 to SPA `/payment/result/{paymentNo}` |

## Orders — buyer
| Method | Path | Role | Purpose |
|---|---|---|---|
| GET | /me/orders | U | own purchases; filter by status |
| GET | /me/orders/{orderNo} | U,O | detail incl. timeline of status stamps |
| POST | /me/orders/{orderNo}/cancel | U,O | allowed in PENDING_PAYMENT or PAID (pre-CONFIRMED). PAID → auto refund REQUESTED |
| POST | /me/orders/{orderNo}/confirm-receipt | U,O | SHIPPED/READY_FOR_MEETUP → COMPLETED |
| POST | /me/orders/{orderNo}/refund-request | U,O | body: reason. Allowed post-CONFIRMED, pre-COMPLETED |

## Orders — seller
| Method | Path | Role | Purpose |
|---|---|---|---|
| GET | /me/sales | U | orders where seller = me |
| GET | /me/sales/{orderNo} | U,O | |
| POST | /me/sales/{orderNo}/confirm | U,O | PAID → CONFIRMED |
| POST | /me/sales/{orderNo}/reject | U,O | body: reason. PAID → CANCELLED + refund REQUESTED |
| POST | /me/sales/{orderNo}/ship | U,O | body: courier, trackingNo. CONFIRMED → SHIPPED (SHIPPING orders) |
| POST | /me/sales/{orderNo}/ready-meetup | U,O | body: meetupNote. CONFIRMED → READY_FOR_MEETUP (MEETUP orders) |
| GET | /me/payouts | U | own payouts + eligible (COMPLETED, unpaid) orders |

## Notifications & content
| Method | Path | Role | Purpose |
|---|---|---|---|
| GET | /me/notifications | U | paginated, newest first |
| PATCH | /me/notifications/{id}/read · /me/notifications/read-all | U | |
| POST/DELETE | /me/push-subscriptions | U | register/remove Web Push subscription |
| GET | /articles · /articles/{slug} | G | PUBLISHED only |
| WS | /ws (STOMP) — subscribe `/user/queue/notifications` | U | real-time notification push |

## Admin (`/admin/...`, role A; all actions audit-logged)
| Method | Path | Purpose |
|---|---|---|
| GET | /admin/dashboard | counters: users, active listings, orders by status, escrow balance, pending refunds/payouts |
| GET/PATCH | /admin/users, /admin/users/{id}/status | list/suspend/reactivate |
| CRUD | /admin/categories | manage tree |
| GET/PATCH | /admin/products, /admin/products/{id}/status | moderate (SUSPENDED) |
| GET | /admin/orders, /admin/orders/{orderNo} | all orders, filters |
| GET | /admin/refunds | queue by status |
| POST | /admin/refunds/{id}/approve · /reject | body: adminNote |
| POST | /admin/refunds/{id}/process | body: bankRef → PROCESSED; order → REFUNDED |
| GET | /admin/payouts/eligible | COMPLETED orders not yet in payout_items, grouped by seller |
| POST | /admin/payouts | body: sellerId, orderIds[] → creates payout PENDING |
| POST | /admin/payouts/{id}/mark-paid | body: bankRef |
| GET | /admin/reconciliation?date= | ToyyibPay getBillTransactions vs local payments diff report |
| CRUD | /admin/articles | article CMS |

## Status codes
200 OK · 201 Created · 204 No Content (deletes) · 400 validation · 401 unauthenticated · 403 forbidden/ownership · 404 not found · 409 state conflict (illegal transition, stock, duplicate) · 422 domain rule violation · 500 with errorId.
