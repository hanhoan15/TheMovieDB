# Step 1: Define the UiState

The UiState is a `data class` that holds everything the screen needs to render.
It lives in the same file as the ViewModel.

## Template

```kotlin
data class <Name>UiState(
    val isLoading: Boolean = true,
    // Add fields for this screen's data
)
```

## Rules
- Always include `isLoading` with default `true` — screen starts in loading state
- Use `emptyList()` as default for list fields — avoids null checks in UI
- Use nullable types only for truly optional data (e.g., detail that might not load)
- Keep it flat — avoid nested objects when possible

---

## Why `data class` for UiState?

### Choice: Flat `data class` with fields
```kotlin
data class HomeUiState(
    val movies: List<MovieItem> = emptyList(),
    val isLoading: Boolean = true,
)
```

### Alternative A: Sealed class/interface hierarchy
```kotlin
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val movies: List<MovieItem>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
```
**Why we don't use this:**
- Forces `when` blocks in the Screen for every render — verbose
- Hard to represent mixed states (loading MORE items while showing existing ones)
- Pagination needs `isPagingLoading` alongside existing data — sealed class can't do this cleanly
- Good for simple screens with truly exclusive states; bad for our screens which overlay loading on content

**When to use sealed class instead:** If a screen has 3+ truly mutually exclusive states with no overlap (e.g., onboarding wizard steps).

### Alternative B: `mutableStateOf` (Compose state)
```kotlin
var movies by mutableStateOf(emptyList<MovieItem>())
var isLoading by mutableStateOf(true)
```
**Why we don't use this:**
- `mutableStateOf` is a Compose runtime concept — ties ViewModel to Compose
- Can't use `.update { it.copy(...) }` for atomic multi-field updates
- Multiple independent states can cause recomposition between updates (screen flickers)
- `StateFlow` is platform-agnostic and works with `collectAsState()` in Compose

**When `mutableStateOf` is OK:** Inside a composable function for local UI state (e.g., dialog visibility, text field value). Never in ViewModels.

### Why defaults on every field?
- The UiState is created with `MutableStateFlow(HomeUiState())` — no-arg construction
- Defaults define the initial screen appearance before data loads
- `isLoading = true` means the screen shows shimmer immediately
- `emptyList()` means no null checks in the UI layer

## Real examples from the codebase

### Simple list screen (HomeViewModel.kt)
```kotlin
data class HomeUiState(
    val movies: List<MovieItem> = emptyList(),
    val selectedTab: AppTab = AppTab.NOW_PLAYING,
    val isListLoading: Boolean = true,
    val isPagingLoading: Boolean = false,
)
```

### Detail screen (DetailViewModel.kt)
```kotlin
data class DetailUiState(
    val detailMovie: MovieDetailItem? = null,   // nullable: might fail to load
    val isLoading: Boolean = true,
)
```

### Search screen (SearchViewModel.kt)
```kotlin
data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<MovieItem> = emptyList(),
    val isSearchLoading: Boolean = false,       // false: no search on start
    val hasSearchAttempted: Boolean = false,     // tracks if user has searched
)
```

## Common patterns
| Need | Field pattern |
|------|--------------|
| Loading spinner | `val isLoading: Boolean = true` |
| Pagination loader | `val isPagingLoading: Boolean = false` |
| List of items | `val items: List<ItemType> = emptyList()` |
| Optional detail | `val detail: DetailType? = null` |
| User input | `val query: String = ""` |
| Selected tab/filter | `val selectedTab: TabEnum = TabEnum.DEFAULT` |
| Empty state check | `val hasAttempted: Boolean = false` |
| Error message | `val errorMessage: String? = null` |
