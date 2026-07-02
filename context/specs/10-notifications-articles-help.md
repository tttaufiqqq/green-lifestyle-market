# Spec 10 — Notifications, Articles, User Guide

## Goal
FR-N1..N4: real-time + push notification delivery for the outbox rows earlier specs already write; article CMS + public reading; in-app /help guide.

## Design
Per docs/notifications.md: NotificationPublisher (post-commit) fans out STOMP (/user/queue/notifications) and Web Push (VAPID via webpush-java; 410 → delete subscription). WebSocketConfig with session-authenticated handshake. ArticleService: markdown stored as-is (CLOB), rendered client-side (react-markdown + remark-gfm), Oracle Text search optional on list. Help: docs/user-guide/*.md bundled into the SPA at build time, Fuse.js index, /help route with sidebar IA.

## Implementation
1. Wire publisher into the transition/notification points already writing rows (specs 07–09) — delivery only, no new triggers.
2. Notification endpoints (list, read, read-all) + push subscription register/remove; permission prompt only from profile toggle (never on load).
3. Bell dropdown + notifications page; live badge via WS.
4. Admin article CRUD + publish; public /articles list + detail per articles-help.md.
5. /help per articles-help.md and docs/user-guide/README.md IA, "Was this helpful?" stub logging.

## Dependencies
Spec 09 (final integration spec).

## Pages
Notifications, Articles list/detail, Admin Articles, Help.

## DB objects
notifications, push_subscriptions, articles.

## API endpoints
"Notifications & content" + admin articles.

## Files Changed
backend com.glm.notification/**, com.glm.article/**, WebSocketConfig; frontend features/notifications/**, features/articles/**, features/help/**, lib/ws.js, lib/push.js

## Verify
- [ ] Order state change reaches an open second browser session via WS within 2 s
- [ ] Web Push arrives with tab closed; dead subscription auto-pruned on 410
- [ ] Notification row commits atomically with the domain change (kill test: publisher down ≠ lost row)
- [ ] Unauthenticated WS handshake rejected
- [ ] /help search finds "refund" pages; deep anchors work
