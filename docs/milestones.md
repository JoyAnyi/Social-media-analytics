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

Scope:
- Elasticsearch indexing and filtered search.
- Alert rule evaluation and notification lifecycle.
- CSV and PDF reporting.

## Milestone 5: Administration, Audit, Hardening, and Release

Scope:
- Admin user management.
- Audit log viewer.
- End-to-end Docker verification.
- Security, observability, and deployment readiness pass.
