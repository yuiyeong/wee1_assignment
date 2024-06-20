package io.hhplus.tdd.point.lock

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

@Component
class PointLockManager {
    private val lockMap = ConcurrentHashMap<Long, ReentrantLock>()

    fun getLock(userId: Long): ReentrantLock {
        // 공정한 락으로 만들어서 순차적으로 처리될 수 있도록 한다.
        return lockMap.computeIfAbsent(userId) { ReentrantLock(true) }
    }
}