## ADDED Requirements

### Requirement: Legacy post feed endpoints SHALL remain unchanged
The existing post feed endpoints SHALL continue returning `List<Post>` and SHALL keep their current request shapes unchanged in this change.

#### Scenario: General posts endpoint stays legacy
- **WHEN** a client calls the existing general posts endpoint
- **THEN** it receives the same legacy response shape based on `List<Post>`

#### Scenario: Existing feed endpoints stay legacy
- **WHEN** a client calls existing user, friends, or regional feed endpoints
- **THEN** it receives the same legacy response shape and parameters as before this change

### Requirement: The system SHALL expose paginated post feed endpoints in parallel
The system SHALL provide new paginated endpoints for post feeds without replacing the existing endpoints.

#### Scenario: New paginated endpoints exist separately
- **WHEN** a client inspects the posts API contract after this change
- **THEN** it can identify new paginated endpoints that are distinct from the legacy ones

#### Scenario: Legacy and paginated endpoints coexist
- **WHEN** a client uses the posts API after this change
- **THEN** it can choose between legacy list endpoints and new paginated endpoints

### Requirement: Paginated post feed responses SHALL be explicit
Each paginated post feed endpoint SHALL return an explicit paginated response with items and pagination metadata rather than a bare `List<Post>`.

#### Scenario: Paginated response contains items and metadata
- **WHEN** a client calls a paginated posts endpoint
- **THEN** the response contains the page items and pagination metadata in an explicit response object

#### Scenario: Paginated response differs from legacy shape
- **WHEN** a client compares a paginated endpoint response with a legacy endpoint response
- **THEN** it can verify that only the paginated endpoint returns explicit pagination metadata

### Requirement: Paginated post feeds SHALL use explicit ordering
Paginated post feed endpoints SHALL use explicit ordering and SHALL default to creation date descending unless another supported sort is requested.

#### Scenario: Default order is descending by creation date
- **WHEN** a client calls a paginated feed without an explicit sort parameter
- **THEN** posts are returned in descending creation order

#### Scenario: Pagination is stable across pages
- **WHEN** a client requests consecutive pages from the same paginated feed
- **THEN** the ordering rule is explicit and consistent across those pages

### Requirement: Paginated regional feed SHALL preserve the current cross-store flow
The paginated regional feed SHALL preserve the current behavior of resolving regional user IDs through PostgreSQL/PostGIS before querying posts in MongoDB.

#### Scenario: Regional user selection still happens first
- **WHEN** a client calls the paginated regional feed
- **THEN** the backend resolves the eligible regional user IDs before loading posts

#### Scenario: Regional pagination does not redefine business logic
- **WHEN** a client compares regional feed eligibility before and after this change
- **THEN** it can verify that pagination changes the delivery shape but not the underlying regional selection logic

### Requirement: Pagination parameters SHALL be validated minimally
Paginated post feed endpoints SHALL accept pagination parameters such as `page` and `size` and SHALL reject invalid values according to the project's current validation style.

#### Scenario: Valid page request succeeds
- **WHEN** a client sends valid pagination parameters
- **THEN** the paginated endpoint returns a successful paginated response

#### Scenario: Invalid pagination parameters are rejected
- **WHEN** a client sends invalid pagination parameters such as negative page or non-positive size
- **THEN** the endpoint rejects the request according to the project's current error handling approach
