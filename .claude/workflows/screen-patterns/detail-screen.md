# Pattern: Detail Screen

> Reference implementation: `feature/detail/DetailScreen.kt` + `DetailViewModel.kt`

## When to use

- Screen shows a **single item's full details** loaded by ID
- Has an **overlapping header** (backdrop image + poster overlay)
- Has **multiple content sections** (tabs: about, reviews, cast, trailers, etc.)
- Needs **cross-repository state** (e.g., bookmark from `WatchListRepository`)
- Receives a **route parameter** (e.g., `movieId: Int`)
- Does **NOT** have bottom nav

---

## File checklist

```
feature/<name>/
├── <Name>ViewModel.kt     ← UiState (nullable detail) + ViewModel with route param
└── <Name>Screen.kt         ← Column + verticalScroll, overlapping header
core/navigation/AppRoutes.kt     ← data class with param (val itemId: Int)
core/navigation/AppNavGraph.kt   ← composable with slide transitions, toRoute<>
core/di/ViewModelModule.kt       ← viewModel { params -> <Name>ViewModel(params.get(), get(), get()) }
commonTest/.../feature/<Name>ViewModelTest.kt
```

---

## Layout blueprint

```
Column (fillMaxSize, verticalScroll, ScreenBackground)
│
├── Row (top bar)
│   ├── IconButton (back arrow)
│   ├── Text (title, ellipsis)
│   └── IconButton (bookmark toggle)
│
├── Box (backdrop, height = 246.dp)
│   ├── AsyncImageWithPlaceholder (backdrop, Crop)
│   └── Row (overlay at bottom, offset for overlap)
│       ├── AsyncImageWithPlaceholder (poster, 120x163dp)
│       └── Column (title + RatingBadge)
│
├── Spacer (negative offset adjustment)
│
├── Row (meta info)
│   ├── DetailMetaItem (year icon + "2024")
│   ├── DetailMetaItem (clock icon + "148 Minutes")
│   └── DetailMetaItem (ticket icon + "Action")
│
├── Row (section tabs, horizontalScroll)
│   └── DetailSection.entries.forEach { tab →
│       Column(clickable) { Text + underline indicator }
│   }
│
└── when (section) {
        ABOUT → Text(overview)
        REVIEWS → Column { reviews.forEach { ReviewCard } }
        TRAILERS → Column { trailers.forEach { TrailerCard } }
        IMAGES → Column { images.chunked(2).forEach { Row { ImageCards } } }
        SIMILAR → Column { similar.forEach { MovieListItem } }
        CAST → Column { cast.chunked(2).forEach { Row { CastItem } } }
    }
```

### Why `Column + verticalScroll` (not `LazyColumn`)

| Layout | Use case | Trade-off |
|--------|----------|-----------|
| **`Column + verticalScroll`** | Fixed sections with overlapping elements | No item recycling (fine for detail screens) |
| `LazyColumn` | Long homogeneous lists | Can't handle overlapping `offset()` layout |
| `NestedScrollConnection` | Parallax header effects | Complex, usually overkill |

**Why not LazyColumn?** The overlapping poster-over-backdrop uses `Modifier.offset(y = overlayPosterOverlap)`. `LazyColumn` doesn't support overlapping items — items are measured independently with no overlap. `Column` allows free positioning with offsets.

**Why `images.chunked(2)` instead of `LazyVerticalGrid`?** You can't nest a `LazyVerticalGrid` inside a `Column + verticalScroll` (nested scrollable containers crash). `chunked(2)` with `Row` creates a simple 2-column layout without nesting scrollable containers.

---

## Component choices

| Component | Why | Alternative | When to switch |
|-----------|-----|-------------|---------------|
| `AsyncImageWithPlaceholder` | Shimmer while loading backdrop/poster | Raw `AsyncImage` | Never for detail — loading states matter |
| `RatingBadge` | Star + rating on translucent chip | Custom `Row` with star icon | When you need different badge styling |
| `DetailMetaItem` | Icon + text chip (year, runtime, genre) | `MetaInfoRow` | `MetaInfoRow` is for `MovieListItem` rows |
| `MovieListItem` | Similar movies vertical list | `MovieCard` in grid | When section needs metadata (title, rating) |
| Custom section tabs | `DetailSection` enum differs from `AppTab` | `CategoryTabs` | `CategoryTabs` is typed to `AppTab` only |

---

## API call pattern

### Repository aggregation (TmdbMovieRepository)

```kotlin
override suspend fun getMovieDetail(movieId: Int): MovieDetailItem? {
    // Required data — fail entire call if missing
    val detail = runCatching { apiService.getMovieDetail(movieId) }.getOrNull()
        ?: return null

    val detailItem = detail.toMovieDetailItem(imageBaseUrl)

    // Optional data — fail gracefully per section
    val reviews = runCatching {
        apiService.getMovieReviews(movieId).results
            .map { it.toMovieReviewItem(imageBaseUrl) }
    }.getOrDefault(emptyList())

    val cast = runCatching {
        apiService.getMovieCredits(movieId).cast
            .map { it.toMovieCastItem(imageBaseUrl) }
    }.getOrDefault(emptyList())

    val trailers = runCatching {
        apiService.getMovieVideos(movieId).results
            .mapNotNull { it.toMovieTrailerItem() }
    }.getOrDefault(emptyList())

    val images = runCatching {
        apiService.getMovieImages(movieId).let { dto ->
            (dto.backdrops + dto.posters).mapNotNull { buildImageUrl(imageBaseUrl, it.filePath) }
        }
    }.getOrDefault(emptyList())

    val similar = runCatching {
        apiService.getSimilarMovies(movieId).results
            .mapNotNull { it.toMovieItem(imageBaseUrl) }
    }.getOrDefault(emptyList())

    return detailItem.copy(
        reviews = reviews, cast = cast, trailers = trailers,
        images = images, similarMovies = similar,
    )
}
```

**Why aggregate in repository (not ViewModel)?**
- One method call = one loading state in ViewModel
- Optional section failures don't block the whole screen
- ViewModel stays simple: call `getMovieDetail()`, update state

---

## State management

### UiState shape

```kotlin
data class <Name>UiState(
    val detail: MovieDetailItem? = null,    // nullable: might fail to load
    val isLoading: Boolean = true,
)
```

**Why nullable detail (not sealed class)?**
- Only two real states: loading or loaded (with possible null)
- Sealed class (`Loading`/`Success`/`Error`) forces `when` blocks everywhere in the Screen
- `if (uiState.isLoading) shimmer else content` is simpler

### ViewModel pattern

```kotlin
class <Name>ViewModel(
    private val itemId: Int,                            // from route
    private val repository: MovieRepository,
    private val watchListRepository: WatchListRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(<Name>UiState())
    val uiState: StateFlow<<Name>UiState> = _uiState.asStateFlow()

    // Derived state: bookmark status from shared repository
    val isBookmarked: StateFlow<Boolean> = watchListRepository.watchList
        .map { list -> list.any { it.id == itemId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadDetail()
    }

    fun toggleBookmark() {
        val movie = _uiState.value.detail?.toWatchListMovie() ?: return
        watchListRepository.toggle(movie)
    }

    private fun loadDetail() {
        viewModelScope.launch {
            val detail = repository.getMovieDetail(itemId)
            _uiState.update { it.copy(detail = detail, isLoading = false) }
        }
    }
}
```

**Key patterns:**
- `itemId` is a constructor param from navigation route (Koin's `params.get()`)
- `isBookmarked` is a **derived StateFlow** from `WatchListRepository` — reactive, auto-updates
- `SharingStarted.WhileSubscribed(5000)` survives config changes (5s grace period)
- `toggleBookmark()` delegates to shared repository — affects both Detail and WatchList screens

### Local UI state for section tabs

```kotlin
// Inside Screen composable (not ViewModel — purely visual state)
var section by remember { mutableStateOf(DetailSection.ABOUT) }
```

**Why `mutableStateOf` here (not in ViewModel)?** Tab selection is purely visual — no API calls, no business logic. Local composable state is appropriate. If switching tabs triggered API calls, it would belong in the ViewModel.

---

## Navigation wiring

```kotlin
// AppRoutes.kt
@Serializable data class <Name>(val itemId: Int) : AppRoutes

// AppNavGraph.kt — slide transition (push navigation)
composable<AppRoutes.<Name>> { backStackEntry ->
    val route = backStackEntry.toRoute<AppRoutes.<Name>>()
    <Name>Screen(
        itemId = route.itemId,
        onMovieClick = { movie ->
            navController.navigate(AppRoutes.Detail(movieId = movie.id))
        },
        onOpenLink = { url ->
            navController.navigate(AppRoutes.Web(url = url))
        },
        onImageClick = { images, index ->
            val json = Json.encodeToString(images)
            navController.navigate(AppRoutes.ImageViewer(imagesJson = json, initialIndex = index))
        },
        onBack = { navController.popBackStack() },
    )
}

// Koin registration — params.get() for route param
viewModel { params -> <Name>ViewModel(params.get(), get(), get()) }
```

---

## Common pitfalls

| Pitfall | Symptom | Fix |
|---------|---------|-----|
| Using `LazyColumn` with overlapping header | Items can't overlap, poster clipped | Use `Column + verticalScroll` with `Modifier.offset()` |
| Nesting `LazyVerticalGrid` inside scrollable `Column` | Crash: nested scrollable containers | Use `items.chunked(2).forEach { Row { } }` instead |
| Asserting `isBookmarked.value` in tests | Flaky — derived StateFlow is async | Assert on `watchListRepository.watchList.value` directly |
| Not guarding `toggleBookmark()` for null detail | NPE when detail hasn't loaded | `val movie = detail ?: return` |
| Single API call for detail | All data fails if one endpoint fails | Wrap optional sections in `runCatching { }.getOrDefault()` |
| Putting section tab state in ViewModel | Unnecessary complexity for visual-only state | Use `remember { mutableStateOf() }` in Screen |
