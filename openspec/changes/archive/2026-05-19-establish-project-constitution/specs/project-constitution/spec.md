## ADDED Requirements

### Requirement: Project constitution documents SHALL define the repository scope
The project constitution SHALL state that this repository contains the IFConnected Spring Boot backend and SHALL state that the frontend mentioned in the README is not present in this repository snapshot.

#### Scenario: Backend-only scope is documented
- **WHEN** a reader opens the constitution documents
- **THEN** they can verify that the writable scope is the backend Spring Boot repository only

#### Scenario: Missing frontend is documented
- **WHEN** a reader compares the constitution with the README narrative
- **THEN** they can verify that the Next.js frontend is referenced as absent from this repository

### Requirement: Project constitution SHALL record the stable architectural center
The project constitution SHALL document polyglot persistence as a central architectural decision and SHALL identify PostgreSQL/PostGIS, MongoDB, Redis, and MinIO as distinct storage responsibilities in the backend.

#### Scenario: Polyglot persistence is explicit
- **WHEN** a reader opens the architecture or stack documentation
- **THEN** they can verify that polyglot persistence is described as a core architectural decision

#### Scenario: Storage roles are named
- **WHEN** a reader reviews the constitution documents
- **THEN** they can verify the named roles of PostgreSQL/PostGIS, MongoDB, Redis, and MinIO in the backend

### Requirement: Project constitution SHALL define the brownfield documentation stance
The project constitution SHALL state that the current code is the operational source of truth and SHALL avoid reconstructing complete functional specs for all existing modules.

#### Scenario: Operational source of truth is documented
- **WHEN** a reader opens the constitution documents
- **THEN** they can verify that current code behavior is treated as authoritative for future changes

#### Scenario: Constitution stays minimal
- **WHEN** a reviewer inspects the created constitution documents
- **THEN** they can verify that the documents describe stable context rather than exhaustive specifications of all current features

### Requirement: OpenSpec workflow SHALL require exploration before implementation
The project constitution and OpenSpec configuration SHALL instruct contributors to start future changes with `/opsx:explore` before creating specs or implementation changes.

#### Scenario: Explore-first workflow is documented
- **WHEN** a contributor reads `CLAUDE.md`, conventions, or OpenSpec configuration
- **THEN** they can verify that `/opsx:explore` is the required starting point for future changes

#### Scenario: OpenSpec context reflects workflow
- **WHEN** a contributor reads `openspec/config.yaml`
- **THEN** they can verify that the brownfield workflow requires exploration before implementation

### Requirement: Constitution SHALL define the minimal governance document set
The change SHALL define the minimal governance document set as `mission.md`, `tech-stack.md`, `roadmap.md`, `CLAUDE.md`, `docs/domain.md`, `docs/architecture.md`, `docs/conventions.md`, and `openspec/config.yaml`.

#### Scenario: Required documents are listed
- **WHEN** a reviewer reads the project constitution spec and related design
- **THEN** they can verify the complete list of required governance documents

#### Scenario: Documents are verifiable outputs
- **WHEN** this change is implemented
- **THEN** each required governance document exists at the expected path
