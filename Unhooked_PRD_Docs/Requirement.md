# Unhooked --- Requirements

## 1. Functional requirements

### FR-01 Dashboard

The app shall display current usage, focus time, overall timer and
active protection state.

### FR-02 App selection

The app shall list installed launchable applications and allow users to
select controlled apps.

### FR-03 App blocking

The app shall block selected apps when an applicable rule is active.

### FR-04 Daily limit

The app shall support a configurable per-app daily usage limit.

### FR-05 Overall timer

The app shall support a daily overall usage limit for
controlled/non-whitelisted apps.

### FR-06 Scheduling

The app shall support recurring and one-time schedules.

### FR-07 Focus sessions

The app shall start a focus session with a selected block profile.

### FR-08 Pomodoro

The app shall support focus and break cycles.

### FR-09 Website blocking

The app shall support domain/URL blocking through a technically
supported architecture.

### FR-10 Keyword rules

The app shall support keyword rules where the selected technical access
can reliably expose the required text.

### FR-11 Notification blocking

The app shall optionally suppress notifications from selected packages
during active sessions.

### FR-12 Blocked page

The app shall show a blocking screen whenever a configured target is
blocked.

### FR-13 Normal mode

The user shall be able to modify active rules normally.

### FR-14 Password mode

Protected configuration changes shall require the configured credential.

### FR-15 Admin protection

The app shall provide an optional Device Administrator activation flow.

### FR-16 Strict mode

Strict mode shall lock the selected configuration for the defined
commitment period and remove normal in-app disable/edit paths.

### FR-17 Permission center

The app shall show permission state and repair links.

### FR-18 Permission onboarding

Each required system permission shall be explained and requested through
its proper Android settings flow.

### FR-19 Persistence

Rules and timer state shall survive process death and normal device
reboot where Android permits.

### FR-20 Analytics

The app shall show daily/weekly/monthly usage and blocked attempts.

## 2. Non-functional requirements

### NFR-01 Compatibility

Minimum API 29.

### NFR-02 Responsive UI

The UI shall adapt to compact, medium and expanded widths.

### NFR-03 Performance

The app shall avoid continuous high-frequency polling.

### NFR-04 Battery

Blocking must use event-driven/system-supported mechanisms where
possible.

### NFR-05 Privacy

Core functionality shall work without a server account.

### NFR-06 Security

Credentials shall never be stored in plaintext.

### NFR-07 Accessibility

Controls must have labels and usable touch targets.

### NFR-08 Recovery

The app shall recover active schedules after process death/reboot.

### NFR-09 Permission state

Permission state shall be revalidated when returning from Android
Settings.

### NFR-10 Testability

Blocking rules, timers and schedules shall be testable without depending
entirely on real clock time.

## 3. Permission requirements

  -----------------------------------------------------------------------
  Capability              Android mechanism       Required?
  ----------------------- ----------------------- -----------------------
  App usage statistics    Usage Access /          Yes
                          `PACKAGE_USAGE_STATS`   

  Foreground/blocking     AccessibilityService    Yes for those features
  detection               where selected          
                          architecture requires   
                          it                      

  Notification blocking   Notification Listener   Optional

  Admin protection        Device Administrator    Optional

  Timer/status            POST_NOTIFICATIONS on   Recommended/required
  notifications           Android 13+             for notification
                                                  feature

  Overlay                 SYSTEM_ALERT_WINDOW     Only if implementation
                                                  needs it
  -----------------------------------------------------------------------

Permissions must never be silently requested.

## 4. Anti-bypass requirements

The system must protect: - active rule edits, - disabling active
profile, - removing password protection, - removing admin protection
where possible, - accidental deletion of rules.

The system must explicitly document limits: - Device Admin is not Device
Owner. - A normal app cannot prevent factory reset. - A user can revoke
system permissions through Android Settings. - OEM battery management
can affect background behavior.

## 5. Acceptance criteria

A feature is complete only when: - it works on API 29, - it works after
app restart, - it handles permission revocation, - it has a clear
empty/error state, - it has a responsive UI, - it has no unnecessary
permission, - it is covered by at least one automated test where
practical.
