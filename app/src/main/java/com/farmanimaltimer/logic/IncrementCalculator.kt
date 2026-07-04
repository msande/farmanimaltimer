package com.farmanimaltimer.logic

import kotlin.math.abs

object IncrementCalculator {
    private const val PERCENT = 0.10

    /** Allowed step sizes, in seconds: 1s, 10s, 30s, 1m, 5m, 10m, 30m, 1h, 5h. */
    private val LADDER = longArrayOf(1, 10, 30, 60, 300, 600, 1800, 3600, 18000)

    /**
     * Step size relative to the current timer: ~10% of [current], snapped to the
     * nearest value on [LADDER] (ties go to the larger step). Never below 1s.
     */
    fun stepSize(current: Long): Long {
        val target = current * PERCENT
        var best = LADDER[0]
        var bestDiff = Double.MAX_VALUE
        for (v in LADDER) {
            val diff = abs(v.toDouble() - target)
            if (diff <= bestDiff) { // <= so equal-distance ties pick the larger (ascending order)
                best = v
                bestDiff = diff
            }
        }
        return best
    }

    fun add(current: Long): Long = TimeMath.clampSeconds(current + stepSize(current))

    fun subtract(current: Long): Long = TimeMath.clampSeconds(current - stepSize(current))

    /** Human label for the current step, e.g. "30s", "5m", "1h". */
    fun stepLabel(current: Long): String = when (val step = stepSize(current)) {
        60L -> "1m"
        300L -> "5m"
        600L -> "10m"
        1800L -> "30m"
        3600L -> "1h"
        18000L -> "5h"
        else -> "${step}s"
    }
}
