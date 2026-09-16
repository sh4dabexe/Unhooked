# Unhooked --- User & System Flows

## 1. First launch

``` text
Launch
 ↓
Welcome
 ↓
Choose goal
  ├─ Study
  ├─ Work
  └─ Digital detox
 ↓
Permission Center
 ↓
Usage Access
 ↓
Accessibility (only if required)
 ↓
Notifications
 ↓
Optional Notification Listener
 ↓
Optional Admin Protection
 ↓
Create first profile
 ↓
Select apps
 ↓
Set limit/schedule
 ↓
Dashboard
```

## 2. App blocking flow

``` text
User opens controlled app
        ↓
Foreground detection
        ↓
Read current rule state
        ↓
Is target protected?
   ├─ No → Allow
   └─ Yes
       ↓
   Log blocked attempt
       ↓
   Block page
       ↓
   User returns Home / safe app
```

## 3. Daily limit flow

``` text
Usage query
 ↓
Get today's app usage
 ↓
Add current eligible usage
 ↓
Compare with rule limit
 ↓
limit remaining > 0
 ├─ yes → allow
 └─ no → block
```

## 4. Overall timer flow

``` text
Day starts
 ↓
Overall limit = X
 ↓
Eligible controlled usage accumulates
 ↓
remaining = X - used
 ↓
remaining reaches 0
 ↓
overall restriction active
 ↓
controlled apps → Block page
```

## 5. Focus flow

``` text
Focus
 ↓
Choose duration/profile
 ↓
Confirm
 ↓
Start timestamp saved
 ↓
Active profile enabled
 ↓
Blocked apps/sites enforced
 ↓
Timer reaches end
 ↓
Profile restored
 ↓
Session saved
 ↓
Completion notification
```

## 6. Pomodoro flow

``` text
Focus 25m
 ↓
Short break 5m
 ↓
Focus 25m
 ↓
Short break 5m
 ↓
...
 ↓
Long break
```

Durations are configurable.

## 7. Password mode

``` text
Protected action
 ↓
Password screen
 ↓
Correct?
 ├─ no → remain locked
 └─ yes → perform action
```

Do not reveal whether a stored password hash exists or expose secrets.

## 8. Strict mode

``` text
Create/edit profile
 ↓
Enable Strict
 ↓
Show commitment warning
 ↓
Confirm
 ↓
Lock protected configuration
 ↓
Active until end timestamp
 ↓
Normal edit controls disabled
 ↓
End timestamp reached
 ↓
Unlock configuration
```

## 9. Admin protection

``` text
Settings
 ↓
Admin Protection
 ↓
Explain capability
 ↓
Android Device Admin activation screen
 ↓
User activates
 ↓
Return to app
 ↓
Verify active
 ↓
Show Protected state
```

## 10. Permission repair

``` text
Dashboard
 ↓
Permission warning
 ↓
Permissions Center
 ↓
Missing permission
 ↓
Explain
 ↓
Open Android Settings
 ↓
User changes permission
 ↓
Return
 ↓
Verify
 ↓
Update status
```

## 11. Permission revocation while blocking

``` text
Required permission revoked
 ↓
Detector notices
 ↓
Mark affected features unavailable
 ↓
Notify user
 ↓
Open repair action
```

Do not silently pretend that blocking is still guaranteed.

## 12. Reboot flow

``` text
Device boots
 ↓
App receives allowed boot signal
 ↓
Load persisted rules
 ↓
Check current time
 ↓
Resolve active schedules
 ↓
Verify system access
 ↓
Continue enforcement if supported
```

## 13. Conflict flow

Example:

``` text
Rule A: YouTube 60m/day
Rule B: YouTube blocked 18:00–20:00
Rule C: Focus session blocks YouTube
```

At 18:30 during focus:

``` text
A = limit
B = block
C = block

Result = BLOCK
```

After 20:00 with focus still active:

``` text
A = limit
B = inactive
C = block

Result = BLOCK
```

After focus ends and limit remains:

``` text
A = limit
B = inactive
C = inactive

Result = ALLOW until limit is exhausted
```
