package io.hhplus.tdd.point

import io.hhplus.tdd.point.domain.PointEntity
import io.hhplus.tdd.point.repository.PointEntityRepository
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.AfterEach
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

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerTest @Autowired constructor(
    @SpyBean private val pointEntityRepository: PointEntityRepository,
    private val mockMvc: MockMvc
) {
    @AfterEach
    fun afterEach() {
        reset(pointEntityRepository)

        pointEntityRepository.deleteAll()
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
}