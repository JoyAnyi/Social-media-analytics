# Production Readiness Notes

## Security

- Passwords are hashed with BCrypt through Spring Security.
- Access tokens are JWTs; refresh tokens are stored server-side and rotated.
- Rate limiting covers login, registration, password reset, and report generation routes.
- CORS and WebSocket origins are explicit environment-driven allowlists.
- Security headers include content security policy, frame denial, content type protection, HSTS, referrer policy, and permissions policy.
- Secrets are provided through environment variables and ignored by Git.

## Reliability

- Docker Compose defines health checks, restart policies, named volumes, and an isolated network.
- Kafka processing uses stable topic names and idempotent duplicate handling around raw event identifiers.
- Post persistence and realtime broadcasting are split so frontend events are published after successful persistence.

## Consistency

- PostgreSQL remains the source of truth for users, posts, notifications, audit events, and reports.
- Kafka is the transport for event flow. Consumers must treat messages as at-least-once and potentially duplicated.
- Redis is a cache only; cached views can be rebuilt from PostgreSQL and analytics calculations.
- Elasticsearch is a query index only; PostgreSQL remains authoritative.

## Dependency Review

No npm or Maven dependency was added for the production readiness foundation. Rate limiting, validation, secure headers, CORS, Docker configuration, and documentation use existing Spring Boot, Java, React, TypeScript, and browser/runtime capabilities.
