package site.cdc.operation

import site.cdc.config.ValidationException

class OperationService(private val repository: OperationRepository) {

    suspend fun list(userId: String): List<Operation> = repository.findAll(userId)

    suspend fun import(userId: String, csv: ByteArray, mapping: List<ColumnMapping>): ImportResult {
        val operations = try {
            parseOperations(csv, mapping)
        } catch (failure: IllegalArgumentException) {
            throw ValidationException(failure.message ?: "Import illisible", failure)
        }
        return ImportResult(deleted = repository.replaceForAccounts(userId, operations), inserted = operations.size)
    }
}
