package io.hhplus.tdd.point.domain

import io.hhplus.tdd.point.UserPoint

data class PointEntity(
    val id: Long,
    val point: Long,
    val updateMillis: Long
) {
    companion object {
        fun from(userPoint: UserPoint): PointEntity {
            return PointEntity(userPoint.id, userPoint.point, userPoint.updateMillis)
        }
    }
}
