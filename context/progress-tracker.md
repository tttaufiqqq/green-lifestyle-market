# Progress Tracker

Status keys: TODO / IN-PROGRESS / DONE / BLOCKED

## Planning
| Item | Status |
|---|---|
| Context files | DONE |
| docs/ suite | DONE |
| ADRs 0001–0005 | DONE |
| Diagrams (use-cases, activity, pages) | DONE |
| Specs 01–10 | DONE |

## Implementation (build in spec order)
| Spec | Unit | Status | Notes |
|---|---|---|---|
| 01 | Project setup & CI | DONE | pom.xml, common/error, SecurityConfig, HealthController, Vite scaffold, CI, nginx |
| 02 | DB migrations (Flyway V1–V6) | DONE | V1–V6 + R__dev_seed + entities + repos; MigrationIT passes against Oracle 23ai FREEPDB1 |
| 03 | Auth & accounts | DONE | AuthController, AuthService, UserService, TokenService, MailService, AccountController; frontend auth pages + Zustand store |
| 04 | Categories & listings | DONE | CategoryController, AdminCategoryController, ListingController, AdminProductController; CategoryService, ProductService, ListingValidator, ImageService, SlugGenerator; frontend MyListingsPage, ListingFormPage, AdminCategoriesPage, AdminProductsPage; Thumbnailator added |
| 05 | Browse, search, product detail | DONE | CatalogQueryService (dynamic JPQL + Oracle Text), ProductPublicController; frontend BrowsePage (URL-synced filters), ProductDetailPage, FilterBar, ProductCard |
| 06 | Cart | TODO | |
| 07 | Checkout, orders, ToyyibPay | TODO | |
| 08 | Order lifecycle & fulfilment | TODO | |
| 09 | Refunds, payouts, admin, reconciliation | TODO | |
| 10 | Notifications, articles, user guide | TODO | |

## Decisions log (pointers)
- DB: Oracle 23ai Free — adr/0002 (supersedes initial MariaDB pick). Frontend: React SPA — adr/0003. Settlement: escrow — adr/0004. Auth: session — adr/0005.
