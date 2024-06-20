package io.hhplus.tdd.point.infra

import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.domain.PointEntity
import io.hhplus.tdd.point.repository.PointEntityRepository
import org.springframework.stereotype.Repository

@Repository
class PointEntityRepositoryImpl(
    private val userPointTable: UserPointTable
) : PointEntityRepository {
    override fun findOrCreateByUserId(userId: Long): PointEntity {
        return PointEntity.from(userPointTable.selectById(userId))
    }

    override fun insertOrUpdate(pointEntity: PointEntity): PointEntity {
        return PointEntity.from(
            userPointTable.insertOrUpdate(pointEntity.id, pointEntity.point)
        )
    }

    override fun deleteAll() {
        // nothing to do
    }
}