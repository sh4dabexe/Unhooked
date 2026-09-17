# Unhooked — Feature & Fix Specification (for Antigravity)

> Purpose: this file is the single source of truth to hand to Antigravity to (a) fix the
> currently broken/unstable behavior in the existing `Unhooked` Android project and
> (b) add the missing Admin-mode emergency-unlock feature (QR + passphrase) described below.
> It is written against the app's **actual current architecture** (Kotlin, Jetpack Compose,
> Room, DataStore, AccessibilityService, DeviceAdminReceiver) so the agent edits existing
> files instead of inventing a new structure.

---

## 0. Source note

This spec is built from the existing `Unhooked` project itself (`README.md`,
`Unhooked_PRD_Docs/*.md`, and the actual `.kt` source), not from the second file you
uploaded. That second file (`orina-stayfocused`) turned out to be an unrelated old
to-do-list app ("Stay Focused Tasks list" by Elmira Andreeva — add/edit/delete tasks with
a RecyclerView) with no blocking, permissions, scheduling, or admin-mode logic in it at
all. It has nothing usable for this feature, so it was ignored. If you meant to attach a
different reference project for the blocking UI, re-upload it and I'll fold it in.

---

## 1. Current state (what already exists in the repo)

The good news: most of what you described is **already implemented**, just buggy. Antigravity
should fix/stabilize these rather than rewrite from scratch:

| Feature | File(s) | Status |
|---|---|---|
| Usage stats / screen time | `system/usage/UsageStatsHelper.kt`, `ui/home/*` | Present |
| App blocking engine | `domain/engine/RuleResolver.kt`, `domain/engine/TimerEngine.kt` | Present |
| Accessibility-based foreground detection | `system/accessibility/AppBlockerAccessibilityService.kt` | Present |
| Scheduling (blocks with start/end time) | `ui/block/ScheduleContent.kt`, `ui/block/BlockViewModel.kt` | Present |
| Website blocking | `ui/block/WebsiteBlockContent.kt` | Present |
| Full-screen block/interruption page | `ui/blocked/BlockActivity.kt`, `ui/blocked/BlockedScreen.kt` | Present |
| Permissions Center | `ui/permissions/PermissionsScreen.kt`, `PermissionViewModel.kt` | Present |
| Device Admin receiver | `system/deviceadmin/UnhookedAdminReceiver.kt`, `res/xml/device_admin.xml` | Present |
| Protection modes UI (Normal / Password / Admin / Strict) | `ui/settings/AntiBypassScreen.kt` | Present |
| Boot recovery | `system/boot/BootCompletedReceiver.kt` | Present |

**Missing / not implemented anywhere in the code:**
- The QR-code emergency-unlock flow for Admin mode.
- The 50–60 word passphrase emergency-unlock flow for Admin mode.
- Any in-app QR **scanner**.
- Admin mode does not currently lock schedule editing/deletion — it only tracks whether
  Device Admin is granted. The "cannot change or delete anything while Admin mode is on"
  behavior needs to be added (see §4.3).

---

## 2. Permission onboarding (first-open flow)

On first launch, before the dashboard, show a sequential permission flow — never a single
dump of Android system dialogs. Current manifest permissions (`AndroidManifest.xml`):

- `PACKAGE_USAGE_STATS` — Usage Access (for screen-time stats and limits)
- `POST_NOTIFICATIONS` — Android 13+ notifications
- `RECEIVE_BOOT_COMPLETED` — restore active blocks after reboot
- `FOREGROUND_SERVICE` — keep the blocking engine alive
- `SYSTEM_ALERT_WINDOW` — "display over other apps" (Overlay)
- Accessibility Service binding (`BIND_ACCESSIBILITY_SERVICE`) — foreground-app detection for blocking
- Notification Listener binding — optional, for notification blocking
- Device Admin binding (`BIND_DEVICE_ADMIN`) — Admin Protection mode

Rules for the flow (already specified in `PRD.md` §4 — keep this contract):
1. Explain **why** a permission is needed on an in-app card *before* opening the system settings screen.
2. Deep-link straight to the correct Android settings page (Usage Access list, Accessibility list, Overlay/"Display over other apps" list, Notification Listener list, Device Admin list) — not the generic Settings home.
3. Re-check permission state with `onResume()` when the user returns from Settings; mark complete only after verification, never optimistically.
4. Never re-prompt for something already granted.
5. Keep a permanent **Permissions Center** in Settings to repair a revoked permission later (Android periodically resets Usage Access / Accessibility on some OEMs — Xiaomi/HyperOS, OnePlus/Oppo especially).
6. Fix target: if permission re-check is currently flaky or not firing on resume, that's the top bug to chase first — it's the most common reason "the app doesn't work properly."

---

## 3. Blocks: create, schedule, restrict

A **block** = a named rule with: a list of apps/websites, a schedule, and a protection mode.

### 3.1 Creation flow
1. User taps "Create Block."
2. Pick apps (app picker: search, installed-apps list, multi-select, selected count) and/or websites (domain list).
3. Set the schedule:
   - Duration presets: 1 hour, 2 hours, custom/multi-hour, or explicit start–end time.
   - Recurrence: one-time, daily, weekday/weekend, custom days (already scaffolded per `PRD.md` §3.8 — verify `ScheduleContent.kt` actually wires recurrence, not just duration).
4. Choose protection mode for this block: Normal, Password, or Admin (§4).
5. Save.

### 3.2 Enforcement window
- The block **only restricts the selected apps/websites during the scheduled window**. Outside that window they are fully usable — this is a hard requirement, don't let the rule linger active after `endAt`.
- Use an **absolute timestamp** (`endAt = startedAt + duration`), never a decrementing counter, so the block survives process death, reboot, and clock/timezone changes (`TimerEngine.kt` already does this for focus sessions — mirror the same pattern for block schedules if it isn't already shared).
- If two schedules overlap on the same app, the **strictest active rule wins**.

---

## 4. Protection modes

Three modes selectable per block, shown as cards in `AntiBypassScreen.kt` (`ModeCard`).

### 4.1 Normal Mode
- No lock. User can freely edit the schedule, add/remove apps, or delete the block at any time, even mid-window.

### 4.2 Password Mode
- While creating the block, user sets a 6-digit PIN.
- Once the block is active, changing the schedule, extending time, removing apps, or deleting the block **requires entering that PIN** first.
- Store the PIN as a salted one-way hash (SHA-256 + salt), never plaintext — `SecurityUtil.kt` already has this pattern for the settings-level password; reuse it per-block instead of only globally if the block-level requirement isn't wired yet.
- Wrong PIN → reject the edit, no retry-count lockout needed for v1.

### 4.3 Admin Mode (new/expanded behavior)
This is the mode with the biggest gap between what's coded and what you described. Target
behavior:

- Requires Device Admin permission to be granted first (existing `UnhookedAdminReceiver`).
- Once a block is set to Admin mode and scheduled, **all edits are frozen** for the scheduled window:
  - Cannot change schedule/timing.
  - Cannot extend or shorten time.
  - Cannot remove apps from the block.
  - Cannot delete the block.
  - Cannot switch the block back to Normal/Password mode.
- The **only** way to end an Admin-mode block early is one of two emergency-unlock methods, generated at the moment the user turns Admin mode on for that block:

**A. QR code unlock**
- When Admin mode is confirmed, generate a random 256-bit secret tied to that block's ID.
- Render it as a QR code (use `zxing-android-embedded` or `ZXing core` + a Compose `Image` from the generated bitmap) that the user can save to their gallery or share to another device.
- Store only a salted hash of the secret in Room (`RuleDao`/a new `EmergencyUnlockEntity`) — never the raw secret.
- Add an in-app **QR scanner** (CameraX + ML Kit Barcode Scanning, or ZXing's scanner activity) reachable from the Anti-Bypass / blocked screen. Scanning the saved QR decodes the secret, hashes it, compares to the stored hash, and on match disables the Admin lock (and, if the user chooses, the block itself).

**B. Passphrase unlock**
- At the same moment, derive a human-typeable 50–60 word passphrase that encodes the *same* secret (e.g. map the secret's bytes through a fixed wordlist, BIP39-style, at a length that lands in the 50–60 word range — this is deliberately long so it can't be casually memorized/guessed but can still be typed out in an emergency).
- Show it once, with a clear "write this down or store it somewhere safe — it will not be shown again" warning, alongside the QR.
- Add a manual-entry screen (multi-line text field) where typing the correct passphrase (case-insensitive, whitespace-normalized before hashing) unlocks the same way as the QR path.
- Both paths converge on the same "verify secret hash → unlock" function so there's one source of truth, not two parallel unlock implementations.

**Security notes for the agent:**
- Never log the plaintext secret, QR payload, or passphrase.
- Generate the secret with `SecureRandom`, not `Random`/`Math.random`.
- The QR and passphrase are two *encodings* of one secret — don't generate them independently or they'll unlock different things.

---

## 5. Block interruption screen (already exists — keep this contract)

When a restricted app/site is opened during an active window, `BlockActivity` launches
(not a `SYSTEM_ALERT_WINDOW` overlay — the app already deliberately avoids overlay-based
tapjacking per `Blocking_page_over_app.md`, which is correct and should stay that way even
though `SYSTEM_ALERT_WINDOW` is a requested permission for other purposes):

- Blocked app icon + name.
- Reason (schedule / daily limit / focus session / manual block).
- Countdown to unlock, computed from the absolute end timestamp, not a stored counter.
- Optional motivational message.
- Safe exits only: Home, or back to a known-safe previous screen.
- In Admin mode specifically: **no** "disable" shortcut, no settings shortcut — only the QR-scan or passphrase-entry emergency path described in §4.3.
- If required permissions (Usage Access/Accessibility) get revoked, show a "Blocking protection unavailable — re-enable it" state instead of silently failing or falsely claiming protection is active.

---

## 6. Dashboard / home screen data — use real device data

Your current UI issue: Antigravity copied the *numbers* from the reference screenshot
verbatim instead of wiring them to live data. Fix:

- Every stat on the Home screen (`ui/home/HomeScreen.kt`, `HomeViewModel.kt`) must come from
  `UsageStatsHelper.kt` / the local Room analytics tables — total screen time, top distracting
  apps, blocked-attempt count, focus time — not hardcoded placeholder values.
- Keep the reference image only as a **style** reference (pastel palette, card shapes, pill
  charts, spacing) — don't port its literal numbers, app names, or exact card copy into the
  build. Pick individual elements that fit (e.g. the pastel pill consistency chart, the
  segmented Daily/Weekly pill) rather than reproducing the whole screen.
- Daily/Weekly toggle should re-query real data for the selected range, not swap between two static datasets.

---

## 7. UI/UX guidance

- Palette: soft lilac background, white/near-white cards, pastel accent squircles — tokens
  already defined in `ui/theme/Color.kt`; make sure new screens (QR display, scanner,
  passphrase entry) pull from those tokens instead of hardcoded hex values.
- Shapes: 24dp card radius, 16dp icon squircles, 999dp pills — already in `ui/theme/Shape.kt`.
- Responsive: single-column card stack on compact width, 2-column grid on medium/expanded
  width (`UI_design.md` §15) — verify this is actually applied with `WindowSizeClass`, not
  just assumed.
- Motion: short 150–250ms transitions only; respect reduced-motion.
- Timer/countdown digits: tabular/monospaced numerals so layout doesn't jitter as digits change.

---

## 8. Acceptance checklist (what "fixed" means)

- [ ] Permission state is correctly re-verified after returning from every Settings deep link (Usage Access, Accessibility, Overlay, Notification Listener, Device Admin).
- [ ] A block only restricts its apps/sites strictly between `startedAt` and `endAt`; it's fully open outside that window.
- [ ] Password-mode PIN is required for edit/delete/extend and survives app restart.
- [ ] Admin-mode blocks reject all in-app edits/deletes until unlocked.
- [ ] QR-generation → save/share → scan → unlock round-trip works.
- [ ] Passphrase-generation → manual entry → unlock round-trip works, and unlocks the *same* lock the matching QR would.
- [ ] Neither the QR secret nor the passphrase is ever stored or logged in plaintext.
- [ ] `BlockActivity` shows correct app/reason/countdown and has no hidden disable path in Admin/Strict mode.
- [ ] Home screen stats are 100% live device data — no leftover placeholder numbers from the reference image.
- [ ] Layout is responsive across compact/medium/expanded width classes and survives rotation.
- [ ] Rules and unlock state survive process death and device reboot.

---

## 9. Open questions for you (resolve before/while building)

1. Should Admin mode's "freeze everything" behavior fully replace the existing separate
   "Strict Mode" card in `AntiBypassScreen.kt`, or should Strict Mode stay as a 4th,
   even-harder option layered on top of Admin? Right now your description of Admin mode
   overlaps almost entirely with what the code already calls Strict Mode, just without the
   QR/passphrase escape hatch.
2. For website blocking specifically — is Accessibility-based URL inspection acceptable for
   your distribution target (sideload/APK) or do you need the VPN/local-filtering fallback
   `PRD.md` mentions? This affects whether website blocks can honor Admin-mode locking the
   same way app blocks do.
