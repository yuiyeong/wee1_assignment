package io.hhplus.tdd.point.domain

import io.hhplus.tdd.point.exception.NegativeAmountException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class PointEntityTest {

    /**
     * 자연수인 포인트를 추가하면, 보유량을 그 만큼 증가시켜야 한다.
     */
    @Test
    fun `should increase points`() {
        // given
        val userEntity = PointEntity(11L, 210L, System.currentTimeMillis())
        val amount = 210L

        // when
        val transaction = userEntity.addPoint(amount)

        // then
        Assertions.assertThat(userEntity.point).isEqualTo(420L)
        Assertions.assertThat(transaction.amount).isEqualTo(amount)
        Assertions.assertThat(transaction.type).isEqualTo(TransactionEntityType.ADD)
    }

    /**
     * 음수 포인트를 추가하려고 하면, NegativeAmountException 을 던져야 한다.
     */
    @Test
    fun `should throw NegativeAmountException for negative amount`() {
        // given
        val userEntity = PointEntity(1L, 100L, System.currentTimeMillis())
        val negativeAmount = -210L

        // when & then
        Assertions.assertThatThrownBy { userEntity.addPoint(negativeAmount) }
            .isInstanceOf(NegativeAmountException::class.java)
            .hasMessageContaining("amount 는 0보다 커야합니다.")
    }

    /**
     * 0 포인트를 추가하려고 하면, NegativeAmountException 을 던져야 한다.
     */
    @Test
    fun `should throw NegativeAmountException for zero amount`() {
        // given
        val userEntity = PointEntity(1L, 100L, System.currentTimeMillis())
        val zeroAmount = 0L

        // when & then
        Assertions.assertThatThrownBy { userEntity.addPoint(zeroAmount) }
            .isInstanceOf(NegativeAmountException::class.java)
            .hasMessageContaining("amount 는 0보다 커야합니다.")
    }
}