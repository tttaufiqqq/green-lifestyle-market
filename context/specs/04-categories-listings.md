# Spec 04 — Categories & Listings

## Goal
FR-C2..C5: verified users create/edit/soft-delete listings with images and fulfilment options; admin moderates; admin manages categories.

## Design
ProductService owns listing rules from docs/domain-rules.md (fulfilment coherence, qty-vs-reservations, delete guard). ImageService: whitelist MIME, ≤2 MB, re-encode (Thumbnailator), store UPLOAD_DIR/products/{id}/, max 5.

## Implementation
1. CategoryController (public GET tree) + admin CRUD.
2. ListingController (/me/listings CRUD + images + status PATCH) with ownership checks in service.
3. Slug generator (title-kebab + id). SOLD_OUT/ACTIVE auto-toggle hooks (used later by order flows).
4. Frontend: create/edit listing form per diagrams/pages/create-listing.md; My Listings table; admin categories + product moderation screens.

## Dependencies
Spec 03.

## Pages
Create/Edit Listing, My Listings, Admin Categories, Admin Listings.

## DB objects
categories, products, product_images.

## API endpoints
"Seller listings", /categories, admin categories/products from docs/api-endpoints.md.

## Files Changed
backend com.glm.catalog/**; frontend features/listings/**, features/admin/{categories,products}/**

## Verify
- [ ] Unverified email blocked from POST /me/listings (R03-E2)
- [ ] Listing without fulfilment option → 422 E-LIST-FULFIL
- [ ] 6th image rejected; 3 MB PNG rejected; webp accepted ≤2 MB
- [ ] Non-owner PUT → 403 E-AUTH-OWN
- [ ] Admin suspend hides listing from public catalog (FR-C5)
