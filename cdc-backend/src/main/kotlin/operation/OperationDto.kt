package site.cdc.operation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ponytail: montants en chaîne pour ne rien perdre en JSON — typage numérique si le front doit calculer.
@Serializable
data class OperationResponse(
    val id: Long,
    @SerialName("operation_date") val operationDate: String,
    @SerialName("debit_date") val debitDate: String,
    val label: String,
    @SerialName("suggested_label") val suggestedLabel: String?,
    val amount: String,
    @SerialName("account_id") val accountId: String,
    @SerialName("account_label") val accountLabel: String,
    val balance: String,
)

fun Operation.toResponse() = OperationResponse(
    id = id,
    operationDate = operationDate.toString(),
    debitDate = debitDate.toString(),
    label = label,
    suggestedLabel = suggestedLabel,
    amount = amount.toPlainString(),
    accountId = accountId,
    accountLabel = accountLabel,
    balance = balance.toPlainString(),
)

@Serializable
data class ImportResponse(val deleted: Int, val inserted: Int)

fun ImportResult.toResponse() = ImportResponse(deleted = deleted, inserted = inserted)
