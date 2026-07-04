package com.farmanimaltimer.model

enum class TimerPhase { IDLE, RUNNING, PAUSED, DONE }

/** Immutable snapshot the UI renders. */
data class TimerUiState(
    val phase: TimerPhase = TimerPhase.IDLE,
    val animal: Animal = Animal.COW,
    val totalSeconds: Long = 0L,      // configured duration (setup) or original run length
    val remainingSeconds: Long = 0L,  // live countdown value
) {
    /** Fraction of the animal revealed: 0f at start, 1f at completion. */
    val revealFraction: Float
        get() = when {
            phase == TimerPhase.DONE -> 1f
            totalSeconds <= 0L -> 0f
            else -> (1f - remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
        }
}
