package io.hhplus.tdd.point.repository

import io.hhplus.tdd.point.domain.TransactionEntity

interface TransactionEntityRepository {
    fun findAllByUserId(userId: Long): List<TransactionEntity>

    fun deleteAll()
}