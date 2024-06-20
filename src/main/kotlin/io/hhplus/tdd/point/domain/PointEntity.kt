package io.hhplus.tdd.point.domain

import io.hhplus.tdd.point.UserPoint
import io.hhplus.tdd.point.exception.ExceedPointException
import io.hhplus.tdd.point.exception.NegativeAmountException

class PointEntity(
    val id: Long,
    point: Long,
    val updatedMillis: Long
) {
    var point: Long = point
        private set

    fun addPoint(amount: Long): TransactionEntity {
        verifyAmountIsNaturalNumber(amount)

        point += amount

        return TransactionEntity(0, id, TransactionEntityType.ADD, amount, 0)
    }

    fun deductPoint(amount: Long): TransactionEntity {
        verifyAmountIsNaturalNumber(amount)

        verifyAmountIsEqualOrLessThanPoint(amount)

        point -= amount

        return TransactionEntity(0, id, TransactionEntityType.DEDUCT, amount, 0)
    }

    private fun verifyAmountIsNaturalNumber(amount: Long) {
        if (amount <= 0)
            throw NegativeAmountException("amount 는 0보다 커야합니다.")
    }

    private fun verifyAmountIsEqualOrLessThanPoint(amount: Long) {
        if (amount > point)
            throw ExceedPointException("amount 는 포인트보다 클 수 없습니다.")
    }


    companion object {
        fun from(userPoint: UserPoint): PointEntity {
            return PointEntity(userPoint.id, userPoint.point, userPoint.updateMillis)
        }
    }
}
