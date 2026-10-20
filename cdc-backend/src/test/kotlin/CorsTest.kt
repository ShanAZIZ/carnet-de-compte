package site.cdc

import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class CorsTest {

    @Test
    fun `allows the configured frontend origin to send its token`() = testApplication {
        configureTestApp()

        val response = client.options("/operations") {
            header(HttpHeaders.Origin, "http://localhost:4200")
            header(HttpHeaders.AccessControlRequestMethod, HttpMethod.Post.value)
            header(HttpHeaders.AccessControlRequestHeaders, HttpHeaders.Authorization)
        }

        assertEquals(
            listOf(HttpStatusCode.OK, "http://localhost:4200"),
            listOf(response.status, response.headers[HttpHeaders.AccessControlAllowOrigin]),
        )
    }

    @Test
    fun `rejects an unknown origin`() = testApplication {
        configureTestApp()

        val response = client.options("/operations") {
            header(HttpHeaders.Origin, "https://evil.example")
            header(HttpHeaders.AccessControlRequestMethod, HttpMethod.Post.value)
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}
