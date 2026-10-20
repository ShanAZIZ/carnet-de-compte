package site.cdc.config

import io.ktor.server.application.Application
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.configureDatabase() {
    val db = environment.config.config("db")
    val url = db.property("url").getString()
    val user = db.property("user").getString()
    val password = db.property("password").getString()

    Flyway.configure().dataSource(url, user, password).load().migrate()

    Database.connect(url = url, driver = "org.postgresql.Driver", user = user, password = password)
}
