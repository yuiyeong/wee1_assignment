package io.hhplus.tdd.point.repository

import io.hhplus.tdd.point.domain.PointEntity
import io.hhplus.tdd.point.domain.TransactionEntity
import io.hhplus.tdd.point.domain.TransactionEntityType

interface TransactionEntityRepository {
    fun findAllByUserId(userId: Long): List<TransactionEntity>

    fun insert(pointEntity: PointEntity, amount: Long, type: TransactionEntityType): TransactionEntity

    fun deleteAll()
}