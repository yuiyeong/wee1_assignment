package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.dto.PointEntityDto
import org.springframework.stereotype.Service

@Service
class PointEntityService {
    fun charge(id: Long, amount: Long): PointEntityDto {
        return PointEntityDto(0, 0, System.currentTimeMillis())
    }
}
