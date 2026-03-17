# Pattern: Local List Screen

> Reference implementation: `feature/watchlist/WatchListScreen.kt` + `WatchListViewModel.kt`

## When to use

- Screen displays data from a **local/in-memory repository** (no API calls)
- Data is **reactive** — changes from other screens appear automatically
- Has a prominent **empty state** when no data exists
- Uses **animated transition** between empty and populated states
- Lives in the **bottom nav** (has `AppBottomNavBar`)

---

## File checklist

```
feature/<name>/
├── <Name>ViewModel.kt     ← Simplest ViewModel — flow passthrough
└── <Name>Screen.kt         ← Scaffold + AnimatedContent (empty vs list)
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
    │   ├── Text ("Watch list")
    │   └── Spacer
    │
    └── AnimatedContent (targetState = watchList.isEmpty())
        ├── true → EmptyStateView (illustration + message)
        └── false → LazyColumn {
                items(watchList, key = { it.id }) { MovieListItem() }
            }
```

### Why `AnimatedContent` for empty/list toggle

| Approach | Effect | Use case |
|----------|--------|----------|
| **`AnimatedContent(isEmpty)`** | Smooth fade between empty and list | Two visual states with animated transition |
| `if (isEmpty) ... else ...` | Instant swap — no transition | When animation isn't needed |
| `Crossfade(isEmpty)` | Only crossfade | Simpler API but less customizable transitions |

The empty state is prominent (full-screen illustration). Without animation, adding the first bookmark causes an abrupt jump from illustration to list. `AnimatedContent` with fade makes it feel polished.

---

## Component choices

| Component | Why | Alternative | When to switch |
|-----------|-----|-------------|---------------|
| `MovieListItem` | Shows poster + metadata in vertical list | `MovieCard` | When showing grid instead of list |
| `EmptyStateView` | Full-screen centered illustration + text | Inline `Text` | When empty state is a minor section |
| `AppBottomNavBar` | Bottom nav tab screen | None | Always for bottom nav screens |
| `LazyColumn` | Efficient list with recycling | `Column + forEach` | Only if list is always small (< 10 items) |

---

## State management

### The simplest ViewModel pattern

```kotlin
class <Name>ViewModel(
    private val repository: WatchListRepository,
) : ViewModel() {

    val watchList: StateFlow<List<MovieItem>> = repository.watchList
}
```

**That's it.** No `MutableStateFlow`, no `init`, no loading states, no coroutines.

**Why so simple?**
- `WatchListRepository` already exposes a `StateFlow<List<MovieItem>>`
- The ViewModel just passes it through to the Screen
- Mutations happen in other ViewModels (e.g., `DetailViewModel.toggleBookmark()`)
- The `single` Koin scope ensures all ViewModels share the same repository instance

**Why have a ViewModel at all?** Even though it's a passthrough:
- Consistency: every screen follows the same `Screen + ViewModel` pattern
- Future-proofing: adding filtering, sorting, or delete actions later is easy
- Testing: can verify the flow is exposed correctly

### No UiState data class needed

For passthrough screens, you can collect the repository's flow directly:

```kotlin
// In Screen:
val watchList by viewModel.watchList.collectAsState()
```

**When to add a UiState wrapper:** If you later need additional state fields (e.g., `isDeleting`, `sortOrder`, `filterBy`), wrap into a UiState:

```kotlin
data class WatchListUiState(
    val watchList: List<MovieItem> = emptyList(),
    val sortOrder: SortOrder = SortOrder.ADDED_DATE,
)
```

---

## Screen composable pattern

```kotlin
@Composable
fun <Name>Screen(
    onMovieClick: (MovieItem) -> Unit,
    onBack: () -> Unit,
    onDestinationSelected: (BottomDestination) -> Unit,
    viewModel: <Name>ViewModel = koinViewModel(),
) {
    val watchList by viewModel.watchList.collectAsState()

    Scaffold(
        containerColor = AppColors.ScreenBackground,
        bottomBar = {
            AppBottomNavBar(
                selected = BottomDestination.WATCH_LIST,
                onSelect = onDestinationSelected,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimensions.ScreenHorizontalPadding),
        ) {
            // Header row...

            AnimatedContent(
                targetState = watchList.isEmpty(),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(250))).togetherWith(
                        fadeOut(animationSpec = tween(180)),
                    )
                },
                label = "watchlist_content",
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyStateView(
                        imageRes = Res.drawable.watch_list_empty,
                        title = "There Is No Movie Yet!",
                        subtitle = "Find your movie by Type title,\ncategories, years, etc",
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(watchList, key = { it.id }) { movie ->
                            MovieListItem(movie = movie, onClick = { onMovieClick(movie) })
                        }
                    }
                }
            }
        }
    }
}
```

---

## Shared repository pattern

```kotlin
// WatchListRepository — Koin `single` (shared across ViewModels)
class WatchListRepository {
    private val _watchList = MutableStateFlow<List<MovieItem>>(emptyList())
    val watchList: StateFlow<List<MovieItem>> = _watchList.asStateFlow()

    fun toggle(movie: MovieItem) {
        _watchList.update { current ->
            if (current.any { it.id == movie.id }) {
                current.filter { it.id != movie.id }    // remove
            } else {
                current + movie                          // add
            }
        }
    }

    fun isBookmarked(movieId: Int): Boolean =
        _watchList.value.any { it.id == movieId }
}
```

**How cross-screen reactivity works:**
1. User taps bookmark in `DetailScreen` → `DetailViewModel.toggleBookmark()` → `WatchListRepository.toggle()`
2. `WatchListRepository._watchList` updates via `MutableStateFlow.update {}`
3. `WatchListScreen` collects `watchList` StateFlow → UI automatically recomposes
4. `DetailViewModel.isBookmarked` derived flow also updates → bookmark icon toggles

All this works because Koin registers `WatchListRepository` as `single` — one instance shared by all ViewModels.

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

## Adding features to this pattern

### Adding swipe-to-delete
```kotlin
// In Screen — wrap MovieListItem with SwipeToDismissBox:
SwipeToDismissBox(
    state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                viewModel.removeMovie(movie)
                true
            } else false
        },
    ),
    backgroundContent = { DeleteBackground() },
) {
    MovieListItem(movie = movie, onClick = { onMovieClick(movie) })
}

// In ViewModel — add remove method:
fun removeMovie(movie: MovieItem) {
    watchListRepository.toggle(movie)   // toggle removes if present
}
```

### Adding persistence (DataStore/Room)
Replace `MutableStateFlow` in `WatchListRepository` with a DataStore or Room database:
```kotlin
class WatchListRepository(private val dataStore: DataStore<Preferences>) {
    val watchList: StateFlow<List<MovieItem>> = dataStore.data
        .map { prefs -> deserializeWatchList(prefs) }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())
}
```

---

## Common pitfalls

| Pitfall | Symptom | Fix |
|---------|---------|-----|
| `factory` instead of `single` for repository | Data disappears between screens | Use `single { WatchListRepository() }` in Koin |
| Not using `key` in `items()` | List flickers on add/remove | `items(watchList, key = { it.id })` |
| Adding loading states for local data | Unnecessary shimmer for instant data | Skip `isLoading` — local data is synchronous |
| Creating new repository per ViewModel | Cross-screen updates don't propagate | Koin `single` ensures one shared instance |
| Testing `isBookmarked` StateFlow value | Flaky — derived flow is async | Assert on `repository.watchList.value` directly |
