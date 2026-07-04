package com.farmanimaltimer.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
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
 * Plays a looping per-animal alert plus vibration until [stop] is called.
 *
 * Sound source, in priority order:
 *  1. A real recording in res/raw named after the animal (e.g. res/raw/cow.ogg or
 *     cow.mp3/cow.wav). Looked up by name at runtime, so no file needs to exist at
 *     compile time — drop recordings in later and they take over automatically.
 *  2. Fallback: a synthesized tone pattern (works with zero audio assets).
 */
class AnimalAlert(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.Default)
    private var loopJob: Job? = null
    private var tone: ToneGenerator? = null
    private var player: MediaPlayer? = null

    fun start(animal: Animal, vibrate: Boolean = true) {
        stop()
        maxOutAlarmVolume()
        val vibrator = if (vibrate) obtainVibrator() else null
        val rawId = appContext.resources.getIdentifier(
            animal.name.lowercase(), "raw", appContext.packageName
        )
        if (rawId != 0) {
            playRecording(rawId)
        }
        // Sound loops via MediaPlayer (recording) or ToneGenerator (fallback);
        // vibration loops alongside only when enabled.
        loopJob = scope.launch {
            val gen = if (rawId == 0) ToneGenerator(AudioManager.STREAM_ALARM, 100).also { tone = it } else null
            while (isActive) {
                if (gen != null) {
                    for ((toneType, durMs) in pattern(animal)) {
                        if (!isActive) break
                        if (toneType != 0) gen.startTone(toneType, durMs.toInt())
                        delay(durMs)
                    }
                }
                if (vibrator != null) vibrateOnce(vibrator)
                delay(if (gen != null) 400 else 1000)
            }
        }
    }

    /** Raise the alarm stream to its maximum so the alert is as loud as possible. */
    private fun maxOutAlarmVolume() {
        val am = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        runCatching {
            am.setStreamVolume(
                AudioManager.STREAM_ALARM,
                am.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                0
            )
        }
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
        tone?.release()
        tone = null
        player?.let { runCatching { it.stop() }; it.release() }
        player = null
    }

    private fun playRecording(rawId: Int) {
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            val afd = appContext.resources.openRawResourceFd(rawId)
            afd.use { setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            isLooping = true
            setVolume(1f, 1f)
            setOnPreparedListener { it.start() }
            prepareAsync()
        }
    }

    // (toneType, durationMs) pairs, distinct rhythm per animal.
    private fun pattern(animal: Animal): List<Pair<Int, Long>> = when (animal) {
        Animal.COW -> listOf(ToneGenerator.TONE_DTMF_1 to 600L, 0 to 200L)          // low long "moo"
        Animal.PIG -> listOf(ToneGenerator.TONE_DTMF_5 to 150L, ToneGenerator.TONE_DTMF_5 to 150L, 0 to 150L)
        Animal.CHICKEN -> listOf(ToneGenerator.TONE_DTMF_9 to 100L, ToneGenerator.TONE_DTMF_7 to 100L, 0 to 250L)
        Animal.SHEEP -> listOf(ToneGenerator.TONE_DTMF_3 to 400L, 0 to 250L)
        Animal.HORSE -> listOf(ToneGenerator.TONE_DTMF_2 to 120L, ToneGenerator.TONE_DTMF_4 to 300L, 0 to 200L)
        Animal.DUCK -> listOf(ToneGenerator.TONE_DTMF_6 to 90L, 0 to 60L, ToneGenerator.TONE_DTMF_6 to 90L, 0 to 250L)
        // Ava always ships with a recording, so this fallback tone is never reached.
        Animal.AVA -> listOf(ToneGenerator.TONE_DTMF_0 to 200L, 0 to 200L)
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
