package io.hhplus.tdd.point.repository

import io.hhplus.tdd.point.domain.PointEntity

interface PointEntityRepository {
    fun findOrCreateByUserId(userId: Long): PointEntity

    fun insertOrUpdate(pointEntity: PointEntity): PointEntity

    fun deleteAll()
}