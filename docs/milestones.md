# Milestones

## Milestone 1: Foundation, Auth, and App Shell

Status: Complete

Scope:
- Spring Boot backend project with modular package boundaries.
- JWT registration, login, refresh, logout, profile update, and password change.
- User, role, refresh token, and audit log persistence.
- Flyway baseline migration for the authentication domain.
- React/Vite app shell with auth panel, session persistence, and dashboard surface.
- Local Docker Compose infrastructure for PostgreSQL, Redis, Kafka, and Elasticsearch.

Verification target:
- Backend tests: `mvn -Dmaven.repo.local=../.m2-cache test`
- Frontend tests: `npm test`
- Frontend build: `npm run build`

## Milestone 2: Feed Simulator and Kafka Processing

Status: Complete

Scope:
- Configurable post simulator.
- Raw post model and Kafka topic publication.
- Processing consumer with sentiment, keyword, hashtag, and mention extraction.
- Persistence for posts and enrichment outputs.

Verification target:
- Backend tests: `mvn -Dmaven.repo.local=../.m2-cache test`
- Frontend tests: `npm test`
- Frontend build: `npm run build`
- npm audit: `npm --cache ../.npm-cache audit --json`

Self-review outcome:
- Added inbound event validation for raw post processing.
- Added processed-post event publication so raw and processed event flow stay symmetrical.
- Hardened persistence against duplicate-key races during concurrent processing.

## Milestone 3: Real-Time Analytics and Dashboard Updates

Status: Complete

Scope:
- Aggregation services for sentiment, platform, keywords, hashtags, activity, and authors.
- WebSocket dashboard event publication.
- Frontend real-time dashboard updates.

Verification target:
- Backend tests: `mvn -Dmaven.repo.local=../.m2-cache test`
- Frontend tests: `npm test`
- Frontend build: `npm run build`
- npm audit: `npm --cache ../.npm-cache audit --json`

Self-review outcome:
- Replaced wildcard WebSocket origins with configurable allowed origin patterns.
- Added dashboard aggregation coverage to verify persisted posts drive summary metrics.
- Aligned default Kafka topic names with the Part 2 contract: `posts.raw` and `posts.processed`.

## Milestone 4: Search, Alerts, Notifications, and Reporting

Status: Pending

Scope:
- Elasticsearch indexing and filtered search.
- Alert rule evaluation and notification lifecycle.
- CSV and PDF reporting.

## Milestone 5: Administration, Audit, Hardening, and Release

Status: In progress

Scope:
- Admin user management.
- Audit log viewer.
- End-to-end Docker verification.
- Security, observability, and deployment readiness pass.

Part 3 hardening gate completed:
- Internal governance files: proprietary license, internal development guide, changelog, security policy, conduct policy, issue templates, PR template, and CI workflow.
- Expanded `.gitignore`, root `.env.example`, backend/frontend Dockerfiles, and full-stack Docker Compose configuration.
- Production profile with environment-driven settings and Flyway validation.
- Explicit CORS, configurable WebSocket origins, secure headers, strong password validation, and rate limiting for sensitive routes.
- Production readiness documentation covering security, consistency, reliability, and deployment notes.

Self-review outcome:
- Narrowed credential ignore patterns after detecting that a broad `*token*` rule hid refresh token source files.
- Returned rate-limit failures directly from the servlet filter as structured 429 API errors.
- Kept rate limiting dependency-free by using existing Spring filter infrastructure and Java concurrency primitives.

No-Docker local mode:
- Added a `standalone` profile backed by local H2 storage so the backend, Swagger, dashboard API, and WebSocket endpoint can run without Docker.
- Frontend defaults now target `http://localhost:8080`, which lets Vite or IDE browser previews interact with the local backend directly.
