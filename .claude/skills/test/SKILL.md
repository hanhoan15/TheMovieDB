---
name: test
description: Run unit tests and report results
user-invocable: true
allowed-tools: Bash(./gradlew *), Read, Grep
argument-hint: "[TestClassName or blank for all]"
---

# Run Tests

Run unit tests for the TheMovieDB project.

## Steps

1. If `$ARGUMENTS` is provided, run specific test class:
   ```
   ./gradlew composeApp:testDebugUnitTest --tests "*.$ARGUMENTS"
   ```
2. If no arguments, run all tests:
   ```
   ./gradlew composeApp:testDebugUnitTest
   ```
3. Report: total tests, passed, failed
4. If any tests fail:
   - Read the failing test file to understand what it tests
   - Analyze the error message
   - Suggest a fix (but don't apply it unless asked)
