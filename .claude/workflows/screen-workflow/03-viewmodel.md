# Step 2: Create the ViewModel

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/feature/<name>/<Name>ViewModel.kt`

The ViewModel manages state and calls the repository. UiState lives in the same file.

## Template — Simple ViewModel (no route params)

```kotlin
package com.example.themoviedb.feature.<name>

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.themoviedb.core.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class <Name>UiState(
    val isLoading: Boolean = true,
)

class <Name>ViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(<Name>UiState())
    val uiState: StateFlow<<Name>UiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val result = repository.getMovies(...)
            _uiState.update { it.copy(isLoading = false, ...) }
        }
    }
}
```

## Template — ViewModel with route parameter

```kotlin
class <Name>ViewModel(
    private val itemId: Int,                        // from navigation route
    private val repository: MovieRepository,
    private val watchListRepository: WatchListRepository,
) : ViewModel() {
    // ...
}
```

## Real example: SearchViewModel (debounce pattern)

```kotlin
package com.example.themoviedb.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<MovieItem> = emptyList(),
    val isSearchLoading: Boolean = false,
    val hasSearchAttempted: Boolean = false,
)

class SearchViewModel(
    private val repository: MovieRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null                  // cancel previous search

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                isSearchLoading = query.isNotBlank(),
                hasSearchAttempted = false,
                searchResults = if (query.isBlank()) emptyList() else current.searchResults,
            )
        }

        searchJob?.cancel()                             // cancel previous
        if (query.isBlank()) return

        searchJob = viewModelScope.launch {
            delay(350)                                  // debounce 350ms
            val results = runCatching {
                repository.searchMovies(query = query, page = 1)
            }.getOrDefault(emptyList())

            _uiState.update { current ->
                if (current.searchQuery != query) {
                    current                             // query changed, discard
                } else {
                    current.copy(
                        isSearchLoading = false,
                        hasSearchAttempted = true,
                        searchResults = results,
                    )
                }
            }
        }
    }
}
```

## Real example: DetailViewModel (route param + shared repository)

```kotlin
package com.example.themoviedb.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.themoviedb.core.data.mapper.toWatchListMovie
import com.example.themoviedb.core.data.model.MovieDetailItem
import com.example.themoviedb.core.data.repository.MovieRepository
import com.example.themoviedb.core.data.repository.WatchListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val detailMovie: MovieDetailItem? = null,
    val isLoading: Boolean = true,
)

class DetailViewModel(
    private val movieId: Int,                           // from navigation route
    private val repository: MovieRepository,
    private val watchListRepository: WatchListRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Derived state from another repository's StateFlow
    val isBookmarked: StateFlow<Boolean> = watchListRepository.watchList
        .map { list -> list.any { it.id == movieId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadDetail()
    }

    fun toggleBookmark() {
        val movie = _uiState.value.detailMovie?.toWatchListMovie() ?: return
        watchListRepository.toggle(movie)
    }

    private fun loadDetail() {
        viewModelScope.launch {
            val detail = repository.getMovieDetail(movieId)
            _uiState.update {
                it.copy(detailMovie = detail, isLoading = false)
            }
        }
    }
}
```

## Rules

### State management
- Use `MutableStateFlow` + `StateFlow` — never `mutableStateOf` in ViewModels
- Expose read-only via `_uiState.asStateFlow()`
- Update with `_uiState.update { it.copy(...) }` — thread-safe

### Coroutines
- Always launch in `viewModelScope` — auto-cancels when ViewModel is destroyed
- Use `runCatching { }.getOrDefault()` for error handling
- Cancel previous jobs when user action invalidates them (e.g., `searchJob?.cancel()`)

### Dependencies
- Inject via constructor — Koin provides them automatically
- Route parameters (e.g., `movieId`) are also constructor params
- Use `MovieRepository` interface (not `TmdbMovieRepository` directly)

### Derived flows
- Use `.map { }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), default)`
- This turns any `Flow` into a `StateFlow` the Screen can collect

---

## Why these choices?

### Why `MutableStateFlow` over alternatives?

| Option | Pros | Cons | Verdict |
|--------|------|------|---------|
| **`MutableStateFlow`** | Thread-safe `.update{}`, platform-agnostic, works with `collectAsState()`, atomic multi-field updates via `.copy()` | Requires `collectAsState()` in Compose | **Use this** |
| `mutableStateOf` | Auto-recomposes, no `collectAsState()` needed | Compose-coupled, no atomic multi-field update, not thread-safe for concurrent writes | Only for local UI state in composables |
| `LiveData` | Lifecycle-aware | Android-only, no KMP support, deprecated pattern | Never use in KMP |
| `SharedFlow` | Supports replay, multiple collectors | No `.value` property (unless `StateFlow`), overkill for UI state | Use for events/one-shots, not screen state |
| `Channel` | Guaranteed delivery | Not state — doesn't replay to new collectors | Use for navigation events only |

### Why `StateFlow` (not `SharedFlow`) for UI?
`StateFlow` has a `.value` property — the Screen always has the current state, even if it resubscribes after config change. `SharedFlow` would lose the current state.

### Why `runCatching` over `try/catch`?
```kotlin
// Our pattern — concise, functional:
val result = runCatching { repository.getMovies(...) }.getOrDefault(emptyList())

// Alternative — verbose but equivalent:
val result = try { repository.getMovies(...) } catch (e: Exception) { emptyList() }
```
Both work. We use `runCatching` because:
- Single-expression result assignment (no multi-line try/catch block)
- Chainable: `.getOrDefault()`, `.getOrNull()`, `.getOrElse { }`, `.map { }`
- Consistent pattern across the codebase

**When to use `try/catch` instead:** When you need to handle specific exception types differently or log the error.

### Why `viewModelScope.launch` (not `withContext`)?
- `viewModelScope` auto-cancels when the ViewModel is destroyed (screen leaves)
- Ktor's suspend functions already switch to IO dispatcher internally
- No need for `withContext(Dispatchers.IO)` — Ktor handles it

### Why constructor injection (not field injection)?
```kotlin
// Our pattern — constructor injection:
class HomeViewModel(private val repository: MovieRepository) : ViewModel()

// Alternative — field injection (Koin supports this):
class HomeViewModel : ViewModel() {
    private val repository: MovieRepository by inject()
}
```
Constructor injection is better because:
- Dependencies are explicit and visible in the constructor
- Testable: just pass fakes in tests, no Koin test setup needed
- Compile-time safety: missing dependency = compile error, not runtime crash

### Why `SharingStarted.WhileSubscribed(5000)`?
```kotlin
val isBookmarked: StateFlow<Boolean> = watchListRepository.watchList
    .map { ... }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
```
- `WhileSubscribed(5000)` keeps the flow active for 5s after the last collector disappears
- Survives configuration changes (screen rotation) without restarting the flow
- More efficient than `Eagerly` (stops when no one is listening)
- More responsive than `Lazily` (restarts if the screen resubscribes after 5s)
