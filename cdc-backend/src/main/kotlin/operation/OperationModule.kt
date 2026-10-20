package site.cdc.operation

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing

fun Application.operationModule() {
    dependencies {
        provide { OperationRepository() }
        provide { OperationService(resolve()) }
    }
    val service: OperationService by dependencies
    routing {
        operationRoutes(service)
    }
}
