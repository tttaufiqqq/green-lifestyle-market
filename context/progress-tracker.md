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
| 06 | Cart | DONE | CartValidator, CartLoader, CartService, CartController; V7 migration; cart store + badge; add-to-cart on ProductDetailPage |
| 07 | Checkout, orders, ToyyibPay | DONE | CheckoutService/Recorder/Validator, FeeCalculator, ToyyibPayClient, PaymentSettleService, WebhookEventService, PaymentCallbackService, PaymentSweeper, OrderTransitionService stub; V8 migration; CheckoutPage, PaymentResultPage, FulfilmentSelector |
| 08 | Order lifecycle & fulfilment | DONE | TransitionGuard (full table), StockRestorer, RefundCreator, OrderNotifier, AutoCompleteJob (03:00 MYT); OrderTransitionService (8 events), BuyerOrderService/Controller, SellerOrderService/Controller, OrderMapper; frontend MyOrdersPage, OrderDetailPage, OrderTimeline, RefundModal, MySalesPage, SaleDetailPage, SaleActionModals |
| 09 | Refunds, payouts, admin, reconciliation | DONE | RefundService (approve/reject/process), PayoutService (eligible/create/markPaid, payout-no PO-YYMM-XXXXXX, race guard E-PO-DUP), ReconciliationService+Job (03:30 MYT, heal MISSED_CALLBACK), AdminDashboardService; 11 DTOs; AdminLayout+Dashboard+Users+Orders+Refunds+Payouts+ReconciliationPages; MyPayoutsPage; routes wired |
| 10 | Notifications, articles, user guide | DONE* | WebSocketConfig (STOMP over native WS, auth via SecurityConfig); NotificationPublisher (STOMP + Web Push/VAPID via webpush-java 6.1.3, wired through a @PostPersist listener on Notification + NotificationContext bridge — OrderNotifier/PaymentSettleService needed zero changes); NotificationController/Service, PushSubscriptionController/Service; ArticleService + ArticlePublicController/AdminArticleController. Frontend: notifications (NotificationBell, NotificationsPage, lib/ws.js, lib/push.js, public/sw.js, stores/notifications.js), articles (ArticlesPage, ArticleDetailPage, AdminArticlesPage, ArticleFormPage), /help (HelpPage + bundled docs/user-guide/*.md copy + Fuse.js search). Fixed 2 unrelated pre-existing bugs that were blocking all builds: UserRepository.java missing `List` import (backend), admin/order/payout .api.js using a default import against lib/api.js's named export (frontend). All 5 commits build/compile/test clean (`mvn test`, `npm run build`, `npm test`). *Not yet done: the spec's Verify checklist needs live manual QA (2s cross-session WS delivery, Web Push with tab closed, kill-test for atomic commit, unauthenticated WS handshake rejection, /help search behavior) — nothing here exercises a real browser session. Also deliberately deferred: REFUND_APPROVED/REJECTED/PROCESSED, PAYOUT_PAID and ADMIN_ALERT notification triggers don't exist yet — RefundService/PayoutService/reconciliation never write notification rows, and those files are outside spec 10's declared Files Changed, so this was left as a follow-up rather than expanding scope. |

## Decisions log (pointers)
- DB: Oracle 23ai Free — adr/0002 (supersedes initial MariaDB pick). Frontend: React SPA — adr/0003. Settlement: escrow — adr/0004. Auth: session — adr/0005.
- Web Push library: nl.martijndwars:webpush is unpublished from Maven Central; use its maintained successor dev.blanke.webpush:webpush (same nl.martijndwars.webpush package, builder-based API) instead.
