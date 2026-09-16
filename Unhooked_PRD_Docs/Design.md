# Unhooked --- Design System & Architecture

## 1. Product architecture

Recommended implementation: **Kotlin + Jetpack Compose + AndroidX**.

Core layers:

``` text
UI
 ↓
ViewModel / State
 ↓
Domain rules
 ↓
Repositories
 ↓
Local storage + Android system services
```

Keep the architecture proportional. Do not create dozens of interfaces
and wrappers before the same behavior is reused.

## 2. Main modules

Suggested packages:

``` text
app/
  ui/
    home/
    focus/
    block/
    insights/
    settings/
    permissions/
    blocked/
  domain/
    blocking/
    timer/
    schedule/
    protection/
  data/
    db/
    preferences/
    repository/
  system/
    usage/
    accessibility/
    notifications/
    deviceadmin/
    scheduling/
```

## 3. Source of truth

Room database: - blocking profiles, - app rules, - website rules, -
schedules, - sessions, - usage snapshots, - blocked attempts.

DataStore: - UI preferences, - theme, - onboarding state, - small
settings, - protection mode.

Never store passwords in plaintext.

## 4. Rule model

Every rule should resolve to:

``` text
Rule
- id
- name
- targets
- schedule
- limit
- protectionMode
- enabled
- createdAt
- updatedAt
```

A resolver combines active rules.

Conflict rule: **most restrictive applicable rule wins.**

Example: - Rule A: Instagram 60 min. - Rule B: Instagram blocked
18:00--20:00. - At 18:30: blocked.

## 5. Blocking engine

Inputs: - current foreground package, - current time, - usage
accumulated, - active schedules, - focus session, - overall timer, -
rule state.

Output:

``` text
ALLOW
BLOCK
WARN
```

Pseudo-logic:

``` text
if package is protected:
    ALLOW

active = resolveActiveRules(package, now)

if active contains hard block:
    BLOCK

if app limit exhausted:
    BLOCK

if overall limit exhausted and package is controlled:
    BLOCK

if no active restriction:
    ALLOW
```

## 6. Usage engine

Use `UsageStatsManager` for historical/aggregated usage. Android
requires the user to grant Usage Access through Settings for most
cross-app usage queries.

For immediate foreground detection/blocking, use the selected
system-access mechanism supported by the target distribution and Android
version. Do not assume UsageStats alone gives a real-time callback.

## 7. Accessibility architecture

If AccessibilityService is used: - subscribe only to necessary event
types, - restrict package scope when possible, - do not read/store
unrelated content, - process locally, - stop observing when not
required.

Use it only for legitimate product behavior and ensure the app's
Play-distribution declaration/policy requirements are satisfied.

## 8. Device Admin architecture

Implement: - `DeviceAdminReceiver`. - activation intent. - active-state
check. - deactivation detection. - protected-action state.

Device Admin is a protection layer, not a full device-management
solution. Device Owner / fully managed device controls are a separate
deployment model and must not be silently assumed.

## 9. Timer architecture

Persist: - timer type, - duration, - start timestamp, - end timestamp, -
paused duration, - state.

Calculate remaining time from timestamps rather than decrementing an
in-memory integer every second.

This prevents drift after process death.

## 10. Schedule engine

Represent schedules using local wall-clock rules.

Handle: - midnight, - daylight-saving changes, - device time changes, -
reboot, - timezone changes.

The active state should always be recomputable from persisted rules.

## 11. Block-page architecture

When a restricted target is detected: 1. Resolve current rule. 2. Record
blocked attempt. 3. Launch/route to Unhooked block activity using the
supported Android mechanism. 4. Render the reason. 5. Provide only safe
navigation. 6. Re-check restriction before returning.

Never depend on a single UI trick for security.

## 12. Privacy

Default: - no account, - no server, - no cloud analytics, - local
database, - minimal permissions.

Never collect: - passwords, - private messages, - browser credentials, -
unrelated screen content.

## 13. State machine

``` text
DISABLED
   ↓
ARMED
   ↓
ACTIVE
   ├── PAUSED
   ├── BLOCKING
   └── COMPLETED
```

Permission state is independent:

``` text
MISSING → REQUESTED → GRANTED
                    ↘ DENIED
```

## 14. Reliability

All critical state must be recoverable after: - process death, - app
restart, - device reboot, - configuration change.

Use idempotent state transitions.
