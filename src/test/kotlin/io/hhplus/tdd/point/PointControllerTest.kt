package io.hhplus.tdd.point

import io.hhplus.tdd.point.domain.PointEntity
import io.hhplus.tdd.point.domain.TransactionEntity
import io.hhplus.tdd.point.domain.TransactionEntityType
import io.hhplus.tdd.point.repository.PointEntityRepository
import io.hhplus.tdd.point.repository.TransactionEntityRepository
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.reset
import org.mockito.BDDMockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerTest @Autowired constructor(
    @SpyBean private val pointEntityRepository: PointEntityRepository,
    @SpyBean private val transactionEntityRepository: TransactionEntityRepository,
    private val mockMvc: MockMvc
) {
    @AfterEach
    fun afterEach() {
        reset(pointEntityRepository)
        reset(transactionEntityRepository)

        pointEntityRepository.deleteAll()
        transactionEntityRepository.deleteAll()
    }

    /**
     * 사용자와 양수인 포인트 충전을 요청을 받으면,
     * 포인트 충전 후 사용자의 id, 기존 포인트 + 요청 받은 포인트, 충전 시간을 보내주어야 한다.
     */
    @Test
    fun `should charge amount and return 200 ok with charged points`() {
        // given
        val userId = 1L
        val point = 1000L
        val amount = 100L

        val pointEntity = PointEntity(userId, point, System.currentTimeMillis() - 10)
        given(pointEntityRepository.findOrCreateByUserId(userId)).willReturn(pointEntity)

        // when
        val resultActions = mockMvc.perform(
            patch("/point/$userId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(amount.toString())
        )

        // then
        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(point + amount))
            .andExpect(jsonPath("$.updatedMillis").value(greaterThan(pointEntity.updatedMillis)))

        verify(pointEntityRepository).findOrCreateByUserId(userId)
    }

    /**
     * 존재 하지 않는 사용자에 대해서 충전을 요청받았다면,
     * 해당 id 로 새로운 PointEntity 를 만들고, 요청 받은 만큼 충전해서 보내주어야 한다.
     */
    @Test
    fun `should create new PointEntity and return 200 ok with charged point`() {
        // given
        val userId = 2L
        val amount = 400L

        // when
        val resultActions = mockMvc.perform(
            patch("/point/$userId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(amount.toString())
        )

        // then
        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(amount))
            .andExpect(jsonPath("$.updatedMillis").isNumber)
    }

    /**
     * 음수인 포인트로 충전을 요청 받았다면,
     * 충전할 수 없다는 error response 를 status code 400 과 함께 보내주어야 한다.
     */
    @Test
    fun `should return 400 bad request for negative amount`() {
        // given
        val userId = 1
        val amount = -100L

        // when
        val resultActions = mockMvc.perform(
            patch("/point/$userId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(amount.toString())
        )

        // then
        resultActions.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("amount 는 0보다 커야합니다."))
    }

    /**
     * 기존 사용자의 포인트에 대한 조회 요청이 오면,
     * 해당 id 의 UserPoint 정보를 보내주어야 한다.
     */
    @Test
    fun `should return 200 ok with PointEntityDto when getting point`() {
        // given
        val userId = 1L
        val point = 1000L

        val pointEntity = PointEntity(userId, point, System.currentTimeMillis() - 10)
        given(pointEntityRepository.findOrCreateByUserId(userId)).willReturn(pointEntity)

        // when
        val resultActions = mockMvc.perform(
            get("/point/$userId").contentType(MediaType.APPLICATION_JSON)
        )

        // then
        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(point))
            .andExpect(jsonPath("$.updatedMillis").value(pointEntity.updatedMillis))

        verify(pointEntityRepository).findOrCreateByUserId(userId)
    }

    /**
     * 새로운 사용자로 포인트 조회 요청이 오면,
     * 새로운 UserPoint 를 만들어 정보를 보내주어야 한다.
     */
    @Test
    fun `should return 200 ok with new PointEntityDto when getting point with new userId`() {
        // given
        val newUserId = 10L

        // when
        val resultActions = mockMvc.perform(
            get("/point/$newUserId").contentType(MediaType.APPLICATION_JSON)
        )

        // then
        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(newUserId))
            .andExpect(jsonPath("$.point").value(0))
            .andExpect(jsonPath("$.updatedMillis").isNumber)
    }

    /**
     * 충전을 한 적이 있는 사용자의 포인트에 대한 내역 요청이 오면, 충전에 대한 내역이 내려와야한다.
     */
    @Test
    fun `should return 200 ok with transactions associated with id`() {
        // given: TransactionEntity 1개가 주어진 상황
        val userId = 1L
        val transactionEntity = TransactionEntity(
            1L, userId, TransactionEntityType.ADD, 100L, System.currentTimeMillis() - 5
        )
        given(transactionEntityRepository.findAllByUserId(userId)).willReturn(listOf(transactionEntity))

        // when
        val resultActions = mockMvc.perform(
            get("/point/$userId/histories").contentType(MediaType.APPLICATION_JSON)
        )

        // then
        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            // 충전을 1번만 한 상황이므로, 정확히 1개 왔는지 확인
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(transactionEntity.id))
            .andExpect(jsonPath("$[0].userId").value(transactionEntity.userId))
            .andExpect(jsonPath("$[0].type").value(transactionEntity.type.toTransactionType().toString()))
            .andExpect(jsonPath("$[0].amount").value(transactionEntity.amount))
            .andExpect(jsonPath("$[0].timeMillis").value(transactionEntity.timeMillis))

        verify(transactionEntityRepository).findAllByUserId(userId)
    }

    /**
     * 충전 혹은 사용한 적이 없는 사용자의 포인트에 대한 내역 요청이 오면, 빈 내역을 보내주어야 한다.
     */
    @Test
    fun `should return 200 ok with empty list when user has no transaction`() {
        // given
        val userId = 1L

        // when
        val resultActions = mockMvc.perform(
            get("/point/$userId/histories").contentType(MediaType.APPLICATION_JSON)
        )

        // then
        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)
    }

    /**
     * 알 수 없는 사용자의 id 로 내역 요청이 오면, 빈 내역을 보내주어야 한다.
     */
    @Test
    fun `should return 200 ok with empty list about unknown id`() {
        // given
        val unknownId = 10L

        // when
        val resultActions = mockMvc.perform(
            get("/point/$unknownId/histories").contentType(MediaType.APPLICATION_JSON)
        )

        // then
        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)
    }

    /**
     * 사용자가 가지고 있는 point 가 사용하려는 포인트보다 많거나 같다면, use 요청을 받았을 때,
     * 사용하고 남은 point 를 보내주어야 한다.
     */
    @Test
    fun `should return 200 ok with remaining point when point is sufficient`() {
        // given: 1000 포인트를 가진 UserPoint 가 있는 상황
        val userId = 1L
        val point = 1000L
        val pointEntity = PointEntity(userId, point, System.currentTimeMillis() - 10)
        given(pointEntityRepository.findOrCreateByUserId(userId)).willReturn(pointEntity)

        val amount = 999L

        // when
        val resultActions = mockMvc.perform(
            patch("/point/$userId/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(amount.toString())
        )

        // then
        resultActions.andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(point - amount))
            .andExpect(jsonPath("$.updatedMillis").value(greaterThan(pointEntity.updatedMillis)))

        verify(pointEntityRepository).findOrCreateByUserId(userId)
    }

    /**
     * 사용자의 포인트보다 더 많은 양을 사용하려고 한다면,
     * 사용할 수 없다는 error response 를 status code 400 과 함께 내려주어야 한다.
     */
    @Test
    fun `should return 400 bad request for exceed amount`() {
        // given: 100 포인트를 가진 UserPoint 가 1명 있는 상황
        val userId = 11L
        val point = 100L
        val pointEntity = PointEntity(userId, point, System.currentTimeMillis() - 10)
        given(pointEntityRepository.findOrCreateByUserId(userId)).willReturn(pointEntity)

        val amount = 999L

        // when: 가진 포인트보다 더 많은 포인트를 사용하려는 상황
        val resultActions = mockMvc.perform(
            patch("/point/$userId/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(amount.toString())
        )

        // then
        resultActions.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("amount 는 포인트보다 클 수 없습니다."))

        verify(pointEntityRepository).findOrCreateByUserId(userId)
    }

    /**
     * 동시에 여러 요청이 들어온다면, 들어온 순서대로 요청을 처리해야한다.
     */
    @RepeatedTest(10)
    fun `should process the request sequentially when multiple requests come simultaneously`() {
        // given: 충전은 10 포인트, 사용은 1포인트로 정함.
        // expectedPoint 를 AtomicLong 으로 설정하여 Thread Safe 하게 더할 수 있도록 함
        val userId = 21L

        val chargingAmount = 10L
        val usageAmount = 1L

        val expectedPoint = AtomicLong(0)

        val tasks = 10
        val latch = CountDownLatch(tasks)
        val executor = Executors.newFixedThreadPool(10)

        // when: 10개의 task 가 10 개의 thread 에서 동시에 실행
        // 5개는 충전, 5개는 사용 api 를 호출한다.
        for (i in 0..<tasks) {
            executor.submit {
                try {
                    val path = if (i % 2 == 0) "/point/$userId/charge" else "/point/$userId/use"
                    val amount = if (i % 2 == 0) chargingAmount else usageAmount

                    mockMvc.perform(
                        patch(path).contentType(MediaType.APPLICATION_JSON).content(amount.toString())
                    ).andExpect(status().isOk)

                    expectedPoint.addAndGet(if (i % 2 == 0) chargingAmount else -usageAmount)
                } finally {
                    latch.countDown()
                }
            }
        }
        latch.await()
        executor.shutdown()

        // then: expectedPoint 와 모든 api 를 처리하고 난 뒤의 point 와 같아야 한다.
        mockMvc.perform(
            get("/point/$userId").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.point").value(expectedPoint.get()))
    }
}