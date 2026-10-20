package site.cdc.operation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

object Operations : Table("operation") {
    val id = long("id").autoIncrement()
    val userId = text("user_id")
    val operationDate = date("operation_date")
    val debitDate = date("debit_date")
    val label = text("label")
    val suggestedLabel = text("suggested_label").nullable()
    val amount = decimal("amount", precision = 14, scale = 2)
    val accountId = text("account_id")
    val accountLabel = text("account_label")
    val balance = decimal("balance", precision = 14, scale = 2)

    override val primaryKey = PrimaryKey(id)
}

class OperationRepository {

    // ponytail: tout le relevé d'un coup — pagination quand un compte dépassera quelques milliers de lignes.
    suspend fun findAll(userId: String): List<Operation> = query {
        Operations.selectAll()
            .where { Operations.userId eq userId }
            .orderBy(Operations.operationDate to SortOrder.DESC, Operations.label to SortOrder.ASC)
            .map { it.toOperation() }
    }

    suspend fun replaceForAccounts(userId: String, operations: List<NewOperation>): Int = query {
        // ponytail: verrou de table, imports sérialisés tous comptes confondus — pg_advisory_xact_lock par compte si ça bloque.
        exec("LOCK TABLE operation IN SHARE ROW EXCLUSIVE MODE")
        val deleted = Operations.deleteWhere {
            (Operations.userId eq userId) and (accountId inList operations.map { it.accountId }.distinct())
        }
        Operations.batchInsert(operations, shouldReturnGeneratedValues = false) {
            this[Operations.userId] = userId
            this[Operations.operationDate] = it.operationDate
            this[Operations.debitDate] = it.debitDate
            this[Operations.label] = it.label
            this[Operations.suggestedLabel] = it.suggestedLabel
            this[Operations.amount] = it.amount
            this[Operations.accountId] = it.accountId
            this[Operations.accountLabel] = it.accountLabel
            this[Operations.balance] = it.balance
        }
        deleted
    }

    private suspend fun <T> query(block: suspend JdbcTransaction.() -> T): T =
        withContext(Dispatchers.IO) { suspendTransaction { block() } }

    private fun ResultRow.toOperation() = Operation(
        id = this[Operations.id],
        operationDate = this[Operations.operationDate],
        debitDate = this[Operations.debitDate],
        label = this[Operations.label],
        suggestedLabel = this[Operations.suggestedLabel],
        amount = this[Operations.amount],
        accountId = this[Operations.accountId],
        accountLabel = this[Operations.accountLabel],
        balance = this[Operations.balance],
    )
}
