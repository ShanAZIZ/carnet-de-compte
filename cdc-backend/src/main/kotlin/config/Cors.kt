package site.cdc.config

import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCors() {
    val allowedOrigins = environment.config.property("cors.allowedOrigins").getString()
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

    install(CORS) {
        allowOrigins { it in allowedOrigins }
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }
}
