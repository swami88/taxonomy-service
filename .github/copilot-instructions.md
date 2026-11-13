# Taxonomy Service Copilot Instructions

## Toolchain and commands

- Use Java 21 and the Maven Wrapper. On Windows, run `.\mvnw.cmd`; on Unix-like systems, use `./mvnw`.
- Build and run all tests: `.\mvnw.cmd clean verify`
- Run the test suite: `.\mvnw.cmd test`
- Run one test class: `.\mvnw.cmd -Dtest=TaxonomyServiceApplicationTests test`
- Run one test method: `.\mvnw.cmd -Dtest=TaxonomyServiceApplicationTests#contextLoads test`
- Start the application locally: `.\mvnw.cmd spring-boot:run`

No separate linting plugin or command is configured.

## Architecture

- This is a Spring Boot 4.0.9 application (`com.rba.taxonomy.taxonomy_service.TaxonomyServiceApplication`) using Java 21.
- The application class is the Spring Boot entry point and component-scan root. Keep application components beneath `com.rba.taxonomy.taxonomy_service` so Spring discovers them automatically.
- The service is set up for servlet-based HTTP endpoints through Spring Web MVC and persistence through Spring Data JPA. SQLite JDBC is a runtime dependency; database and JPA settings belong in `src/main/resources/application.properties`.
- Test sources mirror the production package under `src/test/java`. The existing `@SpringBootTest` context-load test confirms application wiring.

## Repository conventions

- Preserve the Maven Wrapper as the project command entry point rather than assuming a system Maven installation.
- The POM intentionally contains empty `license`, `developers`, and `scm` elements to suppress values inherited from the Spring Boot parent; retain those overrides unless changing the parent-inheritance behavior intentionally.

## Tech Stack
- Java 21, Spring Boot 4.0.9, Maven
- SQLite JDBC
- MapStruct for DTO mapping, Lombok for boilerplate reduction

## Architecture Rules
- Layered architecture: Controller → Service → Repository
- Controllers must never access repositories directly
- All business logic lives in the service layer
- Use constructor injection everywhere, never field injection
- Every public service method must be covered by a unit test

## Coding Conventions
- Use UUID for all entity primary keys
- Use BigDecimal for monetary values, never double or float
- All REST endpoints must be versioned under /api/v1/
- DTOs are records where possible, otherwise use Lombok @Builder
- Return ResponseEntity from controllers, not raw objects
- Use AssertJ assertions in tests, not JUnit assertEquals

## Error Handling
- Throw custom exceptions (EntityNotFoundException, etc.)
- Never return null from service methods — use Optional or throw
- All exceptions are handled centrally via @RestControllerAdvice

## Things to Avoid
- No System.out.println — use SLF4J logger
- No wildcard imports
- No magic strings — use constants or enums
- No business logic in controllers or entities (except state machine on Order)
