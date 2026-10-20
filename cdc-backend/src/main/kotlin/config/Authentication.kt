package site.cdc.config

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import java.net.URI
import java.util.concurrent.TimeUnit

fun Application.configureAuthentication() {
    val firebase = environment.config.config("firebase")
    val projectId = firebase.property("projectId").getString()
    val keys = JwkProviderBuilder(URI(firebase.property("jwksUrl").getString()).toURL())
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt {
            verifier(keys, "https://securetoken.google.com/$projectId") {
                withAudience(projectId)
                acceptLeeway(5)
            }
            validate { credential -> credential.subject?.takeIf { it.isNotBlank() }?.let(::UserIdPrincipal) }
        }
    }
}

val ApplicationCall.userId: String
    get() = checkNotNull(principal<UserIdPrincipal>()) { "Route hors de authenticate { }" }.name
