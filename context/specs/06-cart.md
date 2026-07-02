# Spec 06 — Cart

## Goal
FR-K1: persistent per-user cart grouped by seller, with live price/availability revalidation and own-product rejection.

## Design
CartService: upsert semantics (UNIQUE cart_id+product_id), quantity capped at available (= quantity − HELD reservations), rejects own products (DR-4/E-CART-OWN) and non-ACTIVE listings. Cart GET always recomputes: current price, availability warnings, seller grouping. Badge count piggybacks on /auth/me and updates via zustand after mutations.

## Implementation
1. CartController (GET /cart, POST /cart/items, PATCH/DELETE /cart/items/{id}) + CartService + DTOs (CartView grouped by seller with warnings list).
2. Auto-create cart row on first add.
3. Frontend: cart page per diagrams/pages/cart.md, cart store, add-to-cart button states on product detail (own product → Edit Listing).

## Dependencies
Spec 05.

## Pages
Cart; Add-to-Cart interactions on Product Detail.

## DB objects
carts, cart_items (reads products, stock_reservations).

## API endpoints
"Cart" section of docs/api-endpoints.md.

## Files Changed
backend com.glm.cart/**; frontend features/cart/**, stores/cart.js

## Verify
- [ ] Adding own product → 422 E-CART-OWN
- [ ] Adding beyond availability → 409 E-CART-STOCK with remaining qty
- [ ] Same product twice merges quantities capped at availability (R05-A1)
- [ ] Price change on listing reflects on next cart GET with a warning row
- [ ] Cart persists across sessions (DB-backed, not local)
