# ADR-0003: React SPA + REST API over Thymeleaf/Vue

## Context
Frontend approach for the Spring rebuild. Developer already ships React (Laravel+Inertia, BuzzyHive); Spring is the new skill being learned.

## Decision
React 18 + Vite SPA consuming `/api/v1`, served by Nginx same-origin with the API. Tailwind + shadcn/ui, Zustand + TanStack Query.

## Consequences
+ Only one new technology at a time; backend becomes a clean REST API — the most transferable Spring skill set.
+ Reuses existing UI standards (tokens, modal principles) from prior projects.
− Two build pipelines (Maven + Vite) and an API contract to maintain — mitigated by springdoc-openapi and the error envelope.
− SEO limited for public catalog pages; acceptable for a campus marketplace, revisit with SSR only if organic search ever matters.
