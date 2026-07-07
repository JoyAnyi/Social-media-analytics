# Dependency Review

Last reviewed: 2026-07-07

Policy:
- Direct dependency versions are pinned where practical.
- Packages come from the official npm registry or Maven Central.
- Convenience libraries are avoided when Java, Spring Boot, React, TypeScript, or browser APIs are sufficient.
- Milestone-specific dependencies are deferred until code uses them.

## Direct npm Dependencies

| Dependency | Version | Purpose | Essential | Security / maintenance notes |
| --- | ---: | --- | --- | --- |
| `react` | `18.3.1` | Core UI framework required by the project stack. | Yes | Widely adopted and maintained by Meta/community. |
| `react-dom` | `18.3.1` | Browser rendering for React. | Yes | Same maintenance profile as React. |
| `chart.js` | `4.4.6` | Dashboard chart rendering for sentiment and later analytics visualizations. Native canvas implementation would be possible but would create custom charting risk and accessibility/maintenance burden. | Yes for analytics UI | Widely adopted charting package with active maintenance; use directly without wrapper to reduce dependency count. |

## Direct npm Dev Dependencies

| Dependency | Version | Purpose | Essential | Security / maintenance notes |
| --- | ---: | --- | --- | --- |
| `@vitejs/plugin-react` | `4.7.0` | Official React integration for Vite development and production build. | Yes | Official Vite ecosystem package; selected for Vite 7 peer compatibility. |
| `vite` | `7.3.6` | Frontend dev server and build tool. | Yes | Widely adopted; upgraded after audit showed Vite 5/6 dev-server advisories. Requires Node `^20.19.0 || >=22.12.0`; local Node is `22.16.0`. |
| `typescript` | `5.6.3` | Static typing and build verification. | Yes | Official TypeScript compiler. |
| `tailwindcss` | `3.4.15` | Utility CSS framework required by the project stack. | Yes | Widely adopted; compile-time dependency. |
| `postcss` | `8.5.16` | Tailwind CSS processing. | Yes | Required by Tailwind pipeline; pinned to patched release after audit. |
| `autoprefixer` | `10.4.20` | Vendor prefixing in CSS output. | Yes | Standard PostCSS plugin; build-time only. |
| `vitest` | `4.1.10` | Frontend unit test runner compatible with Vite. | Yes | Upgraded after audit flagged critical vulnerabilities in v2; peer range supports Vite 7. |
| `jsdom` | `25.0.1` | DOM environment for React component tests. | Yes | Test-only; required for browser-like rendering under Node. |
| `@testing-library/react` | `16.1.0` | User-oriented React component tests. | Yes | Widely adopted testing library. |
| `@testing-library/jest-dom` | `6.6.3` | Readable DOM assertions for tests. | Yes | Test-only; improves test correctness and clarity. |
| `@types/react` | `18.3.12` | Type definitions for React. | Yes | Type-only. |
| `@types/react-dom` | `18.3.1` | Type definitions for React DOM. | Yes | Type-only. |
| `eslint` | `9.39.4` | Static code analysis for frontend. | Yes | Development-only; pinned to patched v9 release after audit. |
| `eslint-plugin-react-hooks` | `5.0.0` | Enforces React hooks rules. | Yes | Official React rules package. |
| `eslint-plugin-react-refresh` | `0.4.14` | Validates Vite React refresh constraints. | Yes | Development-only. |

Removed or intentionally not added:
- `axios`: native `fetch` covers current HTTP needs.
- `@tanstack/react-query`: current state is simple enough for React state and explicit async handlers.
- `react-router-dom`: Milestone 1 uses a single app surface; routing will be added only when real multi-page navigation requires it.
- `lucide-react`: small inline SVG icons avoid adding an icon package for the current surface.
- `react-chartjs-2`: direct Chart.js usage avoids a wrapper dependency.

Known transitive notes:
- Vite pulls `esbuild` and Rollup platform packages. This is expected for the selected build tool; Vite is pinned to `7.3.6` to keep current patched dev-server behavior while staying compatible with local Node `22.16.0`.
- `jsdom` pulls `whatwg-encoding`, which npm marks deprecated. It is test-only and currently accepted because React component tests need a DOM implementation; revisit if a maintained alternative materially reduces risk.

Latest npm audit:
- `npm --cache ../.npm-cache audit --json` on 2026-07-07 reported 0 vulnerabilities.
- Dependency totals reported by npm audit: 8 prod, 383 dev, 65 optional, 21 peer, 390 total.

## Direct Maven Dependencies

Spring Boot dependency versions are controlled by the pinned `spring-boot-starter-parent` version `3.3.5`. Non-Boot managed dependencies are pinned explicitly.

| Dependency | Version source | Purpose | Essential | Security / maintenance notes |
| --- | --- | --- | --- | --- |
| `spring-boot-starter-parent` | `3.3.5` | Pins Spring Boot plugin and dependency management. | Yes | Official Spring release train. |
| `spring-boot-starter-actuator` | Boot BOM | Health and operational endpoints required by production readiness. | Yes | Official Spring Boot starter. |
| `spring-boot-starter-data-jpa` | Boot BOM | Persistence abstraction for normalized relational schema. | Yes | Official Spring Boot starter. |
| `spring-boot-starter-security` | Boot BOM | Authentication, authorization, password hashing, and request security. | Yes | Official Spring Security starter. |
| `spring-boot-starter-validation` | Boot BOM | Jakarta Bean Validation for request DTOs. | Yes | Official Spring Boot starter. |
| `spring-boot-starter-web` | Boot BOM | REST API runtime. | Yes | Official Spring Boot starter. |
| `spring-boot-starter-websocket` | Boot BOM, currently `3.3.5` | Authenticated live dashboard WebSocket endpoint for Milestone 3. Native browser WebSocket covers the frontend, but the backend needs first-party Spring WebSocket hosting. | Yes for live dashboard | Official Spring Boot starter. Added instead of npm/STOMP client libraries to keep the frontend dependency footprint unchanged. |
| `spring-kafka` | Boot BOM, currently `3.2.4` | Kafka producer and listener integration for raw social post events in Milestone 2. | Yes for ingestion pipeline | Official Spring project. Added only when Kafka publication/listening code was implemented; avoids custom producer/listener infrastructure. |
| `flyway-core` | Boot BOM | Versioned database migrations. | Yes | Widely adopted migration tool. |
| `flyway-database-postgresql` | Boot BOM | PostgreSQL-specific Flyway support. | Yes | Required with current Flyway/PostgreSQL combination. |
| `springdoc-openapi-starter-webmvc-ui` | `2.6.0` | OpenAPI and Swagger UI required by project brief. | Yes | Widely adopted Spring OpenAPI integration; external to Spring, monitor compatibility. |
| `jjwt-api` | `0.12.6` | JWT creation and validation API. | Yes | Avoids fragile hand-rolled token parsing/signing. |
| `jjwt-impl` | `0.12.6` | Runtime JWT implementation. | Yes | Runtime-only; paired with `jjwt-api`. |
| `jjwt-jackson` | `0.12.6` | JSON serialization support for JWT claims. | Yes | Runtime-only; paired with `jjwt-api`. |
| `postgresql` | Boot BOM | PostgreSQL JDBC driver. | Yes | Official PostgreSQL JDBC driver. |
| `h2` | Boot BOM, test scope | In-memory database for fast Milestone 1 integration tests. | Yes for tests | Test-only; production uses PostgreSQL. |
| `spring-boot-starter-test` | Boot BOM, test scope | JUnit, assertions, and Spring test support. | Yes for tests | Official Spring Boot testing starter. |
| `spring-security-test` | Boot BOM, test scope | Security-aware integration testing. | Yes for tests | Official Spring Security test support. |

Deferred until their milestone code requires them:
- Kafka test support.
- Spring Data Redis.
- Spring Data Elasticsearch.
- Testcontainers.
- MapStruct and Lombok. Current code uses explicit Java classes/mappers to avoid code generation libraries without a stronger architectural need.

Milestone 2 Maven transitive review:
- `spring-kafka:3.2.4` resolves to `spring-messaging:6.1.14`, `spring-retry:2.0.10`, and `kafka-clients:3.7.1` in the compile dependency tree.
- No Kafka test dependency was added; tests use the internal `PostEventPublisher` abstraction and disable Kafka through `app.kafka.enabled=false`.
- Compression/native artifacts can be introduced by Kafka clients at runtime; keep Kafka upgrades on the Spring Boot BOM path unless a security advisory requires a targeted version override.

Milestone 2 review refinements:
- The processing pipeline now validates `RawPostEvent` inputs with Jakarta Validation before persistence.
- `processed-post-topic` is now part of the code path through a dedicated processed-event publisher, which keeps the event model aligned for the upcoming live analytics/dashboard milestone.
- The persistence layer now tolerates duplicate insert races for posts, hashtags, and keywords by refetching after unique-constraint conflicts instead of failing the whole processing path.

Milestone 3 review refinements:
- Spring WebSocket resolves to Spring Framework WebSocket support managed by the pinned Spring Boot BOM.
- The frontend uses native browser `WebSocket`, so no npm dependency was added for live dashboard transport.
- WebSocket origins are configuration-driven through `WEBSOCKET_ALLOWED_ORIGIN_PATTERNS`; wildcard origins are not used by default.

Part 3 production readiness review:
- No new npm or Maven dependencies were added.
- Rate limiting uses the existing Spring Security filter chain plus Java `ConcurrentHashMap` counters; no bucket/rate-limit library was introduced.
- Strong password validation uses Jakarta Bean Validation already present through `spring-boot-starter-validation`.
- Secure headers, CORS, CSRF behavior, and endpoint authorization use Spring Security already present in the project.
- Docker image choices are official or vendor-published images: Maven, Eclipse Temurin, Node, nginx, PostgreSQL, Redis, Bitnami Kafka, and Elastic Elasticsearch. Image tags are pinned to concrete versions where practical, but production deployments should evaluate digest pinning as a release-management step.
