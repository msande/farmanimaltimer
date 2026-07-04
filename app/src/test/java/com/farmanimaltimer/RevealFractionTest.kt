package com.farmanimaltimer

import com.farmanimaltimer.model.Animal
import com.farmanimaltimer.model.TimerPhase
import com.farmanimaltimer.model.TimerUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class RevealFractionTest {
    @Test fun atStart_fractionIsZero() {
        val s = TimerUiState(TimerPhase.RUNNING, Animal.COW, totalSeconds = 100, remainingSeconds = 100)
        assertEquals(0f, s.revealFraction, 0.0001f)
    }

    @Test fun halfway_fractionIsHalf() {
        val s = TimerUiState(TimerPhase.RUNNING, Animal.COW, totalSeconds = 100, remainingSeconds = 50)
        assertEquals(0.5f, s.revealFraction, 0.0001f)
    }

    @Test fun done_fractionIsOne() {
        val s = TimerUiState(TimerPhase.DONE, Animal.COW, totalSeconds = 100, remainingSeconds = 0)
        assertEquals(1f, s.revealFraction, 0.0001f)
    }

    @Test fun zeroTotal_fractionIsZero_noDivideByZero() {
        val s = TimerUiState(TimerPhase.RUNNING, Animal.COW, totalSeconds = 0, remainingSeconds = 0)
        assertEquals(0f, s.revealFraction, 0.0001f)
    }

    @Test fun remainingExceedsTotal_clampsToZero() {
        val s = TimerUiState(TimerPhase.RUNNING, Animal.COW, totalSeconds = 100, remainingSeconds = 150)
        assertEquals(0f, s.revealFraction, 0.0001f)
    }
}
