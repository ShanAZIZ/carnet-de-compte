package site.cdc.operation

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.PayloadTooLargeException
import io.ktor.server.request.contentLength
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.utils.io.readBuffer
import kotlinx.io.readByteArray
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import site.cdc.config.ValidationException

private const val MAX_UPLOAD_BYTES = 10L * 1024 * 1024

fun Route.operationRoutes(service: OperationService) {
    route("/operations") {
        get {
            call.respond(service.list().map { it.toResponse() })
        }
        post {
            val upload = call.receiveImport()
            call.respond(HttpStatusCode.Created, service.import(upload.csv, upload.mapping).toResponse())
        }
    }
}

private class ImportUpload(val csv: ByteArray, val mapping: List<ColumnMapping>)

private suspend fun ApplicationCall.receiveImport(): ImportUpload {
    if ((request.contentLength() ?: 0) > MAX_UPLOAD_BYTES) throw PayloadTooLargeException(MAX_UPLOAD_BYTES)
    var csv: ByteArray? = null
    var mapping: String? = null
    receiveMultipart().forEachPart { part ->
        when (part) {
            is PartData.FileItem if part.name == "file" -> csv = part.provider().readBuffer(MAX_UPLOAD_BYTES + 1).readByteArray()
            is PartData.FormItem if part.name == "mapping" -> mapping = part.value
            else -> {}
        }
        part.release()
    }
    if ((csv?.size ?: 0) > MAX_UPLOAD_BYTES) throw PayloadTooLargeException(MAX_UPLOAD_BYTES)
    return ImportUpload(
        csv = csv ?: throw ValidationException("Part « file » attendue : le CSV du relevé"),
        mapping = decodeMapping(mapping ?: throw ValidationException("Part « mapping » attendue : le JSON des colonnes")),
    )
}

private fun decodeMapping(json: String): List<ColumnMapping> = try {
    Json.decodeFromString(json)
} catch (failure: SerializationException) {
    throw ValidationException("Mapping illisible : ${failure.message}", failure)
}
