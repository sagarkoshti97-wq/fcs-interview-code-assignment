# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would standardize the persistence architecture across the codebase rather than mixing three different patterns:
1. Active Record Pattern (`Store` extends `PanacheEntity` with direct static calls like `Store.findById()` inside `StoreResource`).
2. Repository Pattern coupled to REST (`ProductResource` injects `ProductRepository` and manipulates JPA `Product` entities directly).
3. Hexagonal / Ports-and-Adapters Pattern (`warehouses` module separates domain models `Warehouse` from persistence entities `DbWarehouse` via the `WarehouseStore` port and `WarehouseRepository` adapter).

Refactoring Strategy:
- For simple CRUD modules (`Product` and `Store`), I would at least unify them under the Repository Pattern (`PanacheRepository`) with a dedicated service/application layer so HTTP resources (`StoreResource`, `ProductResource`) do not manage transactions, persistence state, and domain events directly.
- For core domain modules with rich business rules (`Warehouse` and `Fulfilment`), the Hexagonal (Ports & Adapters) pattern used in `warehouses` is the best choice because it decouples domain use cases (`CreateWarehouseUseCase`, `ReplaceWarehouseUseCase`, `ArchiveWarehouseUseCase`) from Hibernate/JPA, allowing fast, isolated unit testing with in-memory fakes/mocks without spinning up a database.
- Additionally, both `ProductResource` and `StoreResource` define duplicate `@Provider public static class ErrorMapper implements ExceptionMapper<Exception>` classes. I would extract a single global `GlobalExceptionMapper` into a shared `common` or `infrastructure` package to avoid JAX-RS provider ambiguity.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
1. Contract-First (OpenAPI Generator - used in `Warehouse`):
   - Pros: Guarantees the API specification (`warehouse-openapi.yaml`) is always the single source of truth; enables parallel frontend/backend/consumer development via mock servers; enforces strict schema validation and consistent DTO generation (`com.warehouse.api.beans.Warehouse`) decoupled from internal database entities.
   - Cons: Requires build-time code generation (`quarkus-openapi-generator-server`), IDE source-folder synchronization, and manual mapping between generated API beans and internal domain models.

2. Code-First (Direct JAX-RS annotations - used in `Product` and `Store`):
   - Pros: Fast to prototype and iterate; minimal boilerplate; developers work directly with familiar Jakarta REST (`@Path`, `@GET`, `@POST`) annotations.
   - Cons: High risk of accidental breaking API changes when refactoring internal JPA entities (`Store`, `Product`) that are exposed directly as JSON payloads; documentation can drift unless SmallRye OpenAPI annotations are meticulously maintained.

My Choice:
For enterprise and external/partner-facing APIs (or multi-team microservice environments), I prefer the Contract-First (OpenAPI) approach used in `Warehouse` because API stability and clear contracts outweigh the minor mapping overhead. For purely internal CRUD endpoints, Code-First with dedicated DTOs (never exposing JPA entities directly) and `quarkus-smallrye-openapi` to auto-generate the spec is a pragmatic alternative.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would follow the Testing Pyramid to maximize confidence while keeping build times fast:

1. Fast Domain Unit Tests (Highest Priority - ~60-70% of tests):
   - Focus on core business rules and edge cases in Use Cases (`CreateWarehouseUseCaseTest`, `ReplaceWarehouseUseCaseTest`, `ArchiveWarehouseUseCaseTest`, `LocationGatewayTest`) using lightweight in-memory implementations of ports (`WarehouseStore`, `LocationResolver`).
   - These run in milliseconds without Docker or Quarkus boot overhead and validate all boundary conditions (duplicate business unit codes, location capacity limits, stock matching on replacement, max warehouses per location).

2. Component / API Integration Tests (`@QuarkusTest` - ~20-30% of tests):
   - Test the full vertical slice from REST endpoints (`WarehouseResourceImpl`, `StoreResource`, `ProductResource`, `FulfilmentResource`) through CDI transactional events (`AFTER_SUCCESS` legacy gateway sync) to the PostgreSQL database via RestAssured.
   - Verify HTTP status codes (200, 201, 204, 400, 404, 422), serialization, and database persistence constraints.

3. Black-Box Packaged Integration Tests (`@QuarkusIntegrationTest` - ~5-10% of tests):
   - Keep a focused smoke suite (`WarehouseEndpointIT`) to verify the packaged JAR/native artifact starts and serves traffic properly.

Ensuring Effective Coverage Over Time:
- Enforce an automated JaCoCo quality gate (`jacoco-maven-plugin` + `quarkus-jacoco`) in the Maven `verify` phase and GitHub Actions CI pipeline requiring >80% instruction coverage on every Pull Request.
- Combine coverage thresholds with meaningful assertion reviews (or mutation testing like PIT) so tests verify business invariants rather than just executing lines of code.
```