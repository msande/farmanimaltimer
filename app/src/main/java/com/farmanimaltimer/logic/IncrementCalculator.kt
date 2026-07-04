package com.farmanimaltimer.logic

import kotlin.math.roundToLong

object IncrementCalculator {
    private const val PERCENT = 0.10

    fun stepSize(current: Long): Long {
        val unit = when {
            current >= 3600 -> 60L
            current >= 60 -> 5L
            else -> 1L
        }
        val raw = current * PERCENT
        val rounded = (raw / unit).roundToLong() * unit
        return maxOf(rounded, unit).coerceAtLeast(1L)
    }

    fun add(current: Long): Long = TimeMath.clampSeconds(current + stepSize(current))

    fun subtract(current: Long): Long = TimeMath.clampSeconds(current - stepSize(current))
}
