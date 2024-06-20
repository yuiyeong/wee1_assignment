package io.hhplus.tdd.point.mock

import io.hhplus.tdd.point.domain.PointEntity
import io.hhplus.tdd.point.repository.PointEntityRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
class StubPointEntityRepository : PointEntityRepository {
    private val table = HashMap<Long, PointEntity>()
    override fun findOrCreateByUserId(userId: Long): PointEntity {
        return table[userId] ?: PointEntity(userId, 0, System.currentTimeMillis())
    }

    override fun insertOrUpdate(pointEntity: PointEntity): PointEntity {
        val userId = pointEntity.id
        val newPointEntity = PointEntity(userId, pointEntity.point, System.currentTimeMillis())
        table[userId] = newPointEntity
        return newPointEntity
    }

    override fun deleteAll() {
        table.clear()
    }
}