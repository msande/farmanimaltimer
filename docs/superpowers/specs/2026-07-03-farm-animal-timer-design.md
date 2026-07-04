# Farm Animal Timer — Design Spec

Date: 2026-07-03

## Overview

An Android countdown timer app named "Farm Animal Timer". The user picks a farm
animal and a duration (up to 99h 99m 99s), then watches a pie-shaped reveal
gradually uncover a full-color illustration of that animal as time elapses.
When the timer completes, the full animal image is shown along with a sound
and vibration alert.

## Architecture

- Kotlin + Jetpack Compose, single-module Android app.
- `minSdk` 26 (Android 8.0+), `targetSdk` latest stable.
- No backend. `SharedPreferences` stores only the last-used animal and
  duration, to pre-fill the setup screen next time the app opens.
- A foreground `Service` (`TimerService`) owns the countdown, computed from an
  elapsed-realtime end timestamp (not a naive tick loop), so it stays accurate
  across process death, doze, and backgrounding. It shows a persistent
  notification with remaining time and the animal name, and exposes ticks to
  the UI via a `StateFlow`.
- MVVM: a `TimerViewModel` binds Compose UI to the service's state. Compose
  screens are stateless renderers of that state.

## Screens

### Setup Screen

- Animal picker: horizontal row of 6 tappable vector illustrations — Cow,
  Pig, Chicken, Sheep, Horse, Duck. Selected animal is visually highlighted.
- Time display in `HH:MM:SS` format, each field independently editable up to
  99 (so max settable time is 99:99:99, i.e. non-normalized units as
  requested), via tap-to-edit numeric entry.
- `+` / `−` buttons beside the time display applying the proportional
  increment rule (below).
- "Start" button, disabled while total time is 0.

### Countdown Screen

- Circular pie-reveal graphic: a muted/greyed silhouette of the selected
  animal sits underneath a full-color version. A clockwise pie-shaped mask
  reveals more of the full-color image as time elapses, reaching 100%
  revealed exactly at zero.
- Digital time readout (`HH:MM:SS`) overlaid on/under the circle.
- `+` / `−` buttons remain active during countdown, adjusting total time and
  recalculating the reveal fraction proportionally without discontinuous
  jumps in the already-revealed animation.
- Pause/Resume and Cancel controls.

### Done State

- Full-color animal fully revealed (pie at 100%).
- Plays that animal's short synthesized sound jingle on a loop (generated via
  `ToneGenerator`/`SoundPool`-style tone sequences — no external audio
  assets).
- Vibrates the device in a repeating pattern.
- Notification updates to an alert state ("Time's up!").
- Tapping the screen or a "Stop" button silences the alert and returns to the
  Setup screen.

## Increment Logic

Each `+` / `−` tap adjusts time by 10% of the *current* remaining/set time,
then rounds to a friendly unit based on magnitude:

- ≥ 1 hour remaining → round to nearest minute
- ≥ 1 minute remaining → round to nearest 5 seconds
- < 1 minute remaining → round to nearest second, minimum step of 1 second

Result is clamped between 1 second and 99h 99m 99s. This rule applies
identically on the Setup screen and during an active countdown.

## Visual Assets

All 6 animals are drawn as flat vector illustrations directly in code
(Compose `Canvas` / `ImageVector`), each with a matching muted-silhouette
variant for the pre-reveal state. No external image files are required, and
there are no licensing concerns.

## Sound Assets

No external audio files. Each animal gets a short, distinct tone pattern
synthesized in code as a placeholder for a "real" animal sound, played via
`ToneGenerator` or a generated-waveform `SoundPool` clip.

## Background Behavior

The countdown must continue accurately, and still fire the completion alert,
if the user backgrounds the app or locks the screen. Implemented via a
foreground service with a persistent notification, per Android's standard
pattern for reliable long-running timers.

## Testing

- Unit tests for the increment-rounding function and the pie-reveal-fraction
  math (pure functions).
- Manual verification pass on an emulator/device covering: timer accuracy
  across backgrounding, pie animation correctness, and sound/vibration on
  completion.

## Out of Scope (v1)

- Real recorded animal sounds / photographic animal images (placeholders are
  intentional; swapping in real assets later is a drop-in change).
- Multiple simultaneous timers.
- Any cloud sync, accounts, or analytics.
