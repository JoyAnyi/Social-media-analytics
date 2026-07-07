# Real-Time Social Media Analytics Engine

Production-oriented modular monolith for real-time social media ingestion, analytics, search, alerting, and reporting.

## Current Milestone

Milestones 1 through 3 are complete:
- Java 21 / Spring Boot backend with authentication, users, refresh tokens, audit logs, feed simulation, and post enrichment persistence.
- Kafka publication and listener boundaries for raw post processing, with Kafka disabled in the test profile.
- Live dashboard summary API and authenticated WebSocket updates.
- React / TypeScript / Vite frontend with an auth flow and dashboard shell.
- Docker Compose services for PostgreSQL, Kafka, Redis, and Elasticsearch.
- Flyway database migrations and focused backend/frontend test coverage.

The milestone plan is tracked in [docs/milestones.md](docs/milestones.md).

## Local Infrastructure

```bash
docker compose -f docker/docker-compose.yml up -d
```

## Backend

```bash
cd backend
mvn -Dmaven.repo.local=../.m2-cache test
mvn spring-boot:run
```

Backend API documentation is available at `http://localhost:8080/swagger-ui.html` after the backend starts.

## Frontend

```bash
cd frontend
npm ci --ignore-scripts
npm test
npm run build
npm run dev
```

The Vite app runs at `http://localhost:5173` and proxies `/api` plus `/ws` requests to the backend.

## Configuration

Copy the example environment files when running locally:

```bash
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
```

Important backend variables:
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`
- `APP_KAFKA_ENABLED`
- `RAW_POST_TOPIC`
- `PROCESSED_POST_TOPIC`
- `WEBSOCKET_ALLOWED_ORIGIN_PATTERNS`
- `REDIS_HOST`
- `ELASTICSEARCH_URIS`
- `JWT_SECRET`

## Architecture

The system is a modular monolith. Modules live under `backend/src/main/java/com/company/socialanalytics`:
- `auth`
- `user`
- `feed`
- `post`
- `processing`
- `kafka`
- `dashboard`
- `notification`
- `report`
- `search`
- `audit`
- `admin`
- `security`
- `config`
- `common`

Entities are not exposed directly from controllers. API boundaries use DTOs and services, with validation and centralized exception handling.
