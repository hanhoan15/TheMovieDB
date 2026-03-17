---
paths:
  - "composeApp/src/commonTest/**/*.kt"
---

# Testing Conventions

## Framework
- Use `kotlin-test` assertions (`assertEquals`, `assertTrue`, `assertNull`, etc.)
- Use `kotlinx-coroutines-test` for ViewModel tests (`runTest`, `advanceUntilIdle`)
- Manual fakes over mocking frameworks - fakes live in `commonTest/.../fake/`

## ViewModel Tests
- Always set `Dispatchers.setMain(StandardTestDispatcher())` in `@BeforeTest`
- Always `Dispatchers.resetMain()` in `@AfterTest`
- Call `advanceUntilIdle()` after ViewModel creation and after any action that triggers coroutines
- Test through `uiState.value` - assert on the state data class fields
- For derived `StateFlow` (e.g., `isBookmarked`), assert on the source repository directly since `stateIn` is async

## Test Fakes
- `FakeMovieRepository`: configurable via `moviesByCategory` map and `detailById` map
- `FakeWatchListRepository`: just instantiate `WatchListRepository()` directly (it's in-memory)
- `TestHelpers.kt`: `movie()` and `detail()` factory functions for test data

## Running Tests
- Run all: `./gradlew composeApp:testDebugUnitTest`
- Run specific: `./gradlew composeApp:testDebugUnitTest --tests "*.HomeViewModelTest"`
- Tests run on JVM (Android debug unit test task)

## What to Test
- ViewModel state transitions (loading -> loaded, error states)
- Business logic (pagination, debounce, bookmark toggle)
- Mapper functions (DTO -> domain, edge cases, null handling)
- Do NOT test composable UI rendering (no Compose UI tests in this project)
