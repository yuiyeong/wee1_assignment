package io.hhplus.tdd.point.exception

open class PointException(message: String) : Exception(message) {
    fun message() = message!!
}