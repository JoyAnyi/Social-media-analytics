# Internal Development Guide

This repository contains proprietary software. Contributions are limited to
authorized team members and approved contractors working under an applicable
confidentiality agreement.

## Development Principles

- Keep the modular monolith boundaries intact.
- Prefer Java, Spring Boot, React, TypeScript, and browser APIs before adding dependencies.
- Pin npm dependency versions exactly.
- Do not commit secrets, local `.env` files, build output, generated runtime data, database dumps, or production logs.
- Add or update tests for behavior changes.

## Local Checks

```bash
cd backend
mvn -Dmaven.repo.local=../.m2-cache test

cd ../frontend
npm ci --ignore-scripts
npm test
npm run build
npm audit
```

## Pull Requests

Each pull request should include:

- A clear summary of the change.
- Test evidence.
- Any dependency additions with purpose, maintenance notes, and security review.
- Notes for migrations, configuration changes, or operational impact.
- Confirmation that no proprietary secrets or customer data are included.

Protected branches should require CI checks and review from configured code
owners before merge.
