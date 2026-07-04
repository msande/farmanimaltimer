package com.farmanimaltimer

import com.farmanimaltimer.logic.IncrementCalculator
import com.farmanimaltimer.logic.TimeMath
import org.junit.Assert.assertEquals
import org.junit.Test

class IncrementCalculatorTest {
    @Test fun stepSize_hoursRange_roundsToMinute() {
        // 2h = 7200s, 10% = 720s -> nearest 60 = 720
        assertEquals(720L, IncrementCalculator.stepSize(7200L))
    }

    @Test fun stepSize_hoursRange_roundsToNearestMinute() {
        // 1h5m = 3900s, 10% = 390 -> nearest 60 = 360
        assertEquals(360L, IncrementCalculator.stepSize(3900L))
    }

    @Test fun stepSize_minutesRange_roundsToFive() {
        // 10m = 600s, 10% = 60 -> nearest 5 = 60
        assertEquals(60L, IncrementCalculator.stepSize(600L))
    }

    @Test fun stepSize_minutesRange_roundsToNearestFive() {
        // 90s, 10% = 9 -> nearest 5 = 10
        assertEquals(10L, IncrementCalculator.stepSize(90L))
    }

    @Test fun stepSize_secondsRange_minimumOne() {
        // 5s, 10% = 0.5 -> nearest 1 = 1 (floored to min 1)
        assertEquals(1L, IncrementCalculator.stepSize(5L))
    }

    @Test fun stepSize_zero_minimumOne() {
        assertEquals(1L, IncrementCalculator.stepSize(0L))
    }

    @Test fun add_increasesByStep() {
        assertEquals(7920L, IncrementCalculator.add(7200L))
    }

    @Test fun subtract_decreasesByStep_flooredAtZero() {
        assertEquals(0L, IncrementCalculator.subtract(3L))
    }

    @Test fun add_clampsAtMax() {
        assertEquals(TimeMath.MAX_TOTAL_SECONDS, IncrementCalculator.add(TimeMath.MAX_TOTAL_SECONDS))
    }
}
