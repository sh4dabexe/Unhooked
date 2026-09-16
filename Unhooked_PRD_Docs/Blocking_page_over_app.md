# Unhooked --- Blocking Page Over App

## 1. Purpose

The blocking page is the user-facing enforcement screen shown when a
restricted application or supported web target is accessed.

It should feel like a calm interruption, not a punishment.

## 2. Visual hierarchy

``` text
[Unhooked icon]

Stay focused.

[Target App Icon]
Instagram

Blocked because
Daily limit reached

Remaining:
Available again at 7:00 PM

[ motivational message ]

[Go Home]
```

Strict mode:

``` text
[Unhooked icon]

This session is locked.

Instagram
Daily limit reached

Available again at 7:00 PM

Strict Mode is active.

[Go Home]
```

Do not show: - "Disable blocker" - "Edit rule" - hidden settings
shortcut - misleading system dialogs

## 3. Block reasons

Supported reason labels: - Daily limit reached. - Overall timer
exhausted. - Scheduled block. - Focus session active. - Website
blocked. - Keyword blocked. - Reels/Shorts rule active. - Manual instant
block.

## 4. Countdown

If a future unlock time exists:

``` text
02:31:09
```

Use an absolute end timestamp and recompute remaining time.

Never store only a decrementing counter.

## 5. Safe navigation

Preferred: - Home. - Return to previous safe app if known and safe.

Avoid: - launching the blocked app, - opening unrestricted browser
routes, - exposing Android Settings from strict mode.

## 6. Launch architecture

The exact mechanism depends on the blocking implementation.

Possible approach for an Accessibility-based foreground blocker:

``` text
AccessibilityService detects target package
 ↓
Rule engine returns BLOCK
 ↓
start blocking Activity
 ↓
blocking Activity renders target/reason
```

The implementation must be tested across Android 10+ and OEM variants.

## 7. Race-condition handling

When block page opens: - immediately re-check the rule. - if the rule
has expired, finish the block page. - if target is no longer active, do
not unnecessarily relaunch it. - prevent multiple copies of the block
activity.

## 8. Back button

Normal: - Back returns to a safe destination.

Strict: - Back does not expose the blocked app. - If required, finish
the blocking activity and return Home.

## 9. Accessibility

Provide: - content descriptions, - logical focus order, - readable
text, - minimum touch target sizes, - screen-reader-friendly countdown.

## 10. Privacy

The block page only needs: - target package/app label, - reason, -
timing, - rule state.

Do not display or store private content from the blocked app.

## 11. Failure mode

If required detection permission is missing:

``` text
Blocking protection unavailable

Usage/access permission is missing.
Re-enable it to restore enforcement.

[Fix Permission]
```

Never claim "Protected" while required enforcement access is disabled.

## 12. OEM behavior

The blocking page must be tested against: - Pixel/stock Android, -
Samsung, - Xiaomi/HyperOS, - OnePlus/Oppo/Realme.

OEM task killers and battery policies can affect background services.
The app should detect likely service failure and provide a repair guide
instead of silently failing.
