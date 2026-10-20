# Kotlin & Ktor

## Source of Truth

- Check the official Ktor documentation (ktor.io/docs) and Kotlin docs (kotlinlang.org/docs) before applying a pattern. Older Ktor 1.x idioms are NOT valid here — this is Ktor 3.
- If a rule here conflicts with the official docs, the docs win — say so and update this file.

## Structure — modular architecture

Ktor's unit of composition is the module: `fun Application.xModule()`. One module per domain, declared in
`application.yaml` under `ktor.application.modules` — never called from `main.kt`, which stays a one-liner
delegating to `EngineMain`.

- Package root `site.cdc`. Technical wiring lives in `site.cdc.config` (`Database.kt`, `Serialization.kt`),
  one file per concern named after what it declares.
- A domain (account, transaction…) is one package `site.cdc.<domain>` owning its routes, its DTOs and
  its Exposed tables, exposing exactly one `fun Application.<domain>Module()` as its entry point. Nothing
  outside the package reaches into it; cross-domain needs go through a service resolved from Ktor DI.
- Layers inside a domain, one file each, dependencies pointing down only:
  - `<Domain>Routes.kt` — `fun Route.<domain>Routes(service)`: decode the request, call the service, map to
    a response DTO. No business rule, no Exposed call.
  - `<Domain>Dto.kt` — `@Serializable` request/response DTOs and their mapping from the domain model.
  - `<Domain>Service.kt` — business rules, orchestration; throws `ValidationException` for invalid input.
  - `<Domain>Repository.kt` — the Exposed table and every query; returns domain models, owns transactions.
  - `<Domain>.kt` — domain models, plain data classes with no framework annotation.
- Wiring is Ktor's DI plugin (`ktor-server-di`): the module `provide`s its repository and service with
  `provide { X(resolve()) }` and resolves the service for its routes. Constructor injection only, concrete
  classes — no interface until a second implementation exists.
- HTTP error mapping lives once in `config/ErrorHandling.kt` (StatusPages). Routes and services throw,
  they never build an error response themselves.
- A module installs only the plugins it needs itself; a plugin shared by all of them belongs in `config`.
- One Gradle module. Split into several (`:server:banking`, `:db:postgres`) only when a part ships
  separately — the package boundary already gives the isolation.

## Kotlin style

- `val` over `var`; data classes for DTOs; no nullable field that a sensible default covers.
- Suspend functions all the way down — never block inside a handler (`runBlocking`, `Thread.sleep`, blocking JDBC on the request thread).
- Prefer stdlib (`require`, `let`, `runCatching`, collection operators) over hand-rolled helpers.
- No `!!`. Validate request input explicitly and respond `HttpStatusCode.BadRequest` — a trust boundary is never simplified away.

## Dependencies

- Versions go in `gradle/libs.versions.toml` and are referenced via `libs.`/`ktorLibs.` aliases. Never hardcode a version in `build.gradle.kts`.
- `ktorLibs` is the Ktor version catalog shipped by the Ktor plugin — use it for every `io.ktor:*` artifact.
- Add a Ktor plugin only when a feature actually needs it (ContentNegotiation, CORS, Auth…), with the matching `install()`.

## Tests

- `kotlin.test` + `testApplication` from `ktor-server-test-host`. Do NOT add JUnit 5 assertions or MockK.
- Anything touching the database goes through the Testcontainers Postgres of `TestDatabase.kt` via
  `configureTestApp()` — never the dev database. `./gradlew test` therefore needs Docker running.
- Backtick test names describing behaviour, as in `ServerTest.kt`.
- Every new route gets one `testApplication` check asserting its status and payload.

## Database

- A schema change is a new `src/main/resources/db/migration/V<n>__description.sql`. Never edit a committed
  migration: Flyway checksums them and will refuse to start.
- Flyway runs from `configureDatabase()` at boot, against `classpath:db/migration`. No Gradle plugin, no
  manual step — starting the app migrates it.
- Exposed `Table` objects describe the schema for queries; the DDL lives in the SQL files. Keeping the two
  in sync is manual.
