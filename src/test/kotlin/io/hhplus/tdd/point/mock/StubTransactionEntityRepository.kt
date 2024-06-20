package io.hhplus.tdd.point.mock

import io.hhplus.tdd.point.domain.TransactionEntity
import io.hhplus.tdd.point.repository.TransactionEntityRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
class StubTransactionEntityRepository : TransactionEntityRepository {
    private val table = mutableListOf<TransactionEntity>()

    override fun findAllByUserId(userId: Long): List<TransactionEntity> {
        return table.filter { it.userId == userId }
    }

    override fun deleteAll() {
        table.clear()
    }
}