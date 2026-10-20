package site.cdc

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import kotlin.test.*

@Serializable
private data class Payload(val ok: Boolean)

class ServerTest {

    @Test
    fun `test root endpoint`() = testApplication {
        configure()
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

    @Test
    fun `responds json for serializable payloads`() = testApplication {
        configure()
        routing { get("/payload") { call.respond(Payload(true)) } }

        val response = client.get("/payload")
        assertEquals(ContentType.Application.Json, response.contentType()?.withoutParameters())
        assertEquals("""{"ok":true}""", response.bodyAsText())
    }

}
