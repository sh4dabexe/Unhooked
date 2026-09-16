# Unhooked --- Coding Style

## 1. Core principle

Write code like an experienced developer working inside a known
codebase:

**context first, abstraction second.**

Do not generate generic architecture merely because it is considered
"best practice".

## 2. Abstraction control

Use the **Rule of Three**.

If logic appears once: - keep it local.

If it appears twice: - consider duplication vs extraction.

If it appears three times: - extract a shared abstraction when it
genuinely improves the code.

Do not create: - one-method interfaces, - wrapper classes around a
single Android API, - repositories that only forward one call, -
factories with one implementation, unless the framework/test boundary
genuinely needs them.

Preferred instruction for generated code:

> Don't abstract unless reused or required by a real boundary. Inline
> simple logic.

## 3. Comments

Comments explain **why**, not what.

Bad:

``` kotlin
// Increment counter by one
count++
```

Good:

``` kotlin
// Keep the timestamp as the source of truth so the timer survives process death.
```

Avoid tutorial-style comments.

## 4. Error handling

Handle realistic failure points: - Android Settings intents, -
permission state, - database I/O, - user input, - system-service
failures.

Do not wrap pure calculations in unnecessary `try/catch`.

Bad:

``` kotlin
try {
    val remaining = endAt - now
} catch (...) {
}
```

Good: - validate actual external/system boundaries.

## 5. Naming

Use short local names where the scope is obvious:

``` kotlin
val cfg = profile.config
for (i in items.indices) { ... }
```

Use descriptive names for public APIs:

``` kotlin
fun resolveActiveBlockingRules(...)
```

Do not shorten exported/public names.

## 6. Kotlin idioms

Prefer: - data classes, - sealed classes where they model real states, -
extension functions when they reduce repetition, - collection operators
when readable, - nullable handling idiomatic to Kotlin, - Compose state
patterns appropriate to the scope.

Avoid verbose Java-style patterns in Kotlin.

## 7. Compose

Prefer: - small composables only when reused or independently
meaningful, - state hoisting when state actually needs to move, - stable
keys in lists, - `remember` only when useful, - derived state for
derived UI values.

Do not split every 10 lines into a new composable.

## 8. Structure

Choose structure according to feature complexity.

For a simple screen:

``` text
Screen.kt
```

is acceptable.

For a complex feature:

``` text
feature/
  Screen.kt
  ViewModel.kt
  components/
```

Do not introduce Clean Architecture layers solely for appearance.

## 9. Line density

Prefer compact code when it remains readable.

Good:

``` kotlin
val remaining = (endAt - now).coerceAtLeast(0)
```

Avoid:

``` text
function A
 → function B
 → helper C
 → utility D
```

when the operation is one obvious expression.

## 10. Edge cases

Handle domain-relevant cases: - permissions revoked, - timer expired, -
schedule overlaps, - process death, - reboot, - invalid user input.

Do not build handlers for impossible inputs merely to make code look
defensive.

## 11. Android permissions

Permission code should be explicit.

Create a small coordinator only because permission flows are reused
across multiple screens.

Each permission: 1. explain, 2. open correct Settings screen, 3.
re-check on return, 4. update state.

Do not repeatedly request an already granted permission.

## 12. Security

-   Never store plaintext password.
-   Never log passwords or sensitive permission data.
-   Avoid logging private screen content.
-   Store only data needed for the feature.
-   Keep blocking decisions local by default.

## 13. State

Prefer one authoritative source of truth.

Timer:

``` text
endAt
```

not:

``` text
remainingCounter
```

Rules:

``` text
database + persisted settings
```

not:

``` text
in-memory singleton
```

## 14. Naming examples

Good:

``` text
BlockProfile
AppRule
FocusSession
UsageRepository
RuleResolver
PermissionState
```

Bad:

``` text
UniversalManager
GlobalHelper
MegaController
CommonUtils2
```

## 15. AI coding instruction

When generating or modifying Unhooked code, follow this instruction:

> Work within the existing scope and architecture. Don't abstract unless
> reused or required by a real boundary. Use idiomatic Kotlin/Android
> APIs. Keep local variables short when their scope is obvious and
> public APIs descriptive. Comments should explain non-obvious decisions
> only. Handle realistic Android/system/database failures, not
> theoretical ones. Prefer compact readable code over many tiny
> functions. Do not add libraries or architecture patterns without a
> concrete requirement. Preserve existing behavior unless the requested
> change requires it.

## 16. Definition of done

Code is done when: - it solves the requested problem, - it fits the
current structure, - it has no unnecessary abstraction, - it handles
relevant failure cases, - it does not introduce unnecessary
dependencies, - it is readable without tutorial comments, - it is
testable at the actual system boundary.
