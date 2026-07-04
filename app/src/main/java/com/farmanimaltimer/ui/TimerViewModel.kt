package com.farmanimaltimer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.farmanimaltimer.data.Prefs
import com.farmanimaltimer.logic.IncrementCalculator
import com.farmanimaltimer.logic.TimeMath
import com.farmanimaltimer.model.Animal
import com.farmanimaltimer.model.TimerUiState
import com.farmanimaltimer.service.TimerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Editable state for the setup screen (raw 0..99 fields). */
data class SetupState(
    val animal: Animal = Animal.COW,
    val hours: Int = 0,
    val minutes: Int = 5,
    val seconds: Int = 0,
    val vibrate: Boolean = true,
) {
    val totalSeconds: Long get() = TimeMath.fieldsToSeconds(hours, minutes, seconds)
}

class TimerViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)

    private val _setup = MutableStateFlow(
        SetupState(animal = prefs.lastAnimal, vibrate = prefs.vibrateEnabled).let { s ->
            val saved = prefs.lastDurationSeconds
            if (saved > 0) {
                val h = (saved / 3600).toInt().coerceAtMost(99)
                val m = ((saved % 3600) / 60).toInt()
                val sec = (saved % 60).toInt()
                s.copy(hours = h, minutes = m, seconds = sec)
            } else s
        }
    )
    val setup: StateFlow<SetupState> = _setup.asStateFlow()

    /** Live timer state from the service. */
    val timerState: StateFlow<TimerUiState> = TimerService.state

    // ---- Setup editing ----

    fun selectAnimal(animal: Animal) { _setup.value = _setup.value.copy(animal = animal) }

    fun setVibrate(enabled: Boolean) { _setup.value = _setup.value.copy(vibrate = enabled) }

    fun setFields(h: Int, m: Int, s: Int) {
        _setup.value = _setup.value.copy(
            hours = h.coerceIn(0, 99),
            minutes = m.coerceIn(0, 99),
            seconds = s.coerceIn(0, 99),
        )
    }

    fun incrementSetup() = applySetupTotal(IncrementCalculator.add(_setup.value.totalSeconds))
    fun decrementSetup() = applySetupTotal(IncrementCalculator.subtract(_setup.value.totalSeconds))

    private fun applySetupTotal(total: Long) {
        val t = TimeMath.clampSeconds(total)
        _setup.value = _setup.value.copy(
            hours = (t / 3600).toInt().coerceAtMost(99),
            minutes = ((t % 3600) / 60).toInt(),
            seconds = (t % 60).toInt(),
        )
    }

    // ---- Timer control ----

    fun start() {
        val s = _setup.value
        val total = s.totalSeconds
        if (total <= 0L) return
        prefs.lastAnimal = s.animal
        prefs.lastDurationSeconds = total
        prefs.vibrateEnabled = s.vibrate
        TimerService.start(getApplication(), s.animal, total, s.vibrate)
    }

    fun incrementRunning() {
        val cur = timerState.value.remainingSeconds
        TimerService.adjust(getApplication(), IncrementCalculator.add(cur))
    }

    fun decrementRunning() {
        val cur = timerState.value.remainingSeconds
        TimerService.adjust(getApplication(), IncrementCalculator.subtract(cur))
    }

    fun pause() = TimerService.pause(getApplication())
    fun resume() = TimerService.resume(getApplication())
    fun cancel() = TimerService.cancel(getApplication())
    fun stopAlert() = TimerService.stopAlert(getApplication())
}
