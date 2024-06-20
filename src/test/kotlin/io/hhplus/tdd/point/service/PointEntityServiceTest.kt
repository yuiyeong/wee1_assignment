package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.domain.PointEntity
import io.hhplus.tdd.point.domain.TransactionEntityType
import io.hhplus.tdd.point.exception.NegativeAmountException
import io.hhplus.tdd.point.repository.PointEntityRepository
import io.hhplus.tdd.point.repository.TransactionEntityRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.reset
import org.mockito.BDDMockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean

@SpringBootTest
class PointEntityServiceTest @Autowired constructor(
    @SpyBean private val pointEntityRepository: PointEntityRepository,
    @SpyBean private val transactionEntityRepository: TransactionEntityRepository,
    private val pointEntityService: PointEntityService
) {
    @AfterEach
    fun afterEach() {
        reset(pointEntityRepository)
        reset(transactionEntityRepository)

        pointEntityRepository.deleteAll()
        transactionEntityRepository.deleteAll()
    }

    /**
     * 양수 amount 로 charge 를 시도하면,
     * ADD type 의 TransactionEntity 를 추가하고, 또한 충전된 정보로 PointEntityDto 를 반환해야한다.
     */
    @Test
    fun `should return UserEntityDto that has the added points and insert TransactionEntity`() {
        // given: PointEntity 1 개가 있고, transaction 은 없는 상황
        val point = 1000L
        val pointEntity = PointEntity(1L, point, System.currentTimeMillis() - 10)
        given(pointEntityRepository.findOrCreateByUserId(pointEntity.id)).willReturn(pointEntity)

        val transactionCount = transactionEntityRepository.findAllByUserId(pointEntity.id).count()
        val amount = 100L

        // when
        val dto = pointEntityService.charge(pointEntity.id, amount)

        // then: 충전이 반영된 PointEntityDto 가 오고
        Assertions.assertThat(dto.id).isEqualTo(pointEntity.id)
        Assertions.assertThat(dto.point).isEqualTo(point + amount)
        Assertions.assertThat(dto.updatedMillis).isGreaterThan(pointEntity.updatedMillis)

        // transaction 은 원래보다 1개 늘어났는지 확인
        val history = transactionEntityRepository.findAllByUserId(pointEntity.id)
        Assertions.assertThat(history.count()).isEqualTo(transactionCount + 1)
        Assertions.assertThat(history.last().amount).isEqualTo(amount)
        Assertions.assertThat(history.last().type).isEqualTo(TransactionEntityType.ADD)
        Assertions.assertThat(history.last().timeMillis).isEqualTo(dto.updatedMillis)

        verify(pointEntityRepository).findOrCreateByUserId(pointEntity.id)
    }

    /**
     * 음수인 amount 로 charge 를 시도하면, NegativeAmountException 을 발생시켜야한다.
     */
    @Test
    fun `should throw NegativeAmountException when trying to charge negative amount`() {
        // given
        val userId = 1L
        val negativeAmount = -1000L

        // when & then
        Assertions.assertThatThrownBy { pointEntityService.charge(userId, negativeAmount) }
            .isInstanceOf(NegativeAmountException::class.java)
            .hasMessageContaining("amount 는 0보다 커야합니다.")
    }

    /**
     * 저장되어있는 UserPoint 의 id 로 조회를 시도하면,
     * 해당 UserPoint 의 정보를 PointEntityDto 로 반환해야한다.
     */
    @Test
    fun `should return UserEntityDto about saved UserPoint`() {
        // given

        val pointEntity = PointEntity(2L, 900L, System.currentTimeMillis() - 10)
        given(pointEntityRepository.findOrCreateByUserId(pointEntity.id)).willReturn(pointEntity)

        // when
        val userPointDto = pointEntityService.findOrCreateOneById(pointEntity.id)

        // then
        Assertions.assertThat(userPointDto.id).isEqualTo(pointEntity.id)
        Assertions.assertThat(userPointDto.point).isEqualTo(pointEntity.point)
        Assertions.assertThat(userPointDto.updatedMillis).isEqualTo(pointEntity.updatedMillis)
    }

    /**
     * 새로운 id 로 findOrCreateOneById 시도하면,
     * 새로운 UserPoint 에 대한 PointEntityDto 를 반환해야한다.
     */
    @Test
    fun `should return new UserEntityDto about new userId`() {
        // given
        val newId = 21L

        // when
        val newUserPointDto = pointEntityService.findOrCreateOneById(newId)

        // then
        Assertions.assertThat(newUserPointDto.id).isEqualTo(newId)
        Assertions.assertThat(newUserPointDto.point).isEqualTo(0)
    }
}