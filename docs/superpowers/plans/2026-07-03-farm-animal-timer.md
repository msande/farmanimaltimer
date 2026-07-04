# Farm Animal Timer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an Android countdown timer app where the user picks a farm animal and a duration (up to 99h 99m 99s), watches a pie-shaped reveal gradually uncover a full-color animal illustration as time elapses, and gets a sound + vibration alert with the fully revealed animal when time is up.

**Architecture:** Kotlin + Jetpack Compose single-module app. A foreground `TimerService` owns the countdown using an elapsed-realtime end timestamp (survives backgrounding/doze) and publishes state via a `StateFlow`. A `TimerViewModel` binds Compose screens (Setup, Countdown) to that state. Pure logic (increment rounding, time formatting, pie fraction) lives in small testable files. Animal art is drawn in Compose `Canvas`; sounds are synthesized tone patterns; no external assets.

**Tech Stack:** Kotlin, Jetpack Compose (Material 3), AndroidX Lifecycle/ViewModel, Coroutines/Flow, foreground Service, ToneGenerator, Vibrator, SharedPreferences. JUnit for unit tests.

**Build note:** This environment has no JDK/Android SDK; the user builds and runs in Android Studio at home. No Gradle wrapper is generated (user has Gradle installed). Unit-test "run" steps are executed by the user at home; write the tests as specified.

---

## File Structure

```
farmanimaltimer/
├── settings.gradle.kts
├── build.gradle.kts                 # root: plugin versions via plugins{} apply false
├── gradle.properties
├── .gitignore
└── app/
    ├── build.gradle.kts             # android + compose config, deps
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/farmanimaltimer/
        │   │   ├── MainActivity.kt
        │   │   ├── model/
        │   │   │   ├── Animal.kt            # enum: 6 animals + display name
        │   │   │   └── TimerPhase.kt        # IDLE/RUNNING/PAUSED/DONE + UiState
        │   │   ├── logic/
        │   │   │   ├── TimeMath.kt          # totalSeconds<->HMS, formatting, clamp
        │   │   │   └── IncrementCalculator.kt   # 10% proportional rounding
        │   │   ├── data/
        │   │   │   └── Prefs.kt             # last animal + duration
        │   │   ├── sound/
        │   │   │   └── AnimalAlert.kt       # synth tone + vibration
        │   │   ├── service/
        │   │   │   └── TimerService.kt      # foreground countdown + notification
        │   │   ├── ui/
        │   │   │   ├── TimerViewModel.kt
        │   │   │   ├── AppRoot.kt           # phase-based screen switch
        │   │   │   ├── SetupScreen.kt
        │   │   │   ├── CountdownScreen.kt
        │   │   │   ├── PieReveal.kt         # Canvas reveal composable
        │   │   │   ├── AnimalArt.kt         # Canvas drawings per animal
        │   │   │   └── theme/Theme.kt
        │   │   └── res/values/strings.xml, themes.xml
        │   └── test/java/com/farmanimaltimer/
        │       ├── TimeMathTest.kt
        │       └── IncrementCalculatorTest.kt
```

Responsibilities: `logic/` is pure Kotlin (unit-tested, no Android deps). `service/` owns time truth. `ui/` renders state. `sound/` and `data/` are thin Android wrappers.

---

## Task 1: Gradle project scaffolding

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `.gitignore`
- Create: `app/build.gradle.kts`, `app/proguard-rules.pro`

- [ ] **Step 1: Create `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "FarmAnimalTimer"
include(":app")
```

- [ ] **Step 2: Create root `build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20" apply false
}
```

- [ ] **Step 3: Create `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 4: Create `.gitignore`**

```gitignore
*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/app/build
/captures
.externalNativeBuild
.cxx
```

- [ ] **Step 5: Create `app/build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.farmanimaltimer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.farmanimaltimer"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.02")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")

    testImplementation("junit:junit:4.13.2")
}
```

- [ ] **Step 6: Create empty `app/proguard-rules.pro`** (single comment line)

```proguard
# Keep default rules; app has no reflection-based needs.
```

- [ ] **Step 7: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties .gitignore app/build.gradle.kts app/proguard-rules.pro
git commit -m "chore: gradle project scaffolding"
```

---

## Task 2: `TimeMath` pure logic (TDD)

Handles conversion between total seconds and the non-normalized H/M/S fields (each 0–99), clamping to the 99:99:99 max, and formatting.

**Design decisions:**
- The app's max is literally "99h 99m 99s". Store canonical time as **total seconds**. `MAX_TOTAL_SECONDS = 99*3600 + 99*60 + 99 = 362439`.
- Display always shows normalized `HH:MM:SS` (e.g. 99:99:99 entered normalizes to 100:40:39 → but we cap display at the stored total; see below). To honor the literal "99:99:99" setup UX, the **setup fields** hold raw 0–99 values; on Start they convert to total seconds via `h*3600+m*60+s`. During countdown, display is normalized `HH:MM:SS` from remaining total seconds (hours can exceed 99 only transiently; clamp hours field display to at most 3 digits).

**Files:**
- Create: `app/src/main/java/com/farmanimaltimer/logic/TimeMath.kt`
- Test: `app/src/test/java/com/farmanimaltimer/TimeMathTest.kt`

- [ ] **Step 1: Write failing test `TimeMathTest.kt`**

```kotlin
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
```

- [ ] **Step 2: Run test to verify it fails** — Run (at home): `./gradlew :app:testDebugUnitTest --tests "com.farmanimaltimer.TimeMathTest"` Expected: FAIL (unresolved reference TimeMath).

- [ ] **Step 3: Implement `TimeMath.kt`**

```kotlin
package com.farmanimaltimer.logic

object TimeMath {
    const val MAX_TOTAL_SECONDS: Long = 99L * 3600 + 99L * 60 + 99L // 362439

    fun fieldsToSeconds(h: Int, m: Int, s: Int): Long =
        h.toLong() * 3600 + m.toLong() * 60 + s.toLong()

    fun clampSeconds(total: Long): Long = total.coerceIn(0L, MAX_TOTAL_SECONDS)

    fun format(totalSeconds: Long): String {
        val t = clampSeconds(totalSeconds)
        val h = t / 3600
        val m = (t % 3600) / 60
        val s = t % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }
}
```

- [ ] **Step 4: Run test to verify it passes** — Expected: PASS (all 7).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/farmanimaltimer/logic/TimeMath.kt app/src/test/java/com/farmanimaltimer/TimeMathTest.kt
git commit -m "feat: TimeMath conversion and formatting"
```

---

## Task 3: `IncrementCalculator` pure logic (TDD)

Each +/- tap changes time by 10% of current total, rounded to a friendly unit by magnitude, floored to a 1s minimum step, clamped to [0, MAX].

**Rounding rule:**
- `remaining >= 3600` → round step to nearest 60s
- `remaining >= 60` → round step to nearest 5s
- else → round step to nearest 1s
- Step magnitude = `max(roundedStep, minUnit)` where minUnit is the same granularity (60/5/1). Ensures a tap is never a no-op.
- Applying: `newTotal = clamp(current +/- step)`.

**Files:**
- Create: `app/src/main/java/com/farmanimaltimer/logic/IncrementCalculator.kt`
- Test: `app/src/test/java/com/farmanimaltimer/IncrementCalculatorTest.kt`

- [ ] **Step 1: Write failing test `IncrementCalculatorTest.kt`**

```kotlin
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
```

- [ ] **Step 2: Run test to verify it fails** — Expected: FAIL (unresolved IncrementCalculator).

- [ ] **Step 3: Implement `IncrementCalculator.kt`**

```kotlin
package com.farmanimaltimer.logic

import kotlin.math.roundToLong

object IncrementCalculator {
    private const val PERCENT = 0.10

    fun stepSize(current: Long): Long {
        val unit = when {
            current >= 3600 -> 60L
            current >= 60 -> 5L
            else -> 1L
        }
        val raw = current * PERCENT
        val rounded = (raw / unit).roundToLong() * unit
        return maxOf(rounded, unit).coerceAtLeast(1L)
    }

    fun add(current: Long): Long = TimeMath.clampSeconds(current + stepSize(current))

    fun subtract(current: Long): Long = TimeMath.clampSeconds(current - stepSize(current))
}
```

- [ ] **Step 4: Run test to verify it passes** — Expected: PASS (all 9).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/farmanimaltimer/logic/IncrementCalculator.kt app/src/test/java/com/farmanimaltimer/IncrementCalculatorTest.kt
git commit -m "feat: proportional increment calculator"
```

---

## Task 4: `Animal` model and `TimerPhase`/`UiState`

**Files:**
- Create: `app/src/main/java/com/farmanimaltimer/model/Animal.kt`
- Create: `app/src/main/java/com/farmanimaltimer/model/TimerPhase.kt`

- [ ] **Step 1: Create `Animal.kt`**

```kotlin
package com.farmanimaltimer.model

enum class Animal(val displayName: String) {
    COW("Cow"),
    PIG("Pig"),
    CHICKEN("Chicken"),
    SHEEP("Sheep"),
    HORSE("Horse"),
    DUCK("Duck");

    companion object {
        fun fromNameOrDefault(name: String?): Animal =
            entries.firstOrNull { it.name == name } ?: COW
    }
}
```

- [ ] **Step 2: Create `TimerPhase.kt`**

```kotlin
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
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/farmanimaltimer/model/
git commit -m "feat: Animal and TimerUiState models"
```

---

## Task 5: `Prefs` (last animal + duration)

**Files:**
- Create: `app/src/main/java/com/farmanimaltimer/data/Prefs.kt`

- [ ] **Step 1: Create `Prefs.kt`**

```kotlin
package com.farmanimaltimer.data

import android.content.Context
import com.farmanimaltimer.model.Animal

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("fat_prefs", Context.MODE_PRIVATE)

    var lastAnimal: Animal
        get() = Animal.fromNameOrDefault(sp.getString(KEY_ANIMAL, null))
        set(value) { sp.edit().putString(KEY_ANIMAL, value.name).apply() }

    var lastDurationSeconds: Long
        get() = sp.getLong(KEY_DURATION, 0L)
        set(value) { sp.edit().putLong(KEY_DURATION, value).apply() }

    private companion object {
        const val KEY_ANIMAL = "last_animal"
        const val KEY_DURATION = "last_duration"
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/farmanimaltimer/data/Prefs.kt
git commit -m "feat: SharedPreferences wrapper for last settings"
```

---

## Task 6: `AnimalAlert` (synth sound + vibration)

Each animal gets a distinct short tone sequence via `ToneGenerator`. Vibration via `Vibrator`/`VibratorManager`.

**Files:**
- Create: `app/src/main/java/com/farmanimaltimer/sound/AnimalAlert.kt`

- [ ] **Step 1: Create `AnimalAlert.kt`**

```kotlin
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
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/farmanimaltimer/sound/AnimalAlert.kt
git commit -m "feat: synthesized per-animal alert with vibration"
```

---

## Task 7: `TimerService` (foreground countdown)

Owns the authoritative countdown from an `elapsedRealtime()` end timestamp. Publishes `TimerUiState` via a `StateFlow` on a companion object so the ViewModel/UI can observe without binding. Handles start/pause/resume/cancel via `startService` intents. Posts a persistent notification; on completion fires `AnimalAlert`.

**Files:**
- Create: `app/src/main/java/com/farmanimaltimer/service/TimerService.kt`

- [ ] **Step 1: Create `TimerService.kt`**

```kotlin
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
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/farmanimaltimer/service/TimerService.kt
git commit -m "feat: foreground TimerService with notification and alert"
```

---

## Task 8: `AndroidManifest.xml`, resources, launcher icon

**Files:**
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/res/drawable/ic_timer.xml`
- Create: `app/src/main/res/drawable/ic_launcher_foreground.xml`
- Create: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`

- [ ] **Step 1: Create `AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.FarmAnimalTimer">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.FarmAnimalTimer">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".service.TimerService"
            android:exported="false"
            android:foregroundServiceType="specialUse">
            <property
                android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
                android:value="countdown_timer" />
        </service>
    </application>
</manifest>
```

- [ ] **Step 2: Create `res/values/strings.xml`**

```xml
<resources>
    <string name="app_name">Farm Animal Timer</string>
</resources>
```

- [ ] **Step 3: Create `res/values/colors.xml`**

```xml
<resources>
    <color name="ic_launcher_background">#7EC8A0</color>
</resources>
```

- [ ] **Step 4: Create `res/values/themes.xml`**

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Theme.FarmAnimalTimer" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

- [ ] **Step 5: Create `res/drawable/ic_timer.xml`** (simple vector for the notification)

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFF"
        android:pathData="M12,4a8,8 0,1 0,0.01 0zM13,13h-2V8h2z" />
    <path android:fillColor="#FFFFFF"
        android:pathData="M9,1h6v2h-6z" />
</vector>
```

- [ ] **Step 6: Create `res/drawable/ic_launcher_foreground.xml`** (a simple cow-face glyph on transparent)

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#FFFFFF"
        android:pathData="M36,42m-8,0a8,8 0,1 1,16 0a8,8 0,1 1,-16 0" />
    <path android:fillColor="#FFFFFF"
        android:pathData="M72,42m-8,0a8,8 0,1 1,16 0a8,8 0,1 1,-16 0" />
    <path android:fillColor="#FFFFFF"
        android:pathData="M54,54m-22,0a22,18 0,1 1,44 0a22,18 0,1 1,-44 0" />
    <path android:fillColor="#333333"
        android:pathData="M46,58m-4,0a4,4 0,1 1,8 0a4,4 0,1 1,-8 0" />
    <path android:fillColor="#333333"
        android:pathData="M62,58m-4,0a4,4 0,1 1,8 0a4,4 0,1 1,-8 0" />
</vector>
```

- [ ] **Step 7: Create `res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`** (identical content)

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

- [ ] **Step 8: Commit**

```bash
git add app/src/main/AndroidManifest.xml app/src/main/res/
git commit -m "feat: manifest, resources, launcher icon"
```

---

## Task 9: `AnimalArt` Canvas drawings

Draws each animal as a flat vector illustration in a Compose `Canvas`, parameterized by a `revealFraction` that governs greyscale-vs-color (silhouette below the reveal, full color above). To keep the reveal simple and animal-agnostic, `AnimalArt` draws full color always; the greying is applied by `PieReveal` via a masked overlay (Task 10). `AnimalArt` exposes `DrawScope.drawAnimal(animal, colorful)` where `colorful=false` renders a muted monochrome version.

**Files:**
- Create: `app/src/main/java/com/farmanimaltimer/ui/AnimalArt.kt`

- [ ] **Step 1: Create `AnimalArt.kt`**

```kotlin
package com.farmanimaltimer.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import com.farmanimaltimer.model.Animal

/**
 * Draws a flat illustration of [animal] filling the current DrawScope bounds.
 * When [colorful] is false, uses a muted grey palette (silhouette look).
 * All coordinates are fractions of size, so it scales to any canvas.
 */
fun DrawScope.drawAnimal(animal: Animal, colorful: Boolean) {
    val w = size.width
    val h = size.height
    fun c(full: Color): Color =
        if (colorful) full else Color(0xFF9E9E9E).copy(alpha = 0.55f)

    fun ellipse(cx: Float, cy: Float, rx: Float, ry: Float, color: Color) {
        drawOval(
            color = color,
            topLeft = Offset((cx - rx) * w, (cy - ry) * h),
            size = Size(rx * 2 * w, ry * 2 * h),
            style = Fill
        )
    }
    fun dot(cx: Float, cy: Float, r: Float, color: Color) =
        drawCircle(color, radius = r * minOf(w, h), center = Offset(cx * w, cy * h))

    when (animal) {
        Animal.COW -> {
            ellipse(0.5f, 0.58f, 0.34f, 0.28f, c(Color(0xFFF3E9E1)))   // body
            ellipse(0.5f, 0.44f, 0.20f, 0.17f, c(Color(0xFFFDF7F2)))   // head
            ellipse(0.5f, 0.50f, 0.13f, 0.10f, c(Color(0xFFF6BFC4)))   // snout
            ellipse(0.32f, 0.30f, 0.06f, 0.05f, c(Color(0xFF4A3728)))  // ear
            ellipse(0.68f, 0.30f, 0.06f, 0.05f, c(Color(0xFF4A3728)))
            dot(0.43f, 0.42f, 0.03f, c(Color(0xFF3A2A20)))            // eyes
            dot(0.57f, 0.42f, 0.03f, c(Color(0xFF3A2A20)))
            dot(0.46f, 0.51f, 0.018f, c(Color(0xFF8A4A50)))          // nostrils
            dot(0.54f, 0.51f, 0.018f, c(Color(0xFF8A4A50)))
            ellipse(0.30f, 0.62f, 0.09f, 0.10f, c(Color(0xFF5A4636))) // spot
        }
        Animal.PIG -> {
            ellipse(0.5f, 0.58f, 0.33f, 0.27f, c(Color(0xFFF7B4C4)))
            ellipse(0.5f, 0.44f, 0.20f, 0.17f, c(Color(0xFFF9C6D3)))
            ellipse(0.5f, 0.50f, 0.11f, 0.09f, c(Color(0xFFE79AAE)))
            dot(0.46f, 0.50f, 0.02f, c(Color(0xFF9C5A6E)))
            dot(0.54f, 0.50f, 0.02f, c(Color(0xFF9C5A6E)))
            ellipse(0.34f, 0.30f, 0.06f, 0.06f, c(Color(0xFFE79AAE)))
            ellipse(0.66f, 0.30f, 0.06f, 0.06f, c(Color(0xFFE79AAE)))
            dot(0.43f, 0.42f, 0.025f, c(Color(0xFF3A2A20)))
            dot(0.57f, 0.42f, 0.025f, c(Color(0xFF3A2A20)))
        }
        Animal.CHICKEN -> {
            ellipse(0.5f, 0.60f, 0.28f, 0.26f, c(Color(0xFFFFFFFF)))  // body
            ellipse(0.5f, 0.38f, 0.16f, 0.15f, c(Color(0xFFFFFFFF)))  // head
            ellipse(0.5f, 0.24f, 0.06f, 0.05f, c(Color(0xFFE23B3B)))  // comb
            ellipse(0.62f, 0.42f, 0.05f, 0.035f, c(Color(0xFFF6A623))) // beak
            dot(0.54f, 0.37f, 0.022f, c(Color(0xFF2A2A2A)))          // eye
            ellipse(0.5f, 0.50f, 0.05f, 0.04f, c(Color(0xFFE23B3B)))  // wattle
        }
        Animal.SHEEP -> {
            ellipse(0.5f, 0.58f, 0.34f, 0.29f, c(Color(0xFFF2F2F2)))  // wool body
            ellipse(0.5f, 0.46f, 0.15f, 0.15f, c(Color(0xFF3A3A3A)))  // face
            ellipse(0.36f, 0.44f, 0.05f, 0.07f, c(Color(0xFF3A3A3A))) // ears
            ellipse(0.64f, 0.44f, 0.05f, 0.07f, c(Color(0xFF3A3A3A)))
            dot(0.45f, 0.45f, 0.022f, c(Color(0xFFFFFFFF)))
            dot(0.55f, 0.45f, 0.022f, c(Color(0xFFFFFFFF)))
        }
        Animal.HORSE -> {
            ellipse(0.52f, 0.60f, 0.32f, 0.26f, c(Color(0xFF9B6A43)))  // body
            ellipse(0.40f, 0.40f, 0.15f, 0.20f, c(Color(0xFFA9764C)))  // head/neck
            ellipse(0.36f, 0.42f, 0.05f, 0.06f, c(Color(0xFF6E4A2E)))  // ear
            ellipse(0.32f, 0.30f, 0.10f, 0.10f, c(Color(0xFF4A3323)))  // mane
            dot(0.34f, 0.44f, 0.022f, c(Color(0xFF2A1E14)))           // eye
            ellipse(0.30f, 0.52f, 0.07f, 0.06f, c(Color(0xFF7A5334)))  // muzzle
        }
        Animal.DUCK -> {
            ellipse(0.52f, 0.60f, 0.30f, 0.24f, c(Color(0xFFF7D64B)))  // body
            ellipse(0.40f, 0.42f, 0.15f, 0.14f, c(Color(0xFFF9DE6A)))  // head
            ellipse(0.26f, 0.46f, 0.09f, 0.045f, c(Color(0xFFF3922B))) // bill
            dot(0.40f, 0.40f, 0.022f, c(Color(0xFF2A2A2A)))           // eye
            ellipse(0.62f, 0.60f, 0.14f, 0.12f, c(Color(0xFFEEC63C)))  // wing
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/farmanimaltimer/ui/AnimalArt.kt
git commit -m "feat: Canvas animal illustrations"
```

---

## Task 10: `PieReveal` composable

Draws the animal twice: a muted version fully, then the colorful version clipped to a clockwise pie sweep of `revealFraction * 360°` starting at 12 o'clock. Result: color grows in like a filling pie.

**Files:**
- Create: `app/src/main/java/com/farmanimaltimer/ui/PieReveal.kt`

- [ ] **Step 1: Create `PieReveal.kt`**

```kotlin
package com.farmanimaltimer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import com.farmanimaltimer.model.Animal

/**
 * Circular reveal: muted animal underneath, colorful animal clipped to a
 * clockwise pie sweep proportional to [revealFraction] (0f..1f).
 */
@Composable
fun PieReveal(
    animal: Animal,
    revealFraction: Float,
    diameter: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(diameter)) {
        // Backdrop circle so the art sits on a clean field.
        drawCircle(color = Color(0xFFEFF6F1))

        // Muted (not-yet-revealed) animal.
        drawAnimal(animal, colorful = false)

        val frac = revealFraction.coerceIn(0f, 1f)
        if (frac <= 0f) return@Canvas

        if (frac >= 1f) {
            drawAnimal(animal, colorful = true)
            return@Canvas
        }

        // Pie wedge path starting at 12 o'clock (-90°), sweeping clockwise.
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.width // large enough to cover the whole canvas
        val path = Path().apply {
            moveTo(cx, cy)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = cx - radius, top = cy - radius,
                    right = cx + radius, bottom = cy + radius
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 360f * frac,
                forceMoveTo = false
            )
            close()
        }
        clipPath(path) {
            drawAnimal(animal, colorful = true)
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/farmanimaltimer/ui/PieReveal.kt
git commit -m "feat: pie-reveal composable"
```

---

## Task 11: `TimerViewModel`

Exposes the service `StateFlow` and setup-screen editing state; issues intents to the service.

**Files:**
- Create: `app/src/main/java/com/farmanimaltimer/ui/TimerViewModel.kt`

- [ ] **Step 1: Create `TimerViewModel.kt`**

```kotlin
package com.farmanimaltimer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.farmanimaltimer.data.Prefs
import com.farmanimaltimer.logic.IncrementCalculator
import com.farmanimaltimer.logic.TimeMath
import com.farmanimaltimer.model.Animal
import com.farmanimaltimer.model.TimerPhase
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
) {
    val totalSeconds: Long get() = TimeMath.fieldsToSeconds(hours, minutes, seconds)
}

class TimerViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)

    private val _setup = MutableStateFlow(
        SetupState(animal = prefs.lastAnimal).let { s ->
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
        TimerService.start(getApplication(), s.animal, total)
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

    fun isActive(state: TimerUiState): Boolean =
        state.phase == TimerPhase.RUNNING || state.phase == TimerPhase.PAUSED || state.phase == TimerPhase.DONE
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/farmanimaltimer/ui/TimerViewModel.kt
git commit -m "feat: TimerViewModel bridging setup state and service"
```

---

## Task 12: `SetupScreen`

**Files:**
- Create: `app/src/main/java/com/farmanimaltimer/ui/SetupScreen.kt`

- [ ] **Step 1: Create `SetupScreen.kt`**

```kotlin
package com.farmanimaltimer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmanimaltimer.model.Animal

@Composable
fun SetupScreen(
    setup: SetupState,
    onSelectAnimal: (Animal) -> Unit,
    onSetFields: (Int, Int, Int) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Farm Animal Timer", fontSize = 26.sp)
        Spacer(Modifier.height(8.dp))
        Text("Pick an animal", fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(Animal.entries) { animal ->
                val selected = animal == setup.animal
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (selected) Color(0xFFDFF3E6) else Color(0xFFF2F2F2))
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) Color(0xFF2E9E5B) else Color(0xFFDDDDDD),
                            shape = CircleShape
                        )
                        .clickable { onSelectAnimal(animal) },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Canvas(Modifier.size(56.dp)) {
                        drawAnimal(animal, colorful = true)
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(setup.animal.displayName, fontSize = 18.sp)

        Spacer(Modifier.height(28.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TimeField("H", setup.hours) { onSetFields(it, setup.minutes, setup.seconds) }
            Colon()
            TimeField("M", setup.minutes) { onSetFields(setup.hours, it, setup.seconds) }
            Colon()
            TimeField("S", setup.seconds) { onSetFields(setup.hours, setup.minutes, it) }
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onDecrement) { Text("− time", fontSize = 18.sp) }
            OutlinedButton(onClick = onIncrement) { Text("+ time", fontSize = 18.sp) }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onStart,
            enabled = setup.totalSeconds > 0,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
        ) { Text("Start", fontSize = 20.sp) }
    }
}

@Composable
private fun Colon() = Text(":", fontSize = 34.sp, modifier = Modifier.padding(horizontal = 6.dp))

@Composable
private fun TimeField(label: String, value: Int, onChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { raw ->
                val digits = raw.filter { it.isDigit() }.take(2)
                onChange(digits.toIntOrNull() ?: 0)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 30.sp, textAlign = TextAlign.Center),
            modifier = Modifier.width(84.dp),
        )
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/farmanimaltimer/ui/SetupScreen.kt
git commit -m "feat: setup screen"
```

---

## Task 13: `CountdownScreen`

**Files:**
- Create: `app/src/main/java/com/farmanimaltimer/ui/CountdownScreen.kt`

- [ ] **Step 1: Create `CountdownScreen.kt`**

```kotlin
package com.farmanimaltimer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmanimaltimer.logic.TimeMath
import com.farmanimaltimer.model.TimerPhase
import com.farmanimaltimer.model.TimerUiState

@Composable
fun CountdownScreen(
    state: TimerUiState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onStopAlert: () -> Unit,
) {
    val animatedFraction by animateFloatAsState(
        targetValue = state.revealFraction,
        label = "reveal",
    )
    val done = state.phase == TimerPhase.DONE

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(state.animal.displayName, fontSize = 22.sp)
        Spacer(Modifier.height(16.dp))

        PieReveal(
            animal = state.animal,
            revealFraction = if (done) 1f else animatedFraction,
            diameter = 260.dp,
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = if (done) "Time's up!" else TimeMath.format(state.remainingSeconds),
            fontSize = if (done) 34.sp else 44.sp,
            fontFamily = FontFamily.Monospace,
            color = if (done) Color(0xFF2E9E5B) else Color.Black,
        )

        Spacer(Modifier.height(28.dp))

        if (done) {
            Button(
                onClick = onStopAlert,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
            ) { Text("Stop", fontSize = 20.sp) }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onDecrement) { Text("− time", fontSize = 16.sp) }
                OutlinedButton(onClick = onIncrement) { Text("+ time", fontSize = 16.sp) }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (state.phase == TimerPhase.PAUSED) {
                    Button(onClick = onResume) { Text("Resume") }
                } else {
                    Button(onClick = onPause) { Text("Pause") }
                }
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/farmanimaltimer/ui/CountdownScreen.kt
git commit -m "feat: countdown screen with pie reveal"
```

---

## Task 14: `AppRoot` + `MainActivity`

**Files:**
- Create: `app/src/main/java/com/farmanimaltimer/ui/AppRoot.kt`
- Create: `app/src/main/java/com/farmanimaltimer/MainActivity.kt`

- [ ] **Step 1: Create `AppRoot.kt`**

```kotlin
package com.farmanimaltimer.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.fillMaxSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farmanimaltimer.model.TimerPhase

@Composable
fun AppRoot(vm: TimerViewModel = viewModel()) {
    val timer by vm.timerState.collectAsStateWithLifecycle()
    val setup by vm.setup.collectAsStateWithLifecycle()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (timer.phase == TimerPhase.IDLE) {
                SetupScreen(
                    setup = setup,
                    onSelectAnimal = vm::selectAnimal,
                    onSetFields = vm::setFields,
                    onIncrement = vm::incrementSetup,
                    onDecrement = vm::decrementSetup,
                    onStart = vm::start,
                )
            } else {
                CountdownScreen(
                    state = timer,
                    onIncrement = vm::incrementRunning,
                    onDecrement = vm::decrementRunning,
                    onPause = vm::pause,
                    onResume = vm::resume,
                    onCancel = vm::cancel,
                    onStopAlert = vm::stopAlert,
                )
            }
        }
    }
}
```

Note: `androidx.compose.ui.fillMaxSize` import is wrong; use `androidx.compose.foundation.layout.fillMaxSize`. Correct it in this step:

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
```

- [ ] **Step 2: Create `MainActivity.kt`** (requests POST_NOTIFICATIONS on Android 13+)

```kotlin
package com.farmanimaltimer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.farmanimaltimer.ui.AppRoot

class MainActivity : ComponentActivity() {

    private val requestNotif =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maybeRequestNotifications()
        setContent { AppRoot() }
    }

    private fun maybeRequestNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) requestNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/farmanimaltimer/ui/AppRoot.kt app/src/main/java/com/farmanimaltimer/MainActivity.kt
git commit -m "feat: AppRoot navigation and MainActivity"
```

---

## Task 15: Final wiring review + manual verification checklist

- [ ] **Step 1: Verify package/name consistency** across all files: package `com.farmanimaltimer`, namespace matches, `R` import resolves (`com.farmanimaltimer.R`).

- [ ] **Step 2: Open in Android Studio at home, Gradle sync**, resolve any dependency version nudges Studio suggests.

- [ ] **Step 3: Run unit tests** — Run: `./gradlew :app:testDebugUnitTest` Expected: all TimeMath + IncrementCalculator tests PASS.

- [ ] **Step 4: Manual device/emulator checks:**
  - Setup: pick each animal (art renders distinctly); type 99/99/99 into fields; +/- changes time proportionally.
  - Start → countdown screen shows pie gradually filling with color; time counts down.
  - +/- during countdown nudges remaining time without resetting reveal.
  - Pause/Resume works; Cancel returns to setup.
  - Let it hit zero → full color animal, sound loops, phone vibrates, notification says "Time's up!".
  - Stop silences and returns to setup.
  - Background the app mid-countdown → notification shows live time; alert still fires at zero.
  - Reopen app after a run → last animal + duration pre-filled.

- [ ] **Step 5: Commit any fixes**

```bash
git add -A
git commit -m "fix: wiring and verification adjustments"
```

---

## Self-Review Notes

- **Spec coverage:** setup animal selection (T12), 99:99:99 entry (T2/T12), proportional +/- both setup & running (T3/T11/T12/T13), pie fill reveal (T10/T13), 6 animals as vectors (T9), synth sound + vibration on done (T6/T7), foreground background run + notification (T7/T8), last-settings persistence (T5/T11). All covered.
- **Type consistency:** `TimerUiState.revealFraction`, `IncrementCalculator.add/subtract/stepSize`, `TimeMath.fieldsToSeconds/clampSeconds/format/MAX_TOTAL_SECONDS`, `TimerService.start/adjust/pause/resume/cancel/stopAlert`, `drawAnimal(animal, colorful)`, `PieReveal(animal, revealFraction, diameter)` — names used consistently across tasks.
- **Known Android nuance to watch at build time:** `foregroundServiceType="specialUse"` requires the `FOREGROUND_SERVICE_SPECIAL_USE` permission (declared T8). On some target SDKs Play Store review scrutinizes specialUse; for a personal build it's fine. If Studio flags it, the alternative is no explicit type on minSdk 26 builds. Documented here so the implementer isn't surprised.
