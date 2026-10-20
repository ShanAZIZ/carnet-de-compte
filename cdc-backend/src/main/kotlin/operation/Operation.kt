package site.cdc.operation

import java.math.BigDecimal
import java.time.LocalDate

data class NewOperation(
    val operationDate: LocalDate,
    val debitDate: LocalDate,
    val label: String,
    val suggestedLabel: String?,
    val amount: BigDecimal,
    val accountId: String,
    val accountLabel: String,
    val balance: BigDecimal,
)

data class Operation(
    val id: Long,
    val operationDate: LocalDate,
    val debitDate: LocalDate,
    val label: String,
    val suggestedLabel: String?,
    val amount: BigDecimal,
    val accountId: String,
    val accountLabel: String,
    val balance: BigDecimal,
)

data class ImportResult(val deleted: Int, val inserted: Int)
