# Folder Structure

Principles: package-by-feature (not by layer), orchestration pattern everywhere a flow has more than one responsibility, every coding file under 200 lines, frontend features mirror backend features so specs map 1:1 to folders.

## Repository root
```
green-lifestyle-market/
├── CLAUDE.md  README.md  CHANGELOG.md
├── context/
│   ├── project-overview.md  architecture.md  ui-context.md
│   ├── code-standards.md    progress-tracker.md
│   └── specs/                       # 01..10 build units
├── docs/                            # design source of truth
│   ├── adr/  diagrams/{use-cases,activity,pages}/  user-guide/
│   └── *.md
├── backend/
├── frontend/
├── deploy/                          # nginx.conf, systemd units, cloudflared config, oracle-provision.sql
└── .github/workflows/ci.yml
```

## Backend (`backend/src/main/java/com/glm/`)
Each feature package owns its controller, orchestrator, collaborators, entities, repositories, and DTOs. Nothing reaches into another feature's internals — cross-feature calls go through the other feature's service interface.

```
com/glm/
├── GlmApplication.java
├── common/
│   ├── config/          SecurityConfig, WebSocketConfig, SchedulingConfig, OpenApiConfig
│   ├── error/           ErrorCode, ApiError, DomainException, GlobalExceptionHandler
│   ├── audit/           AuditLogger, AuditLog entity + repository
│   └── util/            MoneyUtil (sen conversion), SlugGenerator, RefNoGenerator
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java                 # login/logout/session orchestration
│   ├── RegistrationOrchestrator.java    # validate -> create user -> token -> mail
│   ├── TokenService.java  MailService.java  LoginThrottle.java
│   └── dto/
├── user/
│   ├── ProfileController.java  ProfileService.java
│   ├── BankAccountController.java  BankAccountService.java
│   ├── entity/  repository/  dto/
├── catalog/
│   ├── CategoryController.java  CategoryService.java  CategoryAdminController.java
│   ├── listing/         ListingController, ListingOrchestrator (create/edit flow),
│   │                    ListingValidator, ImageService, ListingStatusService
│   ├── query/           ProductPublicController, CatalogQueryService (CONTAINS search),
│   │                    AvailabilityCalculator
│   ├── entity/  repository/  dto/
├── cart/
│   ├── CartController.java  CartService.java  CartViewAssembler.java
│   ├── entity/  repository/  dto/
├── order/
│   ├── checkout/        CheckoutController, CheckoutOrchestrator, CheckoutValidator,
│   │                    FeeCalculator, OrderFactory, ReservationWriter
│   ├── lifecycle/       BuyerOrderController, SellerOrderController,
│   │                    OrderTransitionService (state machine), TransitionEffects
│   │                    (stamps, stock restore, refund auto-create, notify hooks)
│   ├── jobs/            PaymentSweeper, AutoCompleteJob
│   ├── entity/  repository/  dto/
├── payment/
│   ├── PaymentController.java           # status polling, return handler
│   ├── CallbackController.java          # CSRF-exempt webhook endpoint
│   ├── PaymentCallbackOrchestrator.java # insert event -> verify -> settle/flag
│   ├── ToyyibPayClient.java  WebhookRecorder.java  PaymentSettler.java
│   ├── entity/  repository/  dto/
├── refund/
│   ├── RefundAdminController.java  RefundOrchestrator.java  RefundGuards.java
│   ├── entity/  repository/  dto/
├── payout/
│   ├── PayoutAdminController.java  SellerPayoutController.java
│   ├── PayoutOrchestrator.java  EligibilityQuery.java
│   ├── entity/  repository/  dto/
├── admin/
│   ├── DashboardController.java  DashboardQueryService.java
│   ├── UserAdminController.java  ReconciliationController.java
│   ├── ReconciliationOrchestrator.java  GatewayDiffer.java
├── notification/
│   ├── NotificationController.java  PushSubscriptionController.java
│   ├── NotificationWriter.java      # in-TX row writes (outbox-lite)
│   ├── NotificationPublisher.java   # post-commit fan-out orchestrator
│   ├── StompSender.java  WebPushSender.java
│   ├── entity/  repository/  dto/
└── article/
    ├── ArticleController.java  ArticleAdminController.java  ArticleService.java
    ├── entity/  repository/  dto/
```

```
backend/src/main/resources/
├── application.yml  application-dev.yml  application-prod.yml
└── db/migration/    V1__users_auth.sql .. V6__content_notify_audit.sql, R__dev_seed.sql
backend/src/test/java/com/glm/
├── <feature>/...Test.java               # unit, mirrors main
└── it/                                  # Testcontainers integration: MigrationIT,
                                         # CheckoutConcurrencyIT, CallbackIdempotencyIT,
                                         # SweeperRaceIT, PayoutUniquenessIT, SecurityMatrixIT
```

## Frontend (`frontend/src/`)
Pages orchestrate, hooks own logic, components render, api modules own HTTP — the frontend mirror of the orchestration pattern.

```
src/
├── main.jsx  routes.jsx  App.jsx
├── lib/
│   ├── api.js           # fetch wrapper: CSRF echo, error envelope, 401 redirect
│   ├── ws.js            # STOMP connect/subscribe helper
│   ├── push.js          # Web Push subscribe/unsubscribe
│   └── format.js        # RM money, MYT dates
├── stores/              # zustand: auth.js, cart.js, notifications.js
├── components/
│   ├── ui/              # shadcn primitives
│   └── shared/          # Navbar, Footer, BottomTabs, Pagination, EmptyState,
│                        # ConfirmModal, StatusBadge, MoneyText
├── features/
│   ├── auth/            LoginPage, RegisterPage, VerifyPage, ResetPage,
│   │                    useAuthForms.js, auth.api.js
│   ├── catalog/         BrowsePage, ProductDetailPage, FilterBar, ProductCard,
│   │                    useCatalogFilters.js, catalog.api.js
│   ├── listings/        MyListingsPage, ListingFormPage, ImageUploader,
│   │                    FulfilmentFields, useListingForm.js, listings.api.js
│   ├── cart/            CartPage, SellerGroup, CartLine, cart.api.js
│   ├── checkout/        CheckoutPage, AddressForm, SellerGroupCard, TotalsSummary,
│   │                    PaymentResultPage, useCheckout.js, usePaymentPoll.js,
│   │                    checkout.api.js
│   ├── orders/          MyOrdersPage, OrderDetailPage, OrderTimeline,
│   │                    RefundRequestModal, orders.api.js
│   ├── sales/           MySalesPage, SaleDetailPage, ShipModal, MeetupModal,
│   │                    sales.api.js
│   ├── payouts/         PayoutsPage, BankAccountPage, payouts.api.js
│   ├── notifications/   NotificationsPage, BellDropdown, useLiveNotifications.js,
│   │                    notifications.api.js
│   ├── articles/        ArticlesPage, ArticleDetailPage, articles.api.js
│   ├── help/            HelpLayout, HelpPage, useGuideSearch.js (Fuse.js),
│   │                    guide-content.js (bundled from docs/user-guide at build)
│   └── admin/
│       ├── AdminLayout.jsx  DashboardPage.jsx
│       ├── users/  listings/  categories/  orders/  articles/
│       ├── refunds/     RefundQueuePage, RefundReviewModal
│       ├── payouts/     EligiblePage, PendingPayoutsPage
│       └── reconciliation/  ReconciliationPage
└── index.css            # tailwind + tokens from ui-context.md
```

## Rules this structure enforces
1. A spec maps to folders: spec 07 touches only `order/checkout`, `payment`, `features/checkout` — matching its Files Changed list.
2. Orchestrators are the only classes allowed to call more than two collaborators; collaborators never call each other sideways.
3. `entity/` and `repository/` never leak outside their feature; DTOs are the only cross-boundary shapes.
4. Frontend features import only `lib/`, `stores/`, `components/` — never each other.
5. Anything crossing 200 lines gets split along these seams, which already exist — so the limit never forces an awkward cut.
