package com.farmanimaltimer.sound

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.farmanimaltimer.model.Animal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Plays a looping, per-animal tone pattern and vibration until [stop] is called.
 * Tones are synthesized placeholders (no audio assets).
 */
class AnimalAlert(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.Default)
    private var loopJob: Job? = null
    private var tone: ToneGenerator? = null

    // (toneType, durationMs) pairs, distinct rhythm per animal.
    private fun pattern(animal: Animal): List<Pair<Int, Long>> = when (animal) {
        Animal.COW -> listOf(ToneGenerator.TONE_DTMF_1 to 600L, 0 to 200L)          // low long "moo"
        Animal.PIG -> listOf(ToneGenerator.TONE_DTMF_5 to 150L, ToneGenerator.TONE_DTMF_5 to 150L, 0 to 150L)
        Animal.CHICKEN -> listOf(ToneGenerator.TONE_DTMF_9 to 100L, ToneGenerator.TONE_DTMF_7 to 100L, 0 to 250L)
        Animal.SHEEP -> listOf(ToneGenerator.TONE_DTMF_3 to 400L, 0 to 250L)
        Animal.HORSE -> listOf(ToneGenerator.TONE_DTMF_2 to 120L, ToneGenerator.TONE_DTMF_4 to 300L, 0 to 200L)
        Animal.DUCK -> listOf(ToneGenerator.TONE_DTMF_6 to 90L, 0 to 60L, ToneGenerator.TONE_DTMF_6 to 90L, 0 to 250L)
    }

    fun start(animal: Animal) {
        stop()
        val gen = ToneGenerator(AudioManager.STREAM_ALARM, 100).also { tone = it }
        val vibrator = obtainVibrator()
        loopJob = scope.launch {
            while (isActive) {
                for ((toneType, durMs) in pattern(animal)) {
                    if (!isActive) break
                    if (toneType != 0) gen.startTone(toneType, durMs.toInt())
                    delay(durMs)
                }
                vibrateOnce(vibrator)
                delay(400)
            }
        }
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
        tone?.release()
        tone = null
    }

    private fun obtainVibrator(): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    private fun vibrateOnce(vibrator: Vibrator?) {
        vibrator ?: return
        val effect = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.vibrate(CombinedVibration.createParallel(effect))
        } else {
            vibrator.vibrate(effect)
        }
    }
}
