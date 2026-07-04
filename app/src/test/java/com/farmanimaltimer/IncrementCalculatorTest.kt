package com.farmanimaltimer

import com.farmanimaltimer.logic.IncrementCalculator
import com.farmanimaltimer.logic.TimeMath
import org.junit.Assert.assertEquals
import org.junit.Test

class IncrementCalculatorTest {
    // Step = ~10% of current, snapped to nearest ladder value {1,10,30,60,300,600,1800,3600,18000}.

    @Test fun stepSize_twoHours_snapsTo10m() {
        // 2h = 7200s, 10% = 720 -> nearest ladder is 600 (10m)
        assertEquals(600L, IncrementCalculator.stepSize(7200L))
    }

    @Test fun stepSize_oneHour_snapsTo5m() {
        // 1h = 3600s, 10% = 360 -> nearest ladder is 300 (5m)
        assertEquals(300L, IncrementCalculator.stepSize(3600L))
    }

    @Test fun stepSize_tenMinutes_snapsTo1m() {
        // 10m = 600s, 10% = 60 -> exact ladder 60 (1m)
        assertEquals(60L, IncrementCalculator.stepSize(600L))
    }

    @Test fun stepSize_fiveMinutes_snapsTo30s() {
        // 5m = 300s, 10% = 30 -> exact ladder 30
        assertEquals(30L, IncrementCalculator.stepSize(300L))
    }

    @Test fun stepSize_oneMinute_snapsTo10s() {
        // 60s, 10% = 6 -> nearest of {1,10} is 10
        assertEquals(10L, IncrementCalculator.stepSize(60L))
    }

    @Test fun stepSize_twentySeconds_snapsTo1s() {
        // 20s, 10% = 2 -> nearest of {1,10} is 1
        assertEquals(1L, IncrementCalculator.stepSize(20L))
    }

    @Test fun stepSize_zero_isOneSecond() {
        assertEquals(1L, IncrementCalculator.stepSize(0L))
    }

    @Test fun add_increasesByStep() {
        // 2h + 10m
        assertEquals(7800L, IncrementCalculator.add(7200L))
    }

    @Test fun subtract_decreasesByStep_flooredAtZero() {
        // 1s step, floored at zero
        assertEquals(0L, IncrementCalculator.subtract(1L))
    }

    @Test fun add_clampsAtMax() {
        assertEquals(TimeMath.MAX_TOTAL_SECONDS, IncrementCalculator.add(TimeMath.MAX_TOTAL_SECONDS))
    }

    @Test fun stepLabel_matchesInterval() {
        assertEquals("1s", IncrementCalculator.stepLabel(20L))
        assertEquals("10s", IncrementCalculator.stepLabel(60L))
        assertEquals("30s", IncrementCalculator.stepLabel(300L))
        assertEquals("1m", IncrementCalculator.stepLabel(600L))
        assertEquals("5m", IncrementCalculator.stepLabel(3600L))
        assertEquals("10m", IncrementCalculator.stepLabel(7200L))
    }
}
