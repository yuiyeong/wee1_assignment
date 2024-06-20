package io.hhplus.tdd.point.dto

import io.hhplus.tdd.point.TransactionType
import io.hhplus.tdd.point.domain.TransactionEntity

data class TransactionEntityDto(
    val id: Long,
    val userId: Long,
    val type: TransactionType,
    val amount: Long,
    val timeMillis: Long
) {
    companion object {
        fun from(transaction: TransactionEntity): TransactionEntityDto {
            return TransactionEntityDto(
                transaction.id,
                transaction.userId,
                transaction.type.toTransactionType(),
                transaction.amount,
                transaction.timeMillis
            )
        }
    }
}