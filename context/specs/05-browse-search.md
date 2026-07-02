# Spec 05 — Browse, Search, Product Detail

## Goal
FR-C1: public catalog with fulltext search, filters, sorting, pagination; product detail page with seller public info.

## Design
Repository query: Oracle Text `CONTAINS(title, :q, 1) > 0` for q (CONTEXT index from spec 02), plus indexed WHERE for filters; Pageable everywhere; only ACTIVE and quantity>0 exposed. DTOs exclude seller private data. TanStack Query caching keyed by filter object; URL-synced filter state.

## Implementation
1. CatalogQueryService + ProductPublicController (GET /products, /products/{slug}).
2. Availability field on detail = quantity − HELD reservations (join stub until spec 07 populates it).
3. Frontend: home/browse per diagrams/pages/home-browse.md (card grid, filter bar, pagination), product detail per product-detail.md.

## Dependencies
Spec 04.

## Pages
Home/Browse, Product Detail.

## DB objects
Reads products, product_images, categories, stock_reservations.

## API endpoints
"Catalog (public)" section of docs/api-endpoints.md.

## Files Changed
backend com.glm.catalog/query/**; frontend features/catalog/**

## Verify
- [ ] q matches title and description tokens (FR-C1)
- [ ] Filters combine (category + condition + price range + fulfilment)
- [ ] DRAFT/SUSPENDED/DELETED/qty=0 never appear publicly
- [ ] p95 < 500 ms with 5k seeded products (quick k6, NFR-P1 smoke)
