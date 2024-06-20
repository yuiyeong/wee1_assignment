package io.hhplus.tdd.point.domain

import io.hhplus.tdd.point.PointHistory
import io.hhplus.tdd.point.TransactionType

data class TransactionEntity(
    val id: Long,
    val userId: Long,
    val type: TransactionEntityType,
    val amount: Long,
    val timeMillis: Long
) {

    companion object {
        fun from(pointHistory: PointHistory): TransactionEntity {
            return TransactionEntity(
                pointHistory.id,
                pointHistory.userId,
                TransactionEntityType.from(pointHistory.type),
                pointHistory.amount,
                pointHistory.timeMillis
            )
        }
    }
}

enum class TransactionEntityType {
    ADD, DEDUCT;

    fun toTransactionType(): TransactionType {
        return when (this) {
            ADD -> TransactionType.CHARGE
            DEDUCT -> TransactionType.USE
        }
    }

    companion object {
        fun from(transactionType: TransactionType): TransactionEntityType {
            return when (transactionType) {
                TransactionType.CHARGE -> ADD
                TransactionType.USE -> DEDUCT
            }
        }
    }
}
