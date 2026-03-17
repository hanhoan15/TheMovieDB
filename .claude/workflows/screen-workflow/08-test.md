# Step 7: Write Unit Tests

**File:** `composeApp/src/commonTest/kotlin/com/example/themoviedb/feature/<Name>ViewModelTest.kt`

## Template

```kotlin
package com.example.themoviedb.feature

import com.example.themoviedb.core.data.repository.WatchListRepository
import com.example.themoviedb.fake.FakeMovieRepository
import com.example.themoviedb.fake.detail
import com.example.themoviedb.fake.movie
import com.example.themoviedb.feature.<name>.<Name>ViewModel
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
class <Name>ViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)         // REQUIRED: viewModelScope uses Main
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()                     // REQUIRED: cleanup
    }

    @Test
    fun initialLoad_completesSuccessfully() = runTest {
        val repository = FakeMovieRepository()
        // Configure fake data as needed
        val viewModel = <Name>ViewModel(repository)
        advanceUntilIdle()                          // REQUIRED: init coroutines

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
    }
}
```

## CRITICAL rules

1. **Always** `Dispatchers.setMain(StandardTestDispatcher())` in `@BeforeTest`
2. **Always** `Dispatchers.resetMain()` in `@AfterTest`
3. **Always** `advanceUntilIdle()` after creating ViewModel
4. **Always** `advanceUntilIdle()` after any action that triggers coroutines
5. Assert on `viewModel.uiState.value` — the state data class

## Test helpers available

### FakeMovieRepository
```kotlin
val repo = FakeMovieRepository()

// Configure movies by category:
repo.moviesByCategory[MovieCategory.NOW_PLAYING] = listOf(
    movie(1, "Movie A"),
    movie(2, "Movie B"),
)

// Configure movie detail:
repo.detailById[42] = detail(42, "Detail Title")

// Configure search results:
repo.searchResults = listOf(movie(3, "Search Result"))

// Simulate API failure:
repo.throwOnGetMovies = true
```

### Factory functions (TestHelpers.kt)
```kotlin
fun movie(id: Int, title: String): MovieItem
fun detail(id: Int, title: String): MovieDetailItem
```

### WatchListRepository — use real instance
```kotlin
val watchList = WatchListRepository()   // already in-memory, no faking needed
```

## Real test examples

### HomeViewModelTest — testing tab selection
```kotlin
@Test
fun tabSelection_reloadsMovies() = runTest {
    val repository = FakeMovieRepository().apply {
        moviesByCategory[MovieCategory.NOW_PLAYING] = listOf(movie(1, "A"))
        moviesByCategory[MovieCategory.TOP_RATED] = listOf(movie(2, "B"), movie(3, "C"))
    }
    val viewModel = HomeViewModel(repository)
    advanceUntilIdle()

    viewModel.onTabSelected(AppTab.TOP_RATED)
    advanceUntilIdle()

    assertEquals(2, viewModel.uiState.value.movies.size)
    assertEquals(AppTab.TOP_RATED, viewModel.uiState.value.selectedTab)
}
```

### DetailViewModelTest — testing bookmark
```kotlin
@Test
fun toggleBookmark_addsAndRemoves() = runTest {
    val repository = FakeMovieRepository().apply {
        detailById[42] = detail(id = 42, title = "Bookmark Test")
    }
    val watchListRepository = WatchListRepository()
    val viewModel = DetailViewModel(42, repository, watchListRepository)
    advanceUntilIdle()

    viewModel.toggleBookmark()
    advanceUntilIdle()
    assertEquals(1, watchListRepository.watchList.value.size)   // assert on repo directly
    assertTrue(watchListRepository.isBookmarked(42))

    viewModel.toggleBookmark()
    advanceUntilIdle()
    assertTrue(watchListRepository.watchList.value.isEmpty())
    assertFalse(watchListRepository.isBookmarked(42))
}
```

### SearchViewModelTest — testing search
```kotlin
@Test
fun searchQuery_loadsResults() = runTest {
    val repository = FakeMovieRepository().apply {
        searchResults = listOf(movie(1, "Found"))
    }
    val viewModel = SearchViewModel(repository)
    viewModel.onSearchQueryChanged("test")
    advanceUntilIdle()

    assertEquals(1, viewModel.uiState.value.searchResults.size)
    assertFalse(viewModel.uiState.value.isSearchLoading)
}
```

## Running tests

```bash
# All tests:
./gradlew composeApp:testDebugUnitTest

# One test class:
./gradlew composeApp:testDebugUnitTest --tests "*.<Name>ViewModelTest"

# One test method:
./gradlew composeApp:testDebugUnitTest --tests "*.<Name>ViewModelTest.initialLoad_completesSuccessfully"
```

---

## Why these choices?

### Why manual fakes over mocking libraries?

| Approach | Setup | KMP support | Verdict |
|----------|-------|-------------|---------|
| **Manual fakes** | Write a fake class implementing the interface | Full | **Use this** |
| Mokkery | `mock<MovieRepository>()` + `every { }` | Partial (plugin issues with some Kotlin versions) | Backup option |
| Mockk | `mockk<MovieRepository>()` + `every { }` | JVM/Android only | Can't use in KMP |
| Mockito | `mock(MovieRepository::class)` | JVM only | Can't use in KMP |

Manual fakes are preferred because:
- No mocking framework dependency — no plugin compatibility issues
- Explicit: you see exactly what the fake returns
- Reusable across all test classes
- Work on all Kotlin targets (Android, iOS, Desktop)

### Why `StandardTestDispatcher` (not `UnconfinedTestDispatcher`)?

| Dispatcher | Behavior | When to use |
|-----------|----------|-------------|
| **`StandardTestDispatcher`** | Coroutines execute only when you call `advanceUntilIdle()` | **Default — predictable timing** |
| `UnconfinedTestDispatcher` | Coroutines execute immediately inline | When you want fire-and-forget style |

We use `StandardTestDispatcher` because:
- Explicit control: you decide WHEN coroutines run with `advanceUntilIdle()`
- Can test intermediate states (e.g., `isLoading = true` before `advanceUntilIdle()`)
- More realistic: mirrors actual async behavior

### Why assert on `uiState.value` (not collect)?
```kotlin
// Our pattern — direct value access:
val state = viewModel.uiState.value
assertEquals(2, state.movies.size)

// Alternative — collect with Turbine:
viewModel.uiState.test {
    val state = awaitItem()
    assertEquals(2, state.movies.size)
}
```
- `StateFlow` always has a `.value` — no need for collection in tests
- Simpler, fewer dependencies (no Turbine library)
- `advanceUntilIdle()` ensures all coroutines complete before we check `.value`

**When to use Turbine/collect:** If you need to verify a sequence of emissions (e.g., loading → loaded → error).

### Why `Dispatchers.setMain()` / `resetMain()`?
- `viewModelScope` uses `Dispatchers.Main` internally
- In unit tests, there's no Android main looper — `Dispatchers.Main` throws
- `setMain(testDispatcher)` redirects Main to the test dispatcher
- `resetMain()` in `@AfterTest` prevents test pollution
- This is boilerplate that EVERY ViewModel test needs — never skip it

### Why assert on repository directly (not derived StateFlow)?
```kotlin
// Our pattern — assert on repo:
viewModel.toggleBookmark()
advanceUntilIdle()
assertEquals(1, watchListRepository.watchList.value.size)

// Alternative — assert on derived flow:
assertEquals(true, viewModel.isBookmarked.value)  // may not be propagated yet
```
Derived `StateFlow` (created with `.map { }.stateIn()`) is async — the mapped value may not propagate within the same test frame. Asserting on the source repository is synchronous and reliable.
