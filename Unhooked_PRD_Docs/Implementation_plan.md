# Unhooked --- Implementation Plan

## Phase 0 --- Project setup

-   Kotlin.
-   Jetpack Compose.
-   Material 3.
-   Android minSdk 29.
-   Target current stable Android SDK.
-   Room.
-   DataStore.
-   Lifecycle/ViewModel.
-   Navigation Compose.
-   Coroutines.
-   WorkManager only where suitable for deferrable background work.

Do not add a library unless the standard AndroidX/Kotlin implementation
is insufficient.

## Phase 1 --- Design system

Implement: - color tokens, - typography, - spacing, - shapes, -
buttons, - cards, - chips, - progress components, - navigation, - dark
mode.

Match the supplied visual direction without cloning its exact UI.

## Phase 2 --- Data layer

Create Room entities: - `BlockProfile` - `AppRule` - `WebsiteRule` -
`Schedule` - `FocusSession` - `UsageSnapshot` - `BlockedAttempt`

DataStore: - theme, - onboarding, - protection mode, - small
preferences.

## Phase 3 --- Permission center

Build one permission coordinator.

Flow:

``` text
First launch
 ↓
Explain permissions
 ↓
Usage Access
 ↓
Accessibility (if required by selected features)
 ↓
Notifications (Android 13+)
 ↓
Notification Listener (if enabled)
 ↓
Device Admin (if Admin Protection selected)
 ↓
Verification
 ↓
Ready
```

Every return from Settings must call `refreshPermissionState()`.

Do not ask again when the state is already granted.

## Phase 4 --- Usage engine

Implement a `UsageRepository`.

Responsibilities: - query UsageStatsManager, - convert package usage
into app-day usage, - calculate overall controlled usage, - handle
local-day reset, - cache results, - expose usage to UI.

Use timestamps for calculations.

## Phase 5 --- Rule engine

Implement: - active schedule resolution, - per-app limits, - overall
timer, - focus-session restrictions, - whitelist, - conflict resolution.

Unit-test the resolver heavily.

## Phase 6 --- Blocking service

Implement the selected Android mechanism for foreground detection.

If AccessibilityService is used: - minimal event types, - package
filtering, - no unrelated content storage, - local decision engine.

Flow:

``` text
Foreground package event
        ↓
RuleResolver
        ↓
ALLOW / BLOCK
        ↓
if BLOCK
  log attempt
  show BlockPage
```

## Phase 7 --- Block page

Implement a dedicated Activity/route.

It must: - identify target app, - explain the active rule, - show
remaining time, - respect strict mode, - offer safe navigation.

## Phase 8 --- Timer

Implement timer using absolute timestamps:

``` text
endAt = startAt + duration
remaining = max(0, endAt - now)
```

Do not rely on decrementing an integer every second.

Persist timer state.

Implement: - countdown, - Pomodoro, - stopwatch, - break cycles.

## Phase 9 --- Anti-bypass

Implement in this order:

1.  Normal Mode.
2.  Password Mode.
3.  Strict Mode.
4.  Device Admin activation.
5.  Recovery/revocation detection.

Password: - salted hash, - constant-time comparison where appropriate, -
lockout/backoff after repeated failures if desired.

Strict mode: - freeze protected settings, - prevent in-app disabling, -
require password if configured, - persist commitment end timestamp.

## Phase 10 --- Website and content blocking

Start with domain rules.

For browser-aware URL/content blocking: - choose an architecture after
distribution requirements are decided, - document limitations per
browser, - never pretend unsupported browsers are covered.

## Phase 11 --- Notification blocker

Only request Notification Listener when user enables notification
blocking.

Store minimal metadata.

## Phase 12 --- Analytics

Implement: - daily, - weekly, - monthly aggregation, - top apps, -
blocked attempts, - focus sessions.

## Phase 13 --- Reboot/recovery

Register only required boot handling.

On boot: - reload active schedules, - verify permissions, - restore
timer state from timestamps, - do not start unnecessary background
loops.

## Phase 14 --- Testing

### Unit

-   rule resolver,
-   timer math,
-   schedule overlap,
-   daily reset,
-   password verifier,
-   overall timer.

### Instrumentation

-   permission flow,
-   navigation,
-   database persistence.

### Device tests

At minimum: - Android 10, - Android 11, - Android 12, - Android 13, -
Android 14, - Android 15/current supported release.

Test OEM behavior separately for Samsung/Xiaomi/OnePlus/Pixel where
possible.

## Phase 15 --- Hardening

-   permission revocation.
-   battery optimization.
-   reboot.
-   date/time changes.
-   timezone changes.
-   app update.
-   process death.
-   multiple overlapping profiles.
-   strict mode expiry.
-   invalid/empty rules.

## Phase 16 --- Release

Before release: - privacy policy, - permission explanations, -
accessibility-service justification if used, - Play policy review, -
data safety declaration, - crash reporting decision, - backup behavior.

If Play Store distribution is intended, validate AccessibilityService
usage against current Google Play policy before publishing.
