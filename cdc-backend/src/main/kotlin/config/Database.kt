package site.cdc.config

import io.ktor.server.application.Application
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.configureDatabase() {
    val db = environment.config.config("db")
    Database.connect(
        url = db.property("url").getString(),
        driver = "org.postgresql.Driver",
        user = db.property("user").getString(),
        password = db.property("password").getString(),
    )
}
