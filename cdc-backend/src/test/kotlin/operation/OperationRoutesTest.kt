package site.cdc.operation

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.server.testing.testApplication
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import site.cdc.configureTestApp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val HEADER = "Compte;Intitulé du compte;Date;Valeur;Libellé;Catégorie;Montant;Solde"

private val MAPPING = """
    [{"csv_column":"Date","app_column":"operationDate"},
     {"csv_column":"Valeur","app_column":"debitDate"},
     {"csv_column":"Libellé","app_column":"label"},
     {"csv_column":"Catégorie","app_column":"suggestedLabel"},
     {"csv_column":"Montant","app_column":"amount"},
     {"csv_column":"Compte","app_column":"accountId"},
     {"csv_column":"Intitulé du compte","app_column":"accountLabel"},
     {"csv_column":"Solde","app_column":"balance"}]
""".trimIndent()

private fun statement(account: String, vararg rows: String) =
    (listOf(HEADER) + rows.map { "$account;Compte courant;$it" }).joinToString("\n")

private suspend fun HttpClient.import(csv: String?, mapping: String? = MAPPING): HttpResponse =
    post("/operations") {
        setBody(
            MultiPartFormDataContent(
                formData {
                    if (mapping != null) append("mapping", mapping)
                    if (csv != null) {
                        append(
                            "file",
                            csv.toByteArray(),
                            Headers.build {
                                append(HttpHeaders.ContentType, ContentType.Text.CSV.toString())
                                append(HttpHeaders.ContentDisposition, ContentDisposition.File.withParameter("filename", "releve.csv").toString())
                            },
                        )
                    }
                },
            ),
        )
    }

private suspend fun labelsOf(account: String) = withContext(Dispatchers.IO) {
    suspendTransaction {
        Operations.selectAll().where { Operations.accountId eq account }
            .orderBy(Operations.operationDate)
            .map { it[Operations.label] }
    }
}

class OperationRoutesTest {

    @Test
    fun `imports every row of the csv`() = testApplication {
        configureTestApp()

        val response = client.import(
            statement("FR001", "05/01/2026;06/01/2026;CAFE;Restaurant;-3,50;908,90", "06/01/2026;07/01/2026;LOYER;Logement;-700,00;208,90"),
        )

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("""{"deleted":0,"inserted":2}""", response.bodyAsText())
        assertEquals(listOf("CAFE", "LOYER"), labelsOf("FR001"))
    }

    @Test
    fun `re-import replaces instead of duplicating`() = testApplication {
        configureTestApp()

        val csv = statement("FR002", "05/01/2026;06/01/2026;CAFE;Restaurant;-3,50;908,90")
        client.import(csv)
        val response = client.import(csv)

        assertEquals("""{"deleted":1,"inserted":1}""", response.bodyAsText())
        assertEquals(listOf("CAFE"), labelsOf("FR002"))
    }

    @Test
    fun `import of one account leaves the other untouched`() = testApplication {
        configureTestApp()

        client.import(statement("FR003", "05/01/2026;06/01/2026;CAFE;Restaurant;-3,50;908,90"))
        client.import(statement("FR004", "05/01/2026;06/01/2026;ESSENCE;Transport;-60,00;140,00"))
        val response = client.import(statement("FR003", "07/01/2026;08/01/2026;CINEMA;Loisirs;-12,00;896,90"))

        assertEquals("""{"deleted":1,"inserted":1}""", response.bodyAsText())
        assertEquals(listOf("CINEMA"), labelsOf("FR003"))
        assertEquals(listOf("ESSENCE"), labelsOf("FR004"))
    }

    @Test
    fun `lists the imported operations most recent first`() = testApplication {
        configureTestApp()

        client.import(
            statement("FR007", "05/01/2026;06/01/2026;CAFE;Restaurant;-3,50;908,90", "06/01/2026;07/01/2026;LOYER;;-700,00;208,90"),
        )

        val response = client.get("/operations")

        assertEquals(HttpStatusCode.OK, response.status)
        val operations = Json.decodeFromString<List<OperationResponse>>(response.bodyAsText()).filter { it.accountId == "FR007" }
        assertEquals(listOf("LOYER", "CAFE"), operations.map { it.label })
        assertEquals(
            OperationResponse(
                id = operations.first().id,
                operationDate = "2026-01-06",
                debitDate = "2026-01-07",
                label = "LOYER",
                suggestedLabel = null,
                amount = "-700.00",
                accountId = "FR007",
                accountLabel = "Compte courant",
                balance = "208.90",
            ),
            operations.first(),
        )
        assertEquals(2, operations.map { it.id }.distinct().size)
    }

    @Test
    fun `rejects a request without the mapping part`() = testApplication {
        configureTestApp()

        val response = client.import(statement("FR005", "05/01/2026;06/01/2026;CAFE;Restaurant;-3,50;908,90"), mapping = null)

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `rejects a csv whose amount is unparseable`() = testApplication {
        configureTestApp()

        val response = client.import(statement("FR006", "05/01/2026;06/01/2026;CAFE;Restaurant;trois euros;908,90"))

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("amount"), response.bodyAsText())
        assertTrue(labelsOf("FR006").isEmpty())
    }

    @Test
    fun `rejects a chunked upload beyond the size limit`() = testApplication {
        configureTestApp()
        val body = (
            "--b\r\nContent-Disposition: form-data; name=\"file\"; filename=\"releve.csv\"\r\n\r\n" +
                "x".repeat(11 * 1024 * 1024) + "\r\n--b--\r\n"
            ).toByteArray()

        val response = client.post("/operations") {
            setBody(object : OutgoingContent.WriteChannelContent() {
                override val contentType = ContentType.MultiPart.FormData.withParameter("boundary", "b")
                override suspend fun writeTo(channel: ByteWriteChannel) = channel.writeFully(body)
            })
        }

        assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
    }
}
