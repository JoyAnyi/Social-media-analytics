# Security Policy

## Supported Versions

The project is pre-1.0. Security fixes are applied to the main development line.

## Reporting a Vulnerability

Please report suspected vulnerabilities privately to the project maintainers. Do not create a public issue containing exploit details, secrets, tokens, or personal data.

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
