# Farm Animal Timer

An Android countdown timer. Pick a farm animal and a duration (up to 99h 99m 99s),
then watch a pie-shaped reveal gradually uncover a full-color illustration of that
animal as time counts down. When the timer hits zero, the full animal is shown with
a looping sound and vibration until you dismiss it.

## Features

- Six selectable animals (Cow, Pig, Chicken, Sheep, Horse, Duck), drawn in code — no image assets.
- Set time up to 99:99:99 via per-field entry.
- `+` / `−` buttons adjust time by ~10% of the current value (rounded to a friendly
  unit), on both the setup screen and during a running countdown.
- Clockwise pie "fill" reveal of the animal, reaching 100% exactly at zero.
- Synthesized per-animal alert tone + vibration on completion (placeholder sounds).
- Runs as a foreground service with a persistent notification, so the countdown stays
  accurate and still alerts if the app is backgrounded or the screen is locked.
- Remembers the last animal and duration between launches.

## Building

Requires Android Studio (or a local Gradle + Android SDK). This repo intentionally
has **no Gradle wrapper** — use your local Gradle, or just open the folder in Android
Studio and let it sync.

```bash
# Run unit tests (pure logic: TimeMath, IncrementCalculator, revealFraction)
gradle :app:testDebugUnitTest

# Build a debug APK
gradle :app:assembleDebug
```

In Android Studio: **Open** this folder, wait for Gradle sync, then Run on an
emulator or device (minSdk 26 / Android 8.0+).

## Notes

- Animal sounds are short synthesized tone patterns (via `ToneGenerator`) standing in
  for real recordings. To use real audio, drop files in `res/raw/` and swap the tone
  logic in `sound/AnimalAlert.kt`.
- The design spec and implementation plan live under `docs/superpowers/`.
