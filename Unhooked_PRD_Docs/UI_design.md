# Unhooked --- UI/UX Design Specification

## 1. Visual direction

The supplied reference image is the visual reference for the overall
language: - very light neutral background, - soft pastel accent
surface, - large rounded cards, - pill controls, - simple line icons, -
large readable headings, - low visual density, - rounded bottom
navigation, - subtle depth rather than hard borders.

Do not copy the reference app's content, branding or exact layout. Use
the visual principles only.

## 2. Design tokens

### Colors

Use semantic tokens rather than hard-coded colors.

-   `bg`: very light neutral/lavender background.
-   `surface`: white or near-white.
-   `surfaceSoft`: pale pastel lavender.
-   `textPrimary`: near-black.
-   `textSecondary`: muted gray.
-   `accent`: soft purple/lavender.
-   `success`: muted green.
-   `warning`: muted amber.
-   `danger`: muted coral/red.
-   `info`: soft blue.

Provide dark-mode equivalents.

### Shape

-   Card radius: 24dp.
-   Large hero card: 28dp.
-   Button radius: 16--20dp.
-   Pills: 999dp.
-   Icon container: 12--16dp.

### Spacing

Use an 8dp base grid: - 8, 12, 16, 20, 24, 32dp.

## 3. Typography

-   Display: bold, large, short.
-   Headline: semibold.
-   Body: regular.
-   Caption: medium, muted.
-   Timer digits: monospaced or tabular numerals.

Avoid excessive text.

## 4. Navigation

Bottom navigation: 1. Home 2. Focus 3. Block 4. Insights 5. Settings

On tablets/large widths, convert to a navigation rail or expanded
navigation.

## 5. Home screen

Top: - greeting, - date, - profile/settings icon.

Hero: - "Today's focus" - total controlled usage, - overall timer
progress.

Cards: - Overall timer. - Focus time. - Blocked attempts. - Top
distractions.

Quick actions: - Start Focus. - Block App. - Schedule.

Use grid on wide screens and a single-column card stack on narrow
screens.

## 6. Focus screen

Large timer card: - 25:00 or selected duration. - progress ring/bar. -
session name.

Controls: - Start. - Pause. - End session. - Skip break where allowed.

Below: - blocked apps count, - active rules, - notification blocking
state.

## 7. Block screen

Sections: - Blocked Apps. - Websites. - Keywords. - Reels/Shorts. -
Notifications.

Each item: - icon, - title, - short rule summary, - enabled state.

Use a search bar and filter chips.

## 8. App picker

-   Search installed apps.
-   Recently used.
-   Categories.
-   Multi-select.
-   Selected count.
-   Confirm button.

Each row: `[icon] App name     [toggle/check]`

## 9. Rule editor

Fields: - Rule name. - Apps/sites. - Mode. - Start/end time. - Days. -
Daily limit. - Grace period if enabled. - Protection mode.

Show a live summary: \> Instagram --- 30 min/day --- Mon--Fri --- Strict

## 10. Anti-bypass screen

Three cards:

### Normal

"Flexible" Edit and disable rules normally.

### Password

"Protected" Rule changes require PIN/password.

### Admin Protection

"Harder to remove" Uses Android Device Administrator.

Then: - Strict Mode toggle. - Active commitment summary. - Permission
status.

Strict Mode should use warning styling and a clear explanation before
activation.

## 11. Permissions Center

Card per permission: - icon, - name, - why needed, - status, - button:
"Enable" / "Repair".

States: - Not required. - Required --- missing. - Enabled. - Temporarily
unavailable. - Unsupported on this device/version.

Never repeatedly show a permission popup if it is already enabled.

## 12. Blocked app page

Full-screen safe screen.

Elements: - Unhooked logo/icon. - "Stay focused." - blocked app
icon/name. - reason. - countdown/unlock time. - optional motivational
text. - Home button. - Back/close.

Strict mode: - no settings shortcut, - no disable button, - no hidden
escape route.

## 13. Insights

Tabs: - Day. - Week. - Month.

Charts: - total screen time, - controlled time, - focus time, - blocked
attempts, - top apps.

Do not overload the screen with charts.

## 14. Settings

Groups: - Protection. - Permissions. - Timer. - Notifications. -
Appearance. - Data. - About.

## 15. Responsive rules

Width classes: - Compact: \<600dp. - Medium: 600--840dp. - Expanded:
\>840dp.

Compact: - single column, - bottom navigation.

Medium: - two-column dashboard cards.

Expanded: - navigation rail, - 2--4 card grid, - wider analytics charts.

Use scalable dimensions, not fixed screen coordinates.

## 16. Motion

-   Short 150--250ms transitions.
-   Progress animations only where meaningful.
-   No excessive bouncing.
-   Respect Android reduced-motion/accessibility preferences.
