# Contributing

Thank you for helping improve the Real-Time Social Media Analytics Engine.

## Development Principles

- Keep the modular monolith boundaries intact.
- Prefer Java, Spring Boot, React, TypeScript, and browser APIs before adding dependencies.
- Pin npm dependency versions exactly.
- Do not commit secrets, local `.env` files, build output, or generated runtime data.
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
