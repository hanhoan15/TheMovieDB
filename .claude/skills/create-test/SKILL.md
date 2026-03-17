---
name: create-test
description: Step-by-step flow to write unit tests for ViewModels, repositories, and mappers
user-invocable: true
argument-hint: "<ClassName to test>"
---

# Flow: Write Unit Tests

Write tests for: **$ARGUMENTS**

---

## ViewModel Test Template

**File:** `composeApp/src/commonTest/kotlin/com/example/themoviedb/feature/${0}Test.kt`

```kotlin
package com.example.themoviedb.feature

import com.example.themoviedb.fake.FakeMovieRepository
import com.example.themoviedb.core.data.repository.WatchListRepository
import com.example.themoviedb.fake.movie
import com.example.themoviedb.fake.detail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ${0}Test {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsData() = runTest {
        val repository = FakeMovieRepository().apply {
            // Configure fake data:
            // moviesByCategory[MovieCategory.NOW_PLAYING] = listOf(movie(1, "A"))
            // detailById[42] = detail(42, "Movie")
            // searchResults = listOf(movie(1, "Found"))
        }
        val viewModel = MyViewModel(repository)
        advanceUntilIdle()  // ALWAYS call after creating ViewModel

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        // Assert on state fields...
    }

    @Test
    fun someAction_updatesState() = runTest {
        val repository = FakeMovieRepository()
        val viewModel = MyViewModel(repository)
        advanceUntilIdle()

        viewModel.someAction()
        advanceUntilIdle()  // ALWAYS call after triggering coroutines

        val state = viewModel.uiState.value
        // Assert on updated state...
    }
}
```

**CRITICAL rules for ViewModel tests:**
1. `Dispatchers.setMain(StandardTestDispatcher())` in `@BeforeTest` — required for `viewModelScope`
2. `Dispatchers.resetMain()` in `@AfterTest` — cleanup
3. `advanceUntilIdle()` after ViewModel creation — init block launches coroutines
4. `advanceUntilIdle()` after every action that triggers coroutines
5. Assert on `viewModel.uiState.value` — the state data class
6. For derived `StateFlow` (using `stateIn`), assert on the source directly (e.g., `repository.watchList.value`) since `stateIn` propagation is async

---

## Available Test Helpers

### FakeMovieRepository
```kotlin
val repo = FakeMovieRepository()
repo.moviesByCategory[MovieCategory.NOW_PLAYING] = listOf(movie(1, "A"), movie(2, "B"))
repo.detailById[42] = detail(42, "Title")
repo.searchResults = listOf(movie(3, "Found"))
```

### WatchListRepository (use real instance — it's in-memory)
```kotlin
val watchList = WatchListRepository()
watchList.toggle(movie(1, "A"))  // adds
watchList.toggle(movie(1, "A"))  // removes
watchList.isBookmarked(1)        // true/false
watchList.watchList.value        // current list
```

### Factory functions (TestHelpers.kt)
```kotlin
// Quick MovieItem:
movie(id = 1, title = "Test Movie")

// Quick MovieDetailItem:
detail(id = 1, title = "Test Detail")
```

---

## Mapper Test Template

**File:** `composeApp/src/commonTest/kotlin/com/example/themoviedb/core/data/MovieMapperTest.kt`

```kotlin
@Test
fun myMapper_happyPath() {
    val dto = SomeDto(id = 1, name = "Test")
    val result = dto.toDomainItem()
    assertEquals(1, result.id)
    assertEquals("Test", result.name)
}

@Test
fun myMapper_handlesNulls() {
    val dto = SomeDto()  // all nullable defaults
    val result = dto.toDomainItem()
    assertEquals(0, result.id)
    assertEquals("", result.name)
}
```

**Mapper test rules:**
- No coroutine setup needed (pure functions)
- Test happy path + null/empty edge cases
- Test image URL construction separately
- Keep in the existing `MovieMapperTest.kt` file

---

## What to test for each ViewModel

### HomeViewModel
- Initial load populates movies list
- Tab selection reloads with correct category
- Pagination appends to existing list
- Empty API response uses fallback data
- Loading states transition correctly

### DetailViewModel
- Loads detail by movieId
- Returns null gracefully when detail missing
- Bookmark toggle adds/removes from WatchListRepository

### SearchViewModel
- Search query returns results
- Blank query clears results
- Debounce waits before searching (test with `advanceTimeBy(350)`)

### WatchListViewModel
- Reflects current WatchListRepository state

---

## Running Tests

```bash
# All tests:
./gradlew composeApp:testDebugUnitTest

# Specific test class:
./gradlew composeApp:testDebugUnitTest --tests "*.HomeViewModelTest"

# Specific test method:
./gradlew composeApp:testDebugUnitTest --tests "*.HomeViewModelTest.initialState_loadsData"
```
