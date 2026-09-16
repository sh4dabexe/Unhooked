# Unhooked --- Timer Count / Time Engine

> Filename intentionally follows the requested `Timiercount.md`
> spelling.

## 1. Timer types

### Countdown

User chooses a duration.

Example: `25:00 → 00:00`

### Pomodoro

Example: `25 focus → 5 break → 25 focus → 5 break → long break`

### Stopwatch

Counts upward until stopped.

### Daily overall timer

Tracks eligible usage against a daily allowance.

### Per-app daily timer

Tracks one app against its daily allowance.

## 2. Absolute-time model

Never make the timer depend on:

``` text
counter--
every second
```

Instead:

``` text
startedAt = timestamp
endAt = startedAt + duration
remaining = max(0, endAt - currentTime)
```

This survives process death and reduces timer drift.

## 3. Timer state

``` text
TimerState
- id
- type
- status
- startedAt
- endAt
- pausedAt
- totalPausedMs
- currentCycle
- cycleEndAt
```

Possible status:

``` text
IDLE
RUNNING
PAUSED
COMPLETED
CANCELLED
```

## 4. Countdown calculation

``` text
remainingMs = endAtMs - nowMs
```

Display:

``` text
minutes = remainingMs / 60_000
seconds = (remainingMs / 1_000) % 60
```

Clamp below zero to zero.

## 5. Pause

When paused:

``` text
pausedAt = now
status = PAUSED
```

When resumed:

``` text
pauseDuration = now - pausedAt
endAt += pauseDuration
status = RUNNING
```

Strict mode may disable pause.

## 6. Completion

When:

``` text
now >= endAt
```

then: - mark complete, - apply the next Pomodoro phase if applicable, -
otherwise finish the session, - restore temporary blocking profile, -
record session, - post notification if permission exists.

## 7. Daily reset

Daily limits use local calendar boundaries.

Do not calculate reset as:

``` text
24 hours after first use
```

Default behavior:

``` text
00:00 local time = new usage day
```

If device timezone changes, recompute the local-day boundary.

## 8. Overall timer

Example:

``` text
Daily allowance = 180 min
Used = 137 min

Remaining = 43 min
```

When remaining becomes zero:

``` text
overallBlocked = true
```

Only eligible apps consume the allowance.

Whitelist example: - Phone. - Emergency/critical communication. -
Calculator. - Maps. - User-selected study apps.

The exact default whitelist should be configurable.

## 9. Per-app timer

For an app with:

``` text
limit = 30 min
used = 23 min
```

then:

``` text
remaining = 7 min
```

When usage reaches 30 minutes:

``` text
BLOCK
```

## 10. Usage accounting

Use Android usage statistics for historical accounting. The Android
UsageStats API requires Usage Access for querying other apps.

For active enforcement, combine usage accounting with the selected
foreground detection mechanism.

Avoid double-counting: - aggregate intervals consistently, - use
package + local-day keys, - handle overlapping usage events carefully.

## 11. Timer recovery

On process death:

``` text
load persisted timer
 ↓
if RUNNING:
    calculate from timestamps
 ↓
if end reached:
    complete
else:
    continue
```

No timer data should depend solely on memory.

## 12. Scheduled timer

For a schedule:

``` text
09:00 → 12:00
```

At 10:30: - active.

At 12:00: - inactive.

At 12:00:01: - inactive.

Persist local time rules, not only the next alarm timestamp.

## 13. Android scheduling

Use exact alarms only when product behavior truly requires exact timing
and the Android version/distribution rules allow it.

For non-critical deferred work, prefer WorkManager.

The UI timer itself should calculate from timestamps rather than
depending on an alarm firing every second.

## 14. Timer UI

Large timer:

``` text
25:00
```

Progress:

``` text
██████████░░░░
```

Controls:

``` text
[ Pause ] [ End ]
```

Use large tabular digits so the display does not jump as numbers change.

## 15. Strict timer

When strict focus is enabled: - End may be disabled. - Pause may be
disabled. - Profile edits are locked. - Password requirement applies if
configured. - Commitment end timestamp is authoritative.

The user must see these restrictions before starting the session.

## 16. Edge cases

Handle only realistic cases: - process death, - reboot, - time-zone
change, - user changes clock, - daylight-saving transition, - permission
revoked, - timer already expired, - duplicate start request, - app
update.

Do not add speculative complexity until an actual requirement exists.
