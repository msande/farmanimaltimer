package com.farmanimaltimer

import com.farmanimaltimer.logic.TimeMath
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeMathTest {
    @Test fun fieldsToSeconds_normalCase() {
        assertEquals(3661L, TimeMath.fieldsToSeconds(1, 1, 1))
    }

    @Test fun fieldsToSeconds_maxNonNormalized() {
        // 99h 99m 99s = 99*3600 + 99*60 + 99
        assertEquals(362439L, TimeMath.fieldsToSeconds(99, 99, 99))
    }

    @Test fun clampSeconds_capsAtMax() {
        assertEquals(TimeMath.MAX_TOTAL_SECONDS, TimeMath.clampSeconds(999999L))
    }

    @Test fun clampSeconds_flooring() {
        assertEquals(0L, TimeMath.clampSeconds(-5L))
    }

    @Test fun format_normalizesHhMmSs() {
        assertEquals("01:01:01", TimeMath.format(3661L))
    }

    @Test fun format_zero() {
        assertEquals("00:00:00", TimeMath.format(0L))
    }

    @Test fun format_largeHours() {
        // 362439s -> 100h 40m 39s
        assertEquals("100:40:39", TimeMath.format(362439L))
    }
}
