package io.hhplus.tdd.point.dto

import io.hhplus.tdd.point.domain.PointEntity

data class PointEntityDto(
    val id: Long,
    val point: Long,
    val updatedMillis: Long
) {
    companion object {
        fun from(entity: PointEntity): PointEntityDto {
            return PointEntityDto(entity.id, entity.point, entity.updatedMillis)
        }
    }
}
