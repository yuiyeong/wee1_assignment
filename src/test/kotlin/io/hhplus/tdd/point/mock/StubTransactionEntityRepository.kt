package io.hhplus.tdd.point.mock

import io.hhplus.tdd.point.domain.PointEntity
import io.hhplus.tdd.point.domain.TransactionEntity
import io.hhplus.tdd.point.domain.TransactionEntityType
import io.hhplus.tdd.point.repository.TransactionEntityRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
class StubTransactionEntityRepository : TransactionEntityRepository {
    private val table = mutableListOf<TransactionEntity>()
    private var cursor = 1L

    override fun findAllByUserId(userId: Long): List<TransactionEntity> {
        return table.filter { it.userId == userId }
    }

    override fun insert(pointEntity: PointEntity, amount: Long, type: TransactionEntityType): TransactionEntity {
        val id = cursor++
        val transaction = TransactionEntity(
            id,
            pointEntity.id,
            type,
            amount,
            pointEntity.updatedMillis
        )
        table.add(transaction)
        return transaction
    }

    override fun deleteAll() {
        table.clear()
    }
}