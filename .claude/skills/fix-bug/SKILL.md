---
name: fix-bug
description: Investigate and fix a bug in the codebase
user-invocable: true
argument-hint: "<description of the bug>"
---

# Fix Bug

Investigate and fix: **$ARGUMENTS**

## Process

1. **Understand** the bug from the description
2. **Search** the codebase for relevant code using Grep and Glob
3. **Read** the affected files to understand current behavior
4. **Identify** the root cause
5. **Fix** the issue with minimal, targeted changes
6. **Verify** the fix compiles:
   ```
   ./gradlew composeApp:compileDebugKotlinAndroid composeApp:compileKotlinIosSimulatorArm64
   ```
7. **Run tests** to ensure no regressions:
   ```
   ./gradlew composeApp:testDebugUnitTest
   ```
8. **Report** what was wrong and what was changed

## Guidelines
- Make the smallest possible fix - don't refactor surrounding code
- If the bug is in a ViewModel, check if there's a corresponding test to update
- If the bug is platform-specific, check both `androidMain` and `iosMain`
- Consider edge cases: null data, empty lists, network failures
