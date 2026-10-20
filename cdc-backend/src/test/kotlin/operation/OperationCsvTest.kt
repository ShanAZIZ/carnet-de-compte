package site.cdc.operation

import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private val fullMapping = listOf(
    ColumnMapping("Date de l'opération", "operationDate"),
    ColumnMapping("Date de valeur", "debitDate"),
    ColumnMapping("Libellé", "label"),
    ColumnMapping("Catégorie", "suggestedLabel"),
    ColumnMapping("Montant", "amount"),
    ColumnMapping("Compte", "accountId"),
    ColumnMapping("Intitulé du compte", "accountLabel"),
    ColumnMapping("Solde", "balance"),
)

private const val HEADER =
    "Compte;Intitulé du compte;Date de l'opération;Date de valeur;Libellé;Catégorie;Montant;Solde"

private fun csv(vararg rows: String) = (listOf(HEADER) + rows).joinToString("\n").toByteArray()

class OperationCsvTest {

    @Test
    fun `maps columns by header name whatever their order`() {
        val rows = parseOperations(
            csv("FR7612345;Compte courant;05/01/2026;06/01/2026;CAFE;Restaurant;-3,50;908,90"),
            fullMapping,
        )

        assertEquals(
            NewOperation(
                operationDate = LocalDate.of(2026, 1, 5),
                debitDate = LocalDate.of(2026, 1, 6),
                label = "CAFE",
                suggestedLabel = "Restaurant",
                amount = BigDecimal("-3.50"),
                accountId = "FR7612345",
                accountLabel = "Compte courant",
                balance = BigDecimal("908.90"),
            ),
            rows.single(),
        )
    }

    @Test
    fun `keeps a quoted label containing the delimiter`() {
        val rows = parseOperations(
            csv("""FR7612345;Compte courant;05/01/2026;06/01/2026;"CARTE 05/01 AUCHAN;PARIS";Courses;-42,10;870,30"""),
            fullMapping,
        )

        assertEquals("CARTE 05/01 AUCHAN;PARIS", rows.single().label)
    }

    @Test
    fun `parses a french amount with a non breaking thousands separator`() {
        val rows = parseOperations(
            csv("FR7612345;Compte courant;05/01/2026;06/01/2026;LOYER;Logement;-1\u00A0234,56;-363,66"),
            fullMapping,
        )

        assertEquals(BigDecimal("-1234.56"), rows.single().amount)
        assertEquals(BigDecimal("-363.66"), rows.single().balance)
    }

    @Test
    fun `leaves an empty suggested label null`() {
        val rows = parseOperations(
            csv("FR7612345;Compte courant;05/01/2026;06/01/2026;CAFE;;-3,50;908,90"),
            fullMapping,
        )

        assertEquals(null, rows.single().suggestedLabel)
    }

    @Test
    fun `rejects an unknown app column`() {
        assertFailsWith<IllegalArgumentException> {
            parseOperations(csv(), fullMapping + ColumnMapping("Solde", "soldeFinal"))
        }
    }

    @Test
    fun `rejects a mapping missing a required app column`() {
        assertFailsWith<IllegalArgumentException> {
            parseOperations(csv(), fullMapping.filterNot { it.appColumn == "amount" })
        }
    }

    @Test
    fun `rejects a column absent from the header`() {
        assertFailsWith<IllegalArgumentException> {
            parseOperations(
                csv("FR7612345;Compte courant;05/01/2026;06/01/2026;CAFE;Restaurant;-3,50;908,90"),
                fullMapping.map { if (it.appColumn == "label") it.copy(csvColumn = "Intitulé") else it },
            )
        }
    }

    @Test
    fun `rejects an unparseable date`() {
        assertFailsWith<IllegalArgumentException> {
            parseOperations(
                csv("FR7612345;Compte courant;2026-01-05;06/01/2026;CAFE;Restaurant;-3,50;908,90"),
                fullMapping,
            )
        }
    }

    @Test
    fun `rejects a csv without a data row`() {
        assertFailsWith<IllegalArgumentException> { parseOperations(csv(), fullMapping) }
    }
}
