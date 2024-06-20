package io.hhplus.tdd.point.infra

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.point.domain.PointEntity
import io.hhplus.tdd.point.domain.TransactionEntity
import io.hhplus.tdd.point.domain.TransactionEntityType
import io.hhplus.tdd.point.repository.TransactionEntityRepository
import org.springframework.stereotype.Repository

@Repository
class TransactionEntityRepositoryImpl(
    private val pointHistoryTable: PointHistoryTable
) : TransactionEntityRepository {
    override fun findAllByUserId(userId: Long): List<TransactionEntity> {
        return pointHistoryTable.selectAllByUserId(userId).map {
            TransactionEntity.from(it)
        }
    }

    override fun insert(pointEntity: PointEntity, amount: Long, type: TransactionEntityType): TransactionEntity {
        val pointHistory = pointHistoryTable.insert(
            pointEntity.id,
            amount,
            type.toTransactionType(),
            pointEntity.updatedMillis
        )
        return TransactionEntity.from(pointHistory)
    }

    override fun deleteAll() {
        // nothing to do
    }
}