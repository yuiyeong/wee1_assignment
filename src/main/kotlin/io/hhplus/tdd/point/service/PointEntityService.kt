package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.dto.PointEntityDto
import io.hhplus.tdd.point.repository.PointEntityRepository
import io.hhplus.tdd.point.repository.TransactionEntityRepository
import org.springframework.stereotype.Service

@Service
class PointEntityService(
    private val pointEntityRepository: PointEntityRepository,
    private val transactionEntityRepository: TransactionEntityRepository
) {
    fun charge(id: Long, amount: Long): PointEntityDto {
        val pointEntity = pointEntityRepository.findOrCreateByUserId(id)

        // 포인트 충전 비지니스 로직 실행
        val transaction = pointEntity.addPoint(amount)

        // 변경된 UserEntity 저장
        val updatedOne = pointEntityRepository.insertOrUpdate(pointEntity)

        // 변경사항인 transaction 저장
        transactionEntityRepository.insert(
            updatedOne, transaction.amount, transaction.type
        )

        return PointEntityDto.from(updatedOne)
    }
}
