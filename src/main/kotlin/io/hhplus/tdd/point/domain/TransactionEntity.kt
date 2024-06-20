package io.hhplus.tdd.point.domain

data class TransactionEntity(
    val id: Long,
    val userId: Long,
    val type: TransactionEntityType,
    val amount: Long,
    val timeMillis: Long,
)

enum class TransactionEntityType {
    ADD, DEDUCT
}
