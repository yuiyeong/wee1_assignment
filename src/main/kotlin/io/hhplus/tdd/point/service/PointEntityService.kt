package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.dto.PointEntityDto
import io.hhplus.tdd.point.dto.TransactionEntityDto
import io.hhplus.tdd.point.lock.PointLockManager
import io.hhplus.tdd.point.repository.PointEntityRepository
import io.hhplus.tdd.point.repository.TransactionEntityRepository
import org.springframework.stereotype.Service

@Service
class PointEntityService(
    private val pointEntityRepository: PointEntityRepository,
    private val transactionEntityRepository: TransactionEntityRepository,
    private val pointLockManager: PointLockManager
) {
    fun charge(userId: Long, amount: Long): PointEntityDto {
        val result: PointEntityDto

        // 한 사용자의 포인트에 대해서 동시에 여러 요청이 들어와도, 처리할 수 있도록 사용자별 lock 을 사용
        val lock = pointLockManager.getLock(userId).apply { lock() }

        try {
            // 기존 사용자를 가져오거나, 새로운 사용자를 만들거나
            val pointEntity = pointEntityRepository.findOrCreateByUserId(userId)

            // 포인트 충전 비지니스 로직 실행
            val transaction = pointEntity.addPoint(amount)

            // 변경된 PointEntity 저장
            val updatedOne = pointEntityRepository.insertOrUpdate(pointEntity)

            // 변경사항인 transaction 저장
            transactionEntityRepository.insert(
                updatedOne, transaction.amount, transaction.type
            )

            result = PointEntityDto.from(updatedOne)
        } finally {
            lock.unlock()
        }

        return result
    }

    fun findOrCreateOneById(id: Long): PointEntityDto {
        return PointEntityDto.from(pointEntityRepository.findOrCreateByUserId(id))
    }

    fun findTransactionsByUserId(userId: Long): List<TransactionEntityDto> {
        return transactionEntityRepository.findAllByUserId(userId).map { TransactionEntityDto.from(it) }
    }

    fun use(userId: Long, amount: Long): PointEntityDto {
        val result: PointEntityDto

        // 한 사용자의 포인트에 대해서 동시에 여러 요청이 들어와도, 처리할 수 있도록 사용자별 lock 을 사용
        val lock = pointLockManager.getLock(userId).apply { lock() }

        try {
            val pointEntity = pointEntityRepository.findOrCreateByUserId(userId)

            // 포인트 사용 비지니스 로직 실행
            val transaction = pointEntity.deductPoint(amount)

            // 변경된 UserEntity 저장
            val updatedOne = pointEntityRepository.insertOrUpdate(pointEntity)

            // 변경사항인 transaction 저장
            transactionEntityRepository.insert(
                updatedOne, transaction.amount, transaction.type
            )

            result = PointEntityDto.from(updatedOne)
        } finally {
            lock.unlock()
        }

        return result
    }
}
