package com.farmanimaltimer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.farmanimaltimer.MainActivity
import com.farmanimaltimer.R
import com.farmanimaltimer.logic.TimeMath
import com.farmanimaltimer.model.Animal
import com.farmanimaltimer.model.TimerPhase
import com.farmanimaltimer.model.TimerUiState
import com.farmanimaltimer.sound.AnimalAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var ticker: Job? = null
    private var endElapsedMs: Long = 0L
    private var pausedRemainingSec: Long = 0L
    private lateinit var alert: AnimalAlert

    override fun onCreate() {
        super.onCreate()
        alert = AnimalAlert(this)
        createChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val animal = Animal.fromNameOrDefault(intent.getStringExtra(EXTRA_ANIMAL))
                val total = intent.getLongExtra(EXTRA_TOTAL, 0L).coerceAtLeast(0L)
                startCountdown(animal, total)
            }
            ACTION_ADJUST -> adjustTo(intent.getLongExtra(EXTRA_TOTAL, currentRemaining()))
            ACTION_PAUSE -> pause()
            ACTION_RESUME -> resume()
            ACTION_CANCEL -> cancelAll()
            ACTION_STOP_ALERT -> stopAlertAndReset()
        }
        return START_STICKY
    }

    private fun startCountdown(animal: Animal, totalSeconds: Long) {
        val total = TimeMath.clampSeconds(totalSeconds)
        endElapsedMs = SystemClock.elapsedRealtime() + total * 1000
        _state.value = TimerUiState(TimerPhase.RUNNING, animal, total, total)
        startForeground(NOTIF_ID, buildNotification(_state.value))
        launchTicker()
    }

    /** Adjust total & remaining by a new remaining value while running/paused. */
    private fun adjustTo(newRemaining: Long) {
        val s = _state.value
        val clamped = TimeMath.clampSeconds(newRemaining)
        // Keep original totalSeconds as the max of old total and new remaining so
        // the reveal fraction stays continuous and never negative.
        val newTotal = maxOf(s.totalSeconds, clamped)
        if (s.phase == TimerPhase.RUNNING) {
            endElapsedMs = SystemClock.elapsedRealtime() + clamped * 1000
        } else {
            pausedRemainingSec = clamped
        }
        _state.value = s.copy(totalSeconds = newTotal, remainingSeconds = clamped)
        updateNotification()
    }

    private fun pause() {
        val s = _state.value
        if (s.phase != TimerPhase.RUNNING) return
        ticker?.cancel()
        pausedRemainingSec = currentRemaining()
        _state.value = s.copy(phase = TimerPhase.PAUSED, remainingSeconds = pausedRemainingSec)
        updateNotification()
    }

    private fun resume() {
        val s = _state.value
        if (s.phase != TimerPhase.PAUSED) return
        endElapsedMs = SystemClock.elapsedRealtime() + pausedRemainingSec * 1000
        _state.value = s.copy(phase = TimerPhase.RUNNING)
        updateNotification()
        launchTicker()
    }

    private fun cancelAll() {
        ticker?.cancel()
        alert.stop()
        _state.value = TimerUiState()
        stopForegroundCompat()
        stopSelf()
    }

    private fun stopAlertAndReset() {
        alert.stop()
        _state.value = TimerUiState()
        stopForegroundCompat()
        stopSelf()
    }

    private fun launchTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val remaining = currentRemaining()
                _state.value = _state.value.copy(remainingSeconds = remaining)
                updateNotification()
                if (remaining <= 0L) {
                    onFinished()
                    break
                }
                delay(250)
            }
        }
    }

    private fun onFinished() {
        val s = _state.value
        _state.value = s.copy(phase = TimerPhase.DONE, remainingSeconds = 0L)
        alert.start(s.animal)
        updateNotification()
    }

    private fun currentRemaining(): Long {
        val s = _state.value
        return if (s.phase == TimerPhase.PAUSED) pausedRemainingSec
        else ((endElapsedMs - SystemClock.elapsedRealtime()) / 1000).coerceAtLeast(0L)
    }

    // ---- Notification ----

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(CHANNEL_ID, "Timer", NotificationManager.IMPORTANCE_LOW)
            ch.setShowBadge(false)
            nm.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(state: TimerUiState): Notification {
        val tapIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = if (state.phase == TimerPhase.DONE) "Time's up! ${state.animal.displayName}"
        else "${state.animal.displayName} timer"
        val text = if (state.phase == TimerPhase.DONE) "Tap to open" else TimeMath.format(state.remainingSeconds)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(state.phase != TimerPhase.DONE)
            .setContentIntent(tapIntent)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, buildNotification(_state.value))
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION") stopForeground(true)
        }
    }

    override fun onDestroy() {
        ticker?.cancel()
        alert.stop()
        super.onDestroy()
    }

    companion object {
        private val _state = MutableStateFlow(TimerUiState())
        val state: StateFlow<TimerUiState> = _state.asStateFlow()

        const val CHANNEL_ID = "timer_channel"
        const val NOTIF_ID = 1
        const val ACTION_START = "start"
        const val ACTION_ADJUST = "adjust"
        const val ACTION_PAUSE = "pause"
        const val ACTION_RESUME = "resume"
        const val ACTION_CANCEL = "cancel"
        const val ACTION_STOP_ALERT = "stop_alert"
        const val EXTRA_ANIMAL = "animal"
        const val EXTRA_TOTAL = "total"

        fun start(context: Context, animal: Animal, totalSeconds: Long) =
            send(context, ACTION_START) {
                putExtra(EXTRA_ANIMAL, animal.name)
                putExtra(EXTRA_TOTAL, totalSeconds)
            }

        fun adjust(context: Context, newRemaining: Long) =
            send(context, ACTION_ADJUST) { putExtra(EXTRA_TOTAL, newRemaining) }

        fun pause(context: Context) = send(context, ACTION_PAUSE) {}
        fun resume(context: Context) = send(context, ACTION_RESUME) {}
        fun cancel(context: Context) = send(context, ACTION_CANCEL) {}
        fun stopAlert(context: Context) = send(context, ACTION_STOP_ALERT) {}

        private inline fun send(context: Context, action: String, block: Intent.() -> Unit) {
            val i = Intent(context, TimerService::class.java).setAction(action).apply(block)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(i)
            else context.startService(i)
        }
    }
}
