package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.dto.PointEntityDto
import io.hhplus.tdd.point.dto.TransactionEntityDto
import io.hhplus.tdd.point.repository.PointEntityRepository
import io.hhplus.tdd.point.repository.TransactionEntityRepository
import org.springframework.stereotype.Service

@Service
class PointEntityService(
    private val pointEntityRepository: PointEntityRepository,
    private val transactionEntityRepository: TransactionEntityRepository
) {
    fun charge(userId: Long, amount: Long): PointEntityDto {
        val pointEntity = pointEntityRepository.findOrCreateByUserId(userId)

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

    fun findOrCreateOneById(id: Long): PointEntityDto {
        return PointEntityDto.from(pointEntityRepository.findOrCreateByUserId(id))
    }

    fun findTransactionsByUserId(userId: Long): List<TransactionEntityDto> {
        return transactionEntityRepository.findAllByUserId(userId).map { TransactionEntityDto.from(it) }
    }

    fun use(userId: Long, amount: Long): PointEntityDto {
        val pointEntity = pointEntityRepository.findOrCreateByUserId(userId)

        // 포인트 사용 비지니스 로직 실행
        val transaction = pointEntity.deductPoint(amount)

        // 변경된 UserEntity 저장
        val updatedOne = pointEntityRepository.insertOrUpdate(pointEntity)

        // 변경사항인 transaction 저장
        transactionEntityRepository.insert(
            updatedOne, transaction.amount, transaction.type
        )

        return PointEntityDto.from(updatedOne)
    }
}
