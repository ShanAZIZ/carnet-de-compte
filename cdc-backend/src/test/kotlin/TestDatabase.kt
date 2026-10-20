package site.cdc

import io.ktor.server.testing.ApplicationTestBuilder
import org.testcontainers.containers.PostgreSQLContainer

// ponytail: conteneur partagé par tout le run, jamais nettoyé — TRUNCATE dans un @BeforeTest si deux tests se marchent dessus.
internal val postgres = PostgreSQLContainer("postgres:17-alpine").apply { start() }

internal fun ApplicationTestBuilder.configureTestApp() = configure(overrides = {
    put("db.url", postgres.jdbcUrl)
    put("db.user", postgres.username)
    put("db.password", postgres.password)
})
