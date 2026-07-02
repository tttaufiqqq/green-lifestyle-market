# ADR-0001: Modular Monolith on Spring Boot 3

## Context
Rebuild of the GLM workshop project; primary goals are learning Spring Boot properly and shipping a working marketplace at campus scale (hundreds of users). Team of one.

## Decision
One Spring Boot 3 (Java 21) application, package-by-feature, single Oracle DB, REST + STOMP. No microservices, queues, or Redis in v1. Scheduled jobs via @Scheduled inside the app.

## Consequences
+ One deployable, one DB transaction boundary — payment/stock atomicity is trivial compared to distributed alternatives.
+ Fastest path to learning core Spring (Security, Data JPA, Validation, Scheduling, WebSocket) without infra yak-shaving.
− Vertical scaling only; scheduled jobs assume a single instance (acceptable; revisit with a lock table like ShedLock if ever multi-instance).
