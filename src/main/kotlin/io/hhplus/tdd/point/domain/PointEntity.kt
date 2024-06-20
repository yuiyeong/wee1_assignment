package io.hhplus.tdd.point.domain

import io.hhplus.tdd.point.UserPoint
import io.hhplus.tdd.point.exception.NegativeAmountException

class PointEntity(
    val id: Long,
    point: Long,
    val updatedMillis: Long
) {
    var point: Long = point
        private set

    fun addPoint(amount: Long): TransactionEntity {
        if (amount <= 0)
            throw NegativeAmountException("amount 는 0보다 커야합니다.")

        point += amount

        return TransactionEntity(0, id, TransactionEntityType.ADD, amount, 0)
    }

    companion object {
        fun from(userPoint: UserPoint): PointEntity {
            return PointEntity(userPoint.id, userPoint.point, userPoint.updateMillis)
        }
    }
}
