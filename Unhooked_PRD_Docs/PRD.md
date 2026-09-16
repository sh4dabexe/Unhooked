# Unhooked --- Product Requirements Document

## 1. Product

**App name:** Unhooked\
**Platform:** Android 10+ (API 29+)\
**Form factor:** Phone-first, responsive UI\
**Primary goal:** Help users intentionally control distracting apps,
websites, notifications and total screen time through limits, schedules,
focus sessions and anti-bypass controls.

Unhooked is inspired by the functional category of apps such as Stay
Focused, but the product, UI, naming and implementation should be
original.

## 2. Product principles

-   Fast to configure.
-   Local-first: core blocking must work without an account or cloud.
-   Blocking must be deterministic once required system access is
    enabled.
-   Permissions are requested once, explained before the system screen,
    and their state is continuously verified.
-   The app must clearly distinguish a normal limit from a locked/strict
    commitment.
-   No fake security claims: Android's consumer security model cannot
    make a self-control app literally unbreakable.
-   Responsive design from small Android phones through large
    phones/tablets.
-   The supplied reference image establishes the visual direction: soft
    pastel surface, large rounded cards, compact navigation, generous
    spacing and a clean dashboard.

## 3. Core features

### 3.1 Dashboard

-   Today screen.
-   Total screen time.
-   Remaining overall timer.
-   Focus time.
-   Blocked attempts.
-   Active profile/session.
-   Top distracting apps.
-   Quick Start Focus button.
-   Quick Block button.
-   Daily/weekly toggle.
-   Responsive grid/compact layouts.

### 3.2 App blocker

-   Discover installed launchable apps.
-   Search and category filtering.
-   Select apps for a blocklist.
-   Per-app daily limits.
-   Instant block.
-   Scheduled block.
-   Focus-session block.
-   Optional whitelist for essential apps.
-   Protected system/app exclusions so Unhooked does not accidentally
    block itself or critical Android components.

### 3.3 Website blocker

-   Domain-based blocklist.
-   Browser-aware blocking where technically supported.
-   Optional keyword rules.
-   Study/work allowlist.
-   Website statistics where supported.
-   Clear limitation when a browser cannot expose URL information.

Website blocking should use an architecture appropriate to the
distribution target. Accessibility-based URL inspection must be treated
as a policy-sensitive capability; a VPN/local filtering implementation
can be considered for supported website/domain blocking.

### 3.4 Shorts/Reels/content rules

-   Configurable rules for supported apps/sites.
-   YouTube Shorts rule.
-   Instagram/Facebook Reels rule.
-   Keyword rules.
-   Adult-content domain/keyword rules.
-   Rules are modular so unsupported app UI changes do not break app
    blocking.

### 3.5 Notification blocker

-   Optional notification listener.
-   Block or silence notifications from selected apps during focus
    sessions.
-   Never store notification content unless explicitly required by a
    future feature.
-   Store package name and event timestamp only for statistics.

### 3.6 Focus timer

Modes: - Countdown timer. - Pomodoro. - Stopwatch. - Custom focus +
break cycles.

Timer actions: - Start selected block profile automatically. - Lock
selected settings for the session. - Optional notification/DND
integration. - Completion notification. - Completed-session history.

### 3.7 Overall timer

The overall timer limits total device/app usage represented by the
configured usage source.

Example: - Daily overall limit: 3h. - Whitelisted essential apps do not
consume the limit. - Controlled apps consume the limit. - When the limit
reaches zero, configured blocking activates.

The implementation must define whether the limit is: 1. all device
usage, 2. selected apps only, or 3. all non-whitelisted apps.

Default: **all non-whitelisted app foreground usage**.

### 3.8 Schedules

-   One-time schedule.
-   Daily schedule.
-   Weekday/weekend schedule.
-   Custom days.
-   Multiple schedules.
-   Time-zone safe local scheduling.
-   Conflict resolution: strictest active rule wins.

### 3.9 Anti-bypass

Three user-facing protection modes:

#### Normal Mode

-   User can edit and disable rules.
-   No lock credential required.

#### Password Mode

-   Sensitive rule changes require PIN/password.
-   Password is stored as a salted one-way verifier; never store
    plaintext.
-   Optional biometric confirmation can be added later.
-   The user should be warned that knowing the password means they can
    unlock the system.

#### Admin Protection Mode

-   Uses Android Device Administrator where supported/appropriate to
    make uninstalling the app less convenient.
-   Activation is through Android system UI.
-   The app must detect whether administrator status is active.
-   The app must never claim that Device Admin makes the app impossible
    to remove.

#### Strict Mode

Strict mode combines available protections: - Freeze profile edits until
the active commitment ends. - Prevent disabling the active profile from
inside Unhooked. - Require password for protected actions if Password
Mode is enabled. - Optional strict alarm after repeated bypass
attempts. - Optional admin protection. - Record bypass attempts. - Never
provide a hidden in-app bypass.

Important Android limitation: a normal consumer app cannot guarantee
protection against factory reset, device-owner changes, safe-mode
scenarios, removal of required system access, or other OS-level actions
outside its authority.

## 4. One-time permission onboarding

Create a dedicated **Permissions Center**.

Required/optional system access: - Usage Access --- required for
reliable usage statistics and usage-limit accounting. - Accessibility
Service --- required only for features that use it, such as foreground
app detection/blocking and supported on-screen/browser rules. User
explicitly enables it in Android Settings. - Notification Listener ---
optional; required for notification blocking. - Device Administrator ---
optional; required for Admin Protection. - Notifications --- required on
Android 13+ for timer/status notifications. - Display over other apps
--- only if the selected blocking architecture needs an overlay; do not
request it unnecessarily.

The app must: - Explain each permission before opening Settings. -
Deep-link to the correct Android settings page. - Re-check state when
the user returns. - Mark the permission as complete only after
verification. - Avoid repeatedly prompting for already granted access. -
Provide a Settings \> Permissions page to repair/re-enable access later.

## 5. Block page

When a blocked app is opened: - Show Unhooked blocking screen. - App
name/icon. - Reason: daily limit / schedule / focus session / overall
timer. - Remaining block duration or unlock time. - Optional
motivational message. - Back button must not reveal the blocked app. -
Avoid an easy "disable block" button in Strict Mode. - Provide allowed
actions such as Home and Return to previous safe screen.

## 6. Analytics

-   Today, week and month.
-   Total controlled usage.
-   App-level usage.
-   Focus sessions.
-   Blocked attempts.
-   Time saved estimate based on blocked usage.
-   Streaks.
-   No cloud account required for core analytics.

## 7. Non-functional requirements

-   Android 10+.
-   Portrait and landscape-safe layouts where practical.
-   Material 3-compatible implementation.
-   Dark mode and light mode.
-   Accessibility: adequate contrast, touch targets and content
    descriptions.
-   Offline core operation.
-   No unnecessary network permission.
-   Avoid battery-heavy polling.
-   Prefer event-driven detection and bounded background work.
-   State must survive process death and device reboot where Android
    allows it.
-   All user rules must persist transactionally.

## 8. Success criteria

A build is acceptable only when: - Permissions survive app restart. -
Usage data updates correctly. - A blocked app consistently reaches the
block page. - Daily limits reset at local midnight. - Schedules
activate/deactivate correctly. - Focus timer automatically applies and
removes its profile. - Strict mode cannot be casually disabled from the
main UI. - Admin status is detected correctly. - Password mode survives
restart. - Reboot recovery restores scheduled rules. - The app remains
usable on Android 10 through current Android versions.

## 9. Explicit scope boundary

Do not implement: - hidden surveillance, - collection of private message
contents, - credential/password capture, - stealth persistence, - bypass
of Android security controls, - pretending to be a system component.
