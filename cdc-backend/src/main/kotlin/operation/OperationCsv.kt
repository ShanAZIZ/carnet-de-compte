package site.cdc.operation

import com.jsoizo.kotlincsv.CsvDialect
import com.jsoizo.kotlincsv.csvReader
import com.jsoizo.kotlincsv.reader.readAll
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate

@Serializable
data class ColumnMapping(
    @SerialName("csv_index") val csvIndex: Int,
    @SerialName("app_column") val appColumn: String,
)

// ponytail: formats d'export figés ici (`;`, date ISO, virgule décimale, UTF-8) — une banque qui
// exporte autrement demandera de les faire remonter dans le body mapping.
private val csv = csvReader {
    dialect = CsvDialect(delimiter = ';')
    skipEmptyLine = true
}

private const val SUGGESTED_LABEL = "suggestedLabel"
private val requiredColumns = listOf(
    "operationDate",
    "debitDate",
    "label",
    "amount",
    "accountId",
    "accountLabel",
    "balance",
)
private val knownColumns = requiredColumns + SUGGESTED_LABEL

fun parseOperations(content: ByteArray, mapping: List<ColumnMapping>): List<NewOperation> {
    val mapped = mapping.map { it.appColumn }
    require(mapped.all { it in knownColumns }) {
        "Champs applicatifs inconnus : ${mapped - knownColumns.toSet()}. Attendus : $knownColumns"
    }
    require(mapped.distinct() == mapped) {
        "Champs applicatifs mappés plusieurs fois : ${mapped.groupingBy { it }.eachCount().filterValues { it > 1 }.keys}"
    }
    require(requiredColumns.all { it in mapped }) {
        "Champs applicatifs manquants : ${requiredColumns - mapped.toSet()}"
    }

    val rows = csv.readAll(content.inputStream())
    require(rows.size > 1) { "CSV sans ligne de données" }

    val header = rows.first()
    val columnIndex = mapping.associate { (csvIndex, appColumn) ->
        require(csvIndex in header.indices) { "Colonne n°$csvIndex absente de l'en-tête : $header" }
        appColumn to csvIndex
    }

    return rows.drop(1).mapIndexed { index, row ->
        OperationLineReader(row, columnIndex, line = index + 2).read()
    }
}

private class OperationLineReader(
    private val row: List<String>,
    private val columnIndex: Map<String, Int>,
    private val line: Int,
) {
    fun read() = NewOperation(
        operationDate = date("operationDate"),
        debitDate = date("debitDate"),
        label = text("label"),
        suggestedLabel = optionalText(SUGGESTED_LABEL),
        amount = money("amount"),
        accountId = text("accountId"),
        accountLabel = text("accountLabel"),
        balance = money("balance"),
    )

    private fun optionalText(appColumn: String) =
        columnIndex[appColumn]?.let { row.getOrNull(it) }?.trim()?.takeIf { it.isNotEmpty() }

    private fun text(appColumn: String) =
        optionalText(appColumn) ?: reject(appColumn, "est vide")

    private fun date(appColumn: String) = text(appColumn).let { value ->
        runCatching { LocalDate.parse(value) }
            .getOrElse { reject(appColumn, "« $value » n'est pas une date ISO (yyyy-MM-dd)") }
    }

    private fun money(appColumn: String) = text(appColumn).let { value ->
        runCatching { BigDecimal(value.replace('\u00A0', ' ').filterNot { it.isWhitespace() }.replace(',', '.')) }
            .getOrElse { reject(appColumn, "« $value » n'est pas un montant") }
    }

    private fun reject(appColumn: String, why: String): Nothing =
        throw IllegalArgumentException("Ligne $line, champ $appColumn : $why")
}
