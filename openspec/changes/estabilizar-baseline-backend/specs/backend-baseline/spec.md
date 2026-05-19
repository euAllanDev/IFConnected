## ADDED Requirements

### Requirement: Backend baseline documentation SHALL describe the real local execution flow
The repository documentation SHALL describe a backend-only local execution flow for this repository and SHALL state that `docker compose up -d` is the recommended step before running full tests that depend on infrastructure services.

#### Scenario: Local execution flow is documented
- **WHEN** a contributor reads the README
- **THEN** they can verify the commands to start infrastructure and run the backend locally for this repository

#### Scenario: Full-test order is documented
- **WHEN** a contributor reads the baseline documentation
- **THEN** they can verify that `docker compose up -d` is recommended before running full tests

### Requirement: Backend baseline SHALL distinguish checks with and without external services
The repository documentation SHALL state that `./mvnw.cmd -DskipTests package` works without external services and SHALL state that `./mvnw.cmd test` depends on PostgreSQL and Liquibase being available during bootstrap.

#### Scenario: Offline-friendly package check is documented
- **WHEN** a contributor reviews the documented checks
- **THEN** they can verify that `./mvnw.cmd -DskipTests package` is the minimal package command that works without external services

#### Scenario: Infrastructure-dependent test check is documented
- **WHEN** a contributor reviews the documented checks
- **THEN** they can verify that `./mvnw.cmd test` depends on PostgreSQL and Liquibase availability

### Requirement: Backend baseline SHALL provide environment variable guidance
The repository SHALL include a `.env.example` file that lists the variables required to run the backend and its local infrastructure without embedding user-specific secrets.

#### Scenario: Environment example exists
- **WHEN** a contributor inspects the repository root
- **THEN** they can verify that `.env.example` exists

#### Scenario: Environment example is safe to share
- **WHEN** a contributor reads `.env.example`
- **THEN** they can verify that it contains example values or placeholders rather than personal secrets

### Requirement: Backend baseline SHALL reduce repository noise from local artifacts
The repository ignore rules SHALL cover predictable local build artifacts, JVM crash logs, temporary files, and other local-only artifacts that do not belong in version control.

#### Scenario: Ignore rules cover build output
- **WHEN** a contributor reads `.gitignore`
- **THEN** they can verify that local build output and temporary artifacts are ignored

#### Scenario: Ignore rules cover JVM crash logs
- **WHEN** a contributor reads `.gitignore`
- **THEN** they can verify that JVM crash logs and replay logs are ignored

### Requirement: Architecture and conventions SHALL record the current bootstrap constraints
The repository documentation SHALL state that PostgreSQL and Liquibase are part of the current application bootstrap path and SHALL record the current minimal checks in `docs/conventions.md`.

#### Scenario: Bootstrap constraint is documented
- **WHEN** a contributor reads `docs/architecture.md`
- **THEN** they can verify that PostgreSQL and Liquibase are described as part of the current bootstrap path

#### Scenario: Minimal checks are documented
- **WHEN** a contributor reads `docs/conventions.md`
- **THEN** they can verify the current minimal checks and their constraints
