# Error Catalogue

## Envelope (all non-2xx)
```json
{ "error": { "code": "E-CHK-STOCK", "message": "human-readable", "details": [ {"field":"...", "issue":"..."} ], "errorId": "uuid-for-500s" } }
```
Frontend maps `code` to copy; `message` is a fallback. 500s expose only `errorId` (correlate in logs).

## Codes
| Code | HTTP | Meaning / user copy hint |
|---|---|---|
| E-VAL | 400 | Field validation failed (details list) |
| E-AUTH-CRED | 401 | Wrong email or password |
| E-AUTH-LOCKED | 429 | Too many attempts, try again in 15 min |
| E-AUTH-SUSPENDED | 403 | Account suspended |
| E-AUTH-OWN | 403 | Not your resource |
| E-AUTH-VERIFY | 422 | Verify your email first |
| E-NOTFOUND | 404 | Resource not found |
| E-LIST-FULFIL | 422 | Listing needs at least one fulfilment option |
| E-LIST-QTY-HELD | 409 | Quantity below held reservations |
| E-LIST-OPEN-ORDERS | 409 | Cannot delete: open orders exist |
| E-CART-OWN | 422 | You cannot buy your own item |
| E-CART-STOCK | 409 | Only N left in stock |
| E-CHK-EMPTY | 422 | Cart is empty |
| E-CHK-MIN | 422 | Total must be at least RM1.00 |
| E-CHK-ADDRESS | 400 | Shipping address required |
| E-CHK-STOCK | 409 | Items changed while you were checking out (details: items) |
| E-PAY-GATEWAY | 502 | Payment provider unavailable, try again |
| E-PAY-STATE | 409 | Payment already finalised |
| E-ORD-STATE | 409 | Action not allowed in current order status |
| E-REF-EXISTS | 409 | Refund already requested |
| E-REF-PAIDOUT | 409 | Order already paid out — contact support |
| E-PO-BANK | 422 | Seller bank account missing/unverified |
| E-PO-DUP | 409 | Order already in another payout |
| E-UPLOAD-TYPE / E-UPLOAD-SIZE | 400 | Image must be JPEG/PNG/WebP ≤ 2 MB |
| E-RATE | 429 | Slow down |
| E-INTERNAL | 500 | Something broke; quote errorId |

Rules: services throw typed exceptions carrying a code; `GlobalExceptionHandler` is the only place codes become HTTP. New codes must be added here first (fail loud, CLAUDE.md rule 12).
