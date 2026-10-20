# Kotlin & Ktor

## Source of Truth

- Check the official Ktor documentation (ktor.io/docs) and Kotlin docs (kotlinlang.org/docs) before applying a pattern. Older Ktor 1.x idioms are NOT valid here — this is Ktor 3.
- If a rule here conflicts with the official docs, the docs win — say so and update this file.

## Structure

- Package root: `site.cdc`. One file per concern under `src/main/kotlin/`, named after what it declares (`Routing.kt`, `Serialization.kt`).
- Wire features as `Application` extension functions (`fun Application.configureX()`) registered in `application.yaml` under `ktor.application.modules` — not by calling them from `main`.
- `main.kt` stays a one-liner delegating to `EngineMain`. Never add setup code there.
- Group routes per domain in their own `fun Route.xRoutes()` and install them from `configureRouting`. No controller classes, no service layer until a second caller exists.

## Kotlin style

- `val` over `var`; data classes for payloads; no nullable field that a sensible default covers.
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
