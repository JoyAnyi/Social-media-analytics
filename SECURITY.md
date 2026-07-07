# Security Policy

This is proprietary software. Source code, architecture, credentials, tokens,
customer data, operational logs, and vulnerability details are confidential.

## Supported Versions

The project is pre-1.0. Security fixes are applied to the active internal
development line.

## Reporting a Vulnerability

Report suspected vulnerabilities privately to the authorized maintainers. Do
not create public issues, public pull requests, or public discussions containing
exploit details, secrets, tokens, personal data, infrastructure identifiers, or
customer information.

Include:

- Affected component.
- Steps to reproduce.
- Expected and observed behavior.
- Impact assessment.
- Suggested mitigation, if known.

## Dependency Policy

- Dependencies must come from trusted official registries.
- npm versions are pinned exactly.
- Maven dependencies must be justified and preferably managed by the Spring Boot BOM.
- Avoid packages for functionality already provided by Java, Spring Boot, React, TypeScript, or browser APIs.
- Run `npm audit` and Maven tests before release.

## Confidentiality Rules

- Never commit real `.env` files, passwords, JWT secrets, API keys, database dumps, private certificates, or production logs.
- Keep repositories private and restrict access to authorized personnel only.
- Rotate any credential that may have been exposed in commits, logs, screenshots, terminals, CI output, tickets, or chat.
- Do not publish Docker images, generated artifacts, screenshots, or API documentation that reveal proprietary workflows unless approved.
- Keep production Swagger/OpenAPI disabled unless access is protected.
- Require code owner review and branch protection before merging to protected branches.
- Keep GitHub Actions permissions to the minimum needed for each workflow. 
