# Pattern: List Grid Screen

> Reference implementation: `feature/home/HomeScreen.kt` + `HomeViewModel.kt`

## When to use

- Screen displays items in a **multi-column grid** (posters, thumbnails, cards)
- Has **pagination** (infinite scroll / load more)
- Has **tab filtering** that reloads data from API
- Lives in the **bottom nav** (has `AppBottomNavBar`)
- Has a **featured/hero section** above the grid (carousel, banner)

---

## File checklist

```
feature/<name>/
├── <Name>ViewModel.kt     ← UiState + ViewModel with pagination
└── <Name>Screen.kt         ← Scaffold + LazyVerticalGrid
core/navigation/AppRoutes.kt     ← data object (no params)
core/navigation/AppNavGraph.kt   ← composable with crossfade transitions
core/di/ViewModelModule.kt       ← viewModel { <Name>ViewModel(get()) }
commonTest/.../feature/<Name>ViewModelTest.kt
```

---

## Layout blueprint

```
Scaffold (containerColor = AppColors.ScreenBackground)
├── bottomBar = AppBottomNavBar(selected, onSelect)
└── Box (fillMaxSize)
    └── LazyVerticalGrid (GridCells.Fixed(3))
        │
        ├── item(span = GridItemSpan(maxLineSpan))  ← full-width header
        │   └── Column
        │       ├── SectionHeader("Title")
        │       ├── SearchBarReadOnly(onClick = navigate to search)
        │       ├── LazyRow { FeaturedMovieCard items }
        │       └── CategoryTabs(tabs, selected, onSelect)
        │
        ├── if (isListLoading)
        │   └── items(12) { PosterGridPlaceholder() }
        │
        └── else
            └── items(movies, key = { it.id }) { MovieCard() }

    └── if (isPagingLoading)  ← overlay
        └── Box(Alignment.Center) { "Loading more..." badge }
```

### Why `LazyVerticalGrid` (not `LazyColumn`)

| Layout | Use case | Trade-off |
|--------|----------|-----------|
| **`LazyVerticalGrid(Fixed(3))`** | Multi-column poster grid with recycling | Full-width headers need `GridItemSpan(maxLineSpan)` |
| `LazyColumn` with `Row` chunks | Simple 2-3 column layout inside scrollable parent | No item recycling per-row, manual chunking |
| `LazyVerticalStaggeredGrid` | Items with different heights (Pinterest) | Overkill when all items are same height |
| `FlowRow` | Wrapping tags/chips | No recycling, bad for large lists |

**Why `Fixed(3)` not `Adaptive`?** `Adaptive(minSize)` changes column count based on screen width. Our poster cards have a fixed design size (`Dimensions.GridPosterHeight` = 165dp), and the grid always shows 3 columns regardless of device. `Fixed(3)` guarantees consistent layout.

---

## Component choices

| Component | Why | Alternative | When to switch |
|-----------|-----|-------------|---------------|
| `MovieCard` | Grid poster — image fills card, no text | `MovieListItem` | When you need metadata visible (title, rating) |
| `FeaturedMovieCard` | Large numbered carousel card (132x190dp) | `MovieCard` with larger size | When you don't need the numbered overlay |
| `CategoryTabs` | Dynamic-width underline tabs | Material3 `TabRow` | Never — Material tabs enforce equal widths |
| `SearchBarReadOnly` | Clickable fake search field | `SearchInputField` | When the screen handles search itself |
| `AppBottomNavBar` | Bottom nav with 3 tabs | Material3 `NavigationBar` | Never — Material bar has unwanted styling |
| `PosterGridPlaceholder` | Shimmer matching `MovieCard` size | `CircularProgressIndicator` | When you can't predict content shape |

---

## State management

### UiState shape

```kotlin
data class <Name>UiState(
    val items: List<MovieItem> = emptyList(),
    val selectedTab: AppTab = AppTab.NOW_PLAYING,
    val isListLoading: Boolean = true,      // initial + tab switch loading
    val isPagingLoading: Boolean = false,    // load-more loading
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
)
```

**Why two loading flags?**
- `isListLoading = true` → shows shimmer placeholders (replaces entire grid)
- `isPagingLoading = true` → shows "Loading more..." overlay (keeps existing items visible)
- A single `isLoading` would flash shimmer on every page load — jarring UX

### ViewModel pattern

```kotlin
class <Name>ViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(<Name>UiState())
    val uiState: StateFlow<<Name>UiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadFirstPage(AppTab.NOW_PLAYING)
    }

    fun onTabSelected(tab: AppTab) {
        if (tab == _uiState.value.selectedTab) return
        _uiState.update { it.copy(selectedTab = tab) }
        loadFirstPage(tab)
    }

    fun onLoadMore() {
        val state = _uiState.value
        if (!state.canLoadMore || state.isPagingLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPagingLoading = true) }
            val nextPage = state.currentPage + 1
            val newItems = runCatching {
                repository.getMovies(state.selectedTab.toCategory(), nextPage)
            }.getOrDefault(emptyList())

            _uiState.update {
                it.copy(
                    items = it.items + newItems,
                    currentPage = nextPage,
                    isPagingLoading = false,
                    canLoadMore = newItems.isNotEmpty(),
                )
            }
        }
    }

    private fun loadFirstPage(tab: AppTab) {
        loadJob?.cancel()                               // cancel previous tab's load
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isListLoading = true, currentPage = 1) }
            val items = runCatching {
                repository.getMovies(tab.toCategory(), page = 1)
            }.getOrDefault(emptyList())

            _uiState.update {
                it.copy(items = items, isListLoading = false, canLoadMore = items.isNotEmpty())
            }
        }
    }
}
```

**Key patterns:**
- `loadJob?.cancel()` on tab switch prevents stale data from overwriting new tab's results
- `canLoadMore` flag prevents unnecessary API calls when all pages loaded
- Guard `if (!canLoadMore || isPagingLoading) return` prevents duplicate pagination requests

---

## Pagination wiring (Screen side)

```kotlin
val gridState = rememberLazyGridState()

LaunchedEffect(gridState, uiState.items.size, uiState.isListLoading, uiState.isPagingLoading) {
    snapshotFlow {
        val totalItems = gridState.layoutInfo.totalItemsCount
        val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val nearBottom = totalItems > 0 && lastVisible >= totalItems - 6
        nearBottom && !uiState.isListLoading && !uiState.isPagingLoading
    }
        .distinctUntilChanged()
        .collect { shouldLoadMore ->
            if (shouldLoadMore) viewModel.onLoadMore()
        }
}
```

**Why `totalItems - 6` threshold?** Pre-fetches 6 items before the end. With 3 columns, that's 2 rows of lead time — enough for the API to respond before the user reaches the bottom.

**Why `snapshotFlow` + `distinctUntilChanged`?** `snapshotFlow` converts Compose snapshot state (scroll position) into a Flow. `distinctUntilChanged` prevents firing the same `true` signal multiple times during a single scroll gesture.

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
| Not canceling job on tab switch | Old tab's data overwrites new tab | `loadJob?.cancel()` before launching new load |
| Missing `key` in `items()` | Grid flickers on pagination | Use `key = { index -> items[index].id }` |
| Single `isLoading` flag | Full shimmer on every page load | Separate `isListLoading` + `isPagingLoading` |
| No `canLoadMore` guard | Infinite API calls at bottom of list | Track `canLoadMore` from empty API response |
| `LazyColumn` instead of `LazyVerticalGrid` | Can't do multi-column without manual chunking | Use `LazyVerticalGrid(GridCells.Fixed(N))` |
| Forgetting `GridItemSpan(maxLineSpan)` for header | Header only spans 1 column | Wrap header in `item(span = { GridItemSpan(maxLineSpan) })` |
