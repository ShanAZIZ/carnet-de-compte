package site.cdc.operation

import site.cdc.config.ValidationException

class OperationService(private val repository: OperationRepository) {

    suspend fun list(): List<Operation> = repository.findAll()

    suspend fun import(csv: ByteArray, mapping: List<ColumnMapping>): ImportResult {
        val operations = try {
            parseOperations(csv, mapping)
        } catch (failure: IllegalArgumentException) {
            throw ValidationException(failure.message ?: "Import illisible", failure)
        }
        return ImportResult(deleted = repository.replaceForAccounts(operations), inserted = operations.size)
    }
}
