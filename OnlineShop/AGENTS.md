# Repository Guidelines
## Project Structure & Module Organization
OnlineShop is a Spring Boot monolith. Core code lives in `src/main/java/com/example/onlineshop`, layered by responsibility: `Controller` exposes REST entry points, `Service` holds business logic, `Repository` wraps Spring Data JPA, `Entity` contains persistence models, `dto` carries request/response payloads, and `Util` hosts shared helpers. Security, OAuth, and JWT components sit in `config`. Resource files under `src/main/resources` include Flyway migrations inside `db/migration`, Thymeleaf templates in `templates`, static assets, and environment defaults in `application.properties`. Tests mirror the package structure in `src/test/java/com/example/onlineshop`.

## Build, Test, and Development Commands
Run `.\mvnw spring-boot:run` for a local server with devtools reload. `.\mvnw clean verify` performs a full build, runs unit tests, and compiles generated docs. Use `.\mvnw test` for the fast JUnit suite; add `-Dspring.profiles.active=test` when you supply alternate configuration. Flyway migrations execute at startup, or trigger them manually with `.\mvnw flyway:migrate`.

## Coding Style & Naming Conventions
Target Java 17 with four-space indentation and braces on the same line. Name classes in PascalCase, methods and fields in camelCase, and JSON payload DTOs with concise, noun based names. Follow the existing uppercase package folders (`Controller`, `Service`, etc.) to keep imports aligned. Prefer Lombok annotations (`@RequiredArgsConstructor`, `@Builder`) already used in the codebase and keep controllers thin; delegate to services and map DTOs explicitly.

## Testing Guidelines
The project uses Spring Boot Starter Test (JUnit 5 and Mockito). Place new tests under `src/test/java/com/example/onlineshop`, matching the package of the class under test and ending with `*Test`. Favor slice tests (`@WebMvcTest`, `@DataJpaTest`) for focused coverage, reserving `@SpringBootTest` when the full context is required. Reuse the `PasswordEncoderTest` style: arrange fixtures, execute the call, and assert both status and side effects. Add tests for validation, security filters, and migrations that touch data.

## Commit & Pull Request Guidelines
Because the shared git history is unavailable here, default to Conventional Commits (for example, `feat: add order cancellation workflow`) with imperative, <=72 character subjects and optional body bullets for details. Reference issue IDs when applicable. Pull requests should summarize the change, call out new endpoints or Flyway scripts, list manual test steps, and attach screenshots or sample curl commands for user facing API updates.

## Security & Configuration Tips
Do not commit real secrets; the checked in `application.properties` is for local samples only. Override credentials through environment variables such as `SPRING_DATASOURCE_PASSWORD` or a profile specific `application-local.properties` ignored by git. Keep JWT secrets at least 64 characters, rotate OAuth client settings per environment, and validate that Flyway migrations carry idempotent roll forward logic before merging.
