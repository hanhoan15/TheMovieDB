# Pattern: Search Screen

> Reference implementation: `feature/search/SearchScreen.kt` + `SearchViewModel.kt`

## When to use

- Screen has a **text input** that triggers API search
- Needs **debounced input** (delay before API call)
- Has **multiple content states** (blank, loading, empty results, results)
- Uses **animated transitions** between content states
- Lives in the **bottom nav** (has `AppBottomNavBar`)

---

## File checklist

```
feature/<name>/
├── <Name>ViewModel.kt     ← UiState with query + search job + debounce
└── <Name>Screen.kt         ← Scaffold + AnimatedContent for state machine
core/navigation/AppRoutes.kt     ← data object (no params)
core/navigation/AppNavGraph.kt   ← composable with crossfade transitions
core/di/ViewModelModule.kt       ← viewModel { <Name>ViewModel(get()) }
commonTest/.../feature/<Name>ViewModelTest.kt
```

---

## Layout blueprint

```
Scaffold (containerColor = ScreenBackground, bottomBar = AppBottomNavBar)
└── Column (fillMaxSize, padding)
    │
    ├── Row (header)
    │   ├── IconButton (back)
    │   ├── Text ("Search")
    │   └── Spacer
    │
    ├── SearchInputField (value, onValueChange)
    │
    └── AnimatedContent (targetState = contentState)
        ├── BLANK → Box (empty — nothing to show)
        ├── LOADING → LazyColumn { items(5) { MovieListItemPlaceholder() } }
        ├── EMPTY → EmptyStateView (sad image + "We are sorry...")
        └── RESULTS → LazyColumn {
                items(results, key = { it.id }) { MovieListItem() }
            }
```

### Why `AnimatedContent` (not `if`/`when`)

| Approach | Transition | Use case |
|----------|-----------|----------|
| **`AnimatedContent`** | Fade in/out between states | Multiple distinct visual states (4+ states) |
| `Crossfade` | Only crossfade | 2 states with simple toggle |
| Raw `if`/`when` | No animation — instant swap | Inside `LazyListScope` (can't use AnimatedContent) |

**Why not raw `if`/`when`?** Without animation, the UI "jumps" between blank/loading/empty/results. `AnimatedContent` with `fadeIn + fadeOut` makes transitions feel smooth and intentional.

---

## Component choices

| Component | Why | Alternative | When to switch |
|-----------|-----|-------------|---------------|
| `SearchInputField` | TextField with custom dark styling, search icon | Material3 `SearchBar` | Never — Material SearchBar has unwanted elevation/shape |
| `MovieListItem` | Search results need metadata (title, rating, year) | `MovieCard` | When showing grid results instead of list |
| `MovieListItemPlaceholder` | Shimmer matching `MovieListItem` dimensions | `ShimmerPlaceholder` | Only if `MovieListItem` layout changes |
| `EmptyStateView` | Centered illustration + message | Inline `Text` | When empty state is a minor section, not the whole screen |
| `AppBottomNavBar` | Bottom nav tab screen | None | Always for bottom nav screens |

---

## Content state machine

```kotlin
// Enum for content states (defined in Screen file)
private enum class SearchContentState { BLANK, LOADING, EMPTY, RESULTS }

// Derived in Screen composable from UiState:
val contentState = when {
    uiState.searchQuery.isBlank() -> SearchContentState.BLANK
    uiState.isSearchLoading -> SearchContentState.LOADING
    uiState.hasSearchAttempted && uiState.searchResults.isEmpty() -> SearchContentState.EMPTY
    else -> SearchContentState.RESULTS
}
```

**Why derive state in Screen (not ViewModel)?**
- The state machine is purely a UI display concern
- ViewModel provides raw data; Screen decides how to display it
- Keeps ViewModel testable without UI-specific enums

**Why `hasSearchAttempted` flag?**
Without it, an empty `searchResults` list would show "No results" even before the user searches. `hasSearchAttempted` distinguishes:
- `query = ""` + `hasSearchAttempted = false` → BLANK
- `query = "xyz"` + `isSearchLoading = true` → LOADING
- `query = "xyz"` + `hasSearchAttempted = true` + `results = []` → EMPTY
- `query = "xyz"` + `results = [...]` → RESULTS

---

## State management

### UiState shape

```kotlin
data class <Name>UiState(
    val searchQuery: String = "",
    val searchResults: List<MovieItem> = emptyList(),
    val isSearchLoading: Boolean = false,       // false: no loading on screen start
    val hasSearchAttempted: Boolean = false,     // true after first API response
)
```

**Note:** `isSearchLoading` defaults to `false` (unlike list screens that default `true`). Search screens start blank — no data to load until user types.

### ViewModel pattern (debounce)

```kotlin
class <Name>ViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(<Name>UiState())
    val uiState: StateFlow<<Name>UiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        // 1. Update query immediately (responsive typing)
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                isSearchLoading = query.isNotBlank(),
                hasSearchAttempted = false,
                searchResults = if (query.isBlank()) emptyList() else current.searchResults,
            )
        }

        // 2. Cancel previous search
        searchJob?.cancel()
        if (query.isBlank()) return

        // 3. Debounce + fetch
        searchJob = viewModelScope.launch {
            delay(350)                                  // 350ms debounce
            val results = runCatching {
                repository.searchMovies(query = query, page = 1)
            }.getOrDefault(emptyList())

            // 4. Discard if query changed during fetch
            _uiState.update { current ->
                if (current.searchQuery != query) {
                    current                             // stale — discard
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

**Key patterns:**
1. **Immediate query update** — typing feels responsive, loading indicator shows instantly
2. **`searchJob?.cancel()`** — cancels previous in-flight search when user types more
3. **`delay(350)`** — waits 350ms after last keystroke before API call
4. **Stale query check** — `if (current.searchQuery != query)` discards results if user typed more during the fetch

**Why 350ms debounce?** Under 200ms fires too many API calls (user still typing). Over 500ms feels sluggish. 350ms is the sweet spot — fast enough to feel responsive, slow enough to batch keystrokes.

---

## AnimatedContent wiring (Screen side)

```kotlin
AnimatedContent(
    targetState = contentState,
    transitionSpec = {
        (fadeIn(animationSpec = tween(250))).togetherWith(
            fadeOut(animationSpec = tween(180)),
        )
    },
    label = "search_content_transition",
) { state ->
    when (state) {
        SearchContentState.BLANK -> Box(Modifier.fillMaxSize())
        SearchContentState.LOADING -> {
            LazyColumn {
                items(5) { MovieListItemPlaceholder() }
            }
        }
        SearchContentState.EMPTY -> {
            EmptyStateView(
                imageRes = Res.drawable.no_result,
                title = "We are sorry, we can not find the movie :(",
                subtitle = "Find your movie by Type title,\ncategories, years, etc",
                titleColor = AppColors.NoResultTitle,
                subtitleColor = AppColors.NoResultSubtitle,
            )
        }
        SearchContentState.RESULTS -> {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(uiState.searchResults, key = { it.id }) { movie ->
                    MovieListItem(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        }
    }
}
```

---

## Navigation wiring

```kotlin
// AppRoutes.kt
@Serializable data object <Name> : AppRoutes

// AppNavGraph.kt — crossfade for bottom nav tab
composable<AppRoutes.<Name>>(
    enterTransition = { NavTransitions.crossfadeIn() },
    exitTransition = { NavTransitions.crossfadeOut() },
) {
    <Name>Screen(
        onMovieClick = { movie ->
            navController.navigate(AppRoutes.Detail(movieId = movie.id))
        },
        onBack = { navController.popBackStack() },
        onDestinationSelected = { dest ->
            navigateToDestination(navController, dest)
        },
    )
}
```

---

## Common pitfalls

| Pitfall | Symptom | Fix |
|---------|---------|-----|
| No debounce | API call on every keystroke — rate limiting, lag | `delay(350)` + `searchJob?.cancel()` |
| No stale query check | Old results overwrite new search | Check `current.searchQuery != query` before updating |
| Missing `hasSearchAttempted` | "No results" shown before user searches | Add flag, only show empty state when `true` |
| `isSearchLoading = true` as default | Loading spinner on screen open | Default to `false` — search starts blank |
| `AnimatedContent` with wrong key | Unnecessary re-animation on every recomposition | Use a stable enum as `targetState`, not raw Boolean |
| Clearing results on blank query only in update | Stale results flash when clearing search | Clear results immediately in `_uiState.update` when `query.isBlank()` |
