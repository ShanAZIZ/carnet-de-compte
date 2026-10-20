package site.cdc.config

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.PayloadTooLargeException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

class ValidationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message.orEmpty())
        }
        exception<PayloadTooLargeException> { call, cause ->
            call.respond(HttpStatusCode.PayloadTooLarge, cause.message.orEmpty())
        }
    }
}
