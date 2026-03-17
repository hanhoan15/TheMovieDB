# Step 3: Create the Screen Composable

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/feature/<name>/<Name>Screen.kt`

---

## Choosing the right root layout

### `Scaffold` — use for screens with bottom nav or top bar
```kotlin
Scaffold(
    containerColor = AppColors.ScreenBackground,
    bottomBar = { AppBottomNavBar(...) },
) { innerPadding ->
    Column(modifier = Modifier.padding(innerPadding)) { ... }
}
```
**Why:** Scaffold handles system insets (status bar, navigation bar) and positions the bottom bar correctly. The `innerPadding` avoids content hiding behind the bottom nav.

**When NOT to use:** Full-screen immersive experiences (e.g., ImageViewerScreen uses raw `Box` without Scaffold because it needs edge-to-edge display).

### `Column` with `verticalScroll` — use for scrollable detail screens
```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .background(AppColors.ScreenBackground)
        .verticalScroll(rememberScrollState()),
) { ... }
```
**Why DetailScreen uses this instead of LazyColumn:** The detail screen has a complex overlapping header (backdrop + poster) with `offset()`. LazyColumn doesn't support overlapping items well. `Column` + `verticalScroll` allows free positioning.

**Alternative — `LazyColumn`:** Use when content is a simple linear list with many items. LazyColumn recycles items off-screen (better for memory). But it can't handle overlapping content or non-standard layouts.

### `LazyVerticalGrid` — use for poster grids
```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(3),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = PaddingValues(
        start = Dimensions.ScreenHorizontalPadding,
        end = Dimensions.ScreenHorizontalPadding,
        top = Dimensions.ScreenVerticalPadding,
        bottom = Dimensions.ScreenVerticalPadding,
    ),
) { ... }
```
**Why HomeScreen uses this:** Shows movie posters in a 3-column grid with full-width header (search bar, featured carousel, tabs). `GridItemSpan(maxLineSpan)` makes the header span all 3 columns.

**Alternative — `LazyVerticalStaggeredGrid`:** Use when items have different heights (e.g., a Pinterest-style layout). Not used here because all posters have the same height (`Dimensions.GridPosterHeight` = 165dp).

**Alternative — `LazyColumn` with `Row` chunks:** `images.chunked(2).forEach { Row { ... } }` — used in DetailScreen ImagesSection for a 2-column layout inside a scrollable Column. Simpler than nesting a LazyGrid inside a scrollable Column (which has measurement issues).

---

## Choosing the right list composable

### `LazyRow` — horizontal scrollable list
```kotlin
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = PaddingValues(horizontal = 10.dp),
) {
    itemsIndexed(featuredMovies) { index, movie ->
        FeaturedMovieCard(movie = movie, index = index, onClick = { ... })
    }
}
```
**Why HomeScreen uses this for featured movies:** Horizontal carousel of 8 large poster cards. `LazyRow` only renders visible cards (performance). `contentPadding` adds spacing at edges without affecting item spacing.

**Alternative — `Row` with `horizontalScroll`:** Use for a small number of items (< 10) that don't need recycling. Used in DetailScreen's section tabs (`Row(modifier = Modifier.horizontalScroll(rememberScrollState()))`).

### `LazyColumn` — vertical scrollable list
```kotlin
LazyColumn(
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(bottom = 16.dp),
) {
    items(movieList, key = { movie -> movie.id }) { movie ->
        MovieListItem(movie = movie, onClick = { onMovieClick(movie) })
    }
}
```
**Why SearchScreen and WatchListScreen use this:** Displays vertical list of `MovieListItem` cards. `key = { movie.id }` enables efficient recomposition — Compose only re-renders changed items.

**When NOT to use LazyColumn:** Inside a scrollable parent (Column with verticalScroll). Nested scrollable containers crash. Use `Column` + `forEach` instead (as DetailScreen does for reviews, trailers, similar).

---

## Choosing content display strategies

### `AnimatedContent` — smooth transitions between states
```kotlin
AnimatedContent(
    targetState = contentState,
    transitionSpec = {
        (fadeIn(animationSpec = tween(250))).togetherWith(
            fadeOut(animationSpec = tween(180)),
        )
    },
    label = "content_transition",
) { state ->
    when (state) {
        ContentState.LOADING -> { /* shimmer placeholders */ }
        ContentState.EMPTY -> { EmptyStateView(...) }
        ContentState.RESULTS -> { LazyColumn { ... } }
    }
}
```
**Why SearchScreen uses this:** Animates smoothly between BLANK → LOADING → EMPTY → RESULTS states. Without it, the UI would "jump" between states.

**Alternative — `if`/`when` without animation:** Simpler but no transition. Use when the state change is rare or animation isn't needed (e.g., DetailScreen's section switching uses `when (section)` without animation because the tabs already provide visual feedback).

**Alternative — `Crossfade`:** Simpler than `AnimatedContent` but only does crossfade. Use `AnimatedContent` when you want custom transitions (slide, scale, etc.).

### Direct `if` / `when` — for loading/loaded toggle
```kotlin
if (uiState.isLoading) {
    items(12) { PosterGridPlaceholder() }
} else {
    items(uiState.movies.size, key = { index -> uiState.movies[index].id }) { index ->
        MovieCard(movie = uiState.movies[index], onClick = { ... })
    }
}
```
**Why HomeScreen uses this:** Inside `LazyVerticalGrid`, you can't use `AnimatedContent` (it breaks lazy layout measurement). Direct `if` is the only option inside `LazyListScope`.

---

## Choosing the right movie card

### `MovieCard` — for grid posters
```kotlin
MovieCard(
    movie = movie,
    onClick = { onMovieClick(movie) },
)
```
**What it is:** Rounded poster thumbnail (14dp corners). Image fills the card. No text overlay.
**Size:** Full grid column width × `Dimensions.GridPosterHeight` (165dp).
**Use when:** Showing movies in a grid (HomeScreen main grid).
**Shimmer placeholder:** `PosterGridPlaceholder()` — matches the same size with ShimmerPlaceholder.

### `FeaturedMovieCard` — for featured carousel
```kotlin
FeaturedMovieCard(
    movie = movie,
    index = index,
    onClick = { onMovieClick(movie) },
)
```
**What it is:** Larger poster card (132×190dp) with a big numbered overlay (1, 2, 3...). Number uses an outline text effect (4 offset layers + fill).
**Size:** 150×218dp total box (poster + number overflow).
**Use when:** Horizontal featured carousel (HomeScreen top section).
**Why not just MovieCard?** The numbered overlay requires special Box layout with offset positioning. MovieCard is a simple Surface — can't overlay numbers.
**Shimmer placeholder:** `Surface(shape = RoundedCornerShape(16.dp), modifier = Modifier.size(Dimensions.FeaturedCardSize)) { ShimmerPlaceholder() }`

### `MovieListItem` — for vertical lists
```kotlin
MovieListItem(movie = movie, onClick = { onMovieClick(movie) })
```
**What it is:** Horizontal row: poster (92×126dp) + column of metadata (title, rating, genre, year, runtime). Uses `MetaInfoRow` and `MetaInfoRowDrawable` for icon+text pairs.
**Use when:** Search results, watchlist, similar movies, any vertical list where you need metadata visible.
**Why not MovieCard?** MovieCard only shows the poster image. MovieListItem shows title, rating, genre, year — much more informative for list browsing.
**Shimmer placeholder:** `MovieListItemPlaceholder()` — matches the exact layout dimensions with shimmer blocks.

---

## Choosing the right image component

### `AsyncImageWithPlaceholder` — default for all remote images
```kotlin
AsyncImageWithPlaceholder(
    model = movie.posterUrl,
    contentDescription = movie.title,
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Crop,   // default
)
```
**What it does:** Shows shimmer animation while loading, then cross-fades to the loaded image. Background stays shimmer color.
**Why not raw `AsyncImage`?** Raw Coil `AsyncImage` shows blank/transparent during loading — looks broken. Our wrapper always shows the shimmer effect.
**ContentScale options:**
- `ContentScale.Crop` (default) — fills the container, clips edges. Use for posters, backdrops, thumbnails.
- `ContentScale.Fit` — fits within container, shows letterboxing. Use for full-screen image viewer.
- `ContentScale.FillWidth` — fills width, may overflow height. Use for wide banners.

### `Image` with `painterResource` — for local assets only
```kotlin
Image(
    painter = painterResource(Res.drawable.watch_list_empty),
    contentDescription = "Empty state",
    modifier = Modifier.size(96.dp),
)
```
**Use when:** Displaying bundled drawable resources (empty state illustrations, icons).
**Why not AsyncImageWithPlaceholder?** Local resources load instantly — no shimmer needed.

---

## Choosing the right loading state

### `ShimmerPlaceholder` — animated skeleton
```kotlin
ShimmerPlaceholder(
    modifier = Modifier
        .width(210.dp)
        .height(18.dp)
        .clip(RoundedCornerShape(6.dp)),
)
```
**What it does:** Vertical gradient animation sweeping top-to-bottom (1300ms loop). Shows a dark base color with lighter shimmer moving across.
**Use when:** Loading state for any content area. Shape it with `clip()` and size with `width()`/`height()`.

**Alternative — `CircularProgressIndicator`:** Material spinner. Use for indeterminate loading where you can't predict the content shape (e.g., the "Loading more..." overlay in HomeScreen's pagination).

**Alternative — `Text("Loading...")`:** DetailScreen uses this for section content loading. Simpler but less polished than shimmer.

### Pre-built shimmer composables
| Composable | Mimics | Used in |
|-----------|--------|---------|
| `PosterGridPlaceholder()` | `MovieCard` grid cell | HomeScreen grid loading |
| `MovieListItemPlaceholder()` | `MovieListItem` row | SearchScreen loading |
| `FeaturedMoviesRowPlaceholder()` | `FeaturedMovieCard` carousel | HomeScreen featured loading |

**Why use these instead of raw ShimmerPlaceholder?** They match the exact dimensions and layout of the real content, so the UI doesn't "jump" when data loads. Users see the same shapes they'll see with real data.

---

## Choosing the right empty/error state

### `EmptyStateView` — centered image + text
```kotlin
EmptyStateView(
    imageRes = Res.drawable.watch_list_empty,
    title = "There Is No Movie Yet!",
    subtitle = "Find your movie by Type title,\ncategories, years, etc",
)
```
**What it does:** Centered vertically and horizontally. Image (96dp) + title (16sp semibold) + subtitle (12sp medium).
**Use when:** Empty lists (watchlist, search with no results).
**Customizable colors:** `titleColor` and `subtitleColor` params — SearchScreen uses `AppColors.NoResultTitle`/`AppColors.NoResultSubtitle` for a different color scheme than WatchListScreen's defaults.

**Alternative — inline Text:** `Text("No reviews available.")` — used in DetailScreen sections. Simpler, no illustration. Use when the empty state is a minor section, not the whole screen.

---

## Choosing search input

### `SearchBarReadOnly` — clickable, navigates to search screen
```kotlin
SearchBarReadOnly(onClick = { onDestinationSelected(BottomDestination.SEARCH) })
```
**What it is:** Looks like a search field but is read-only. Clicking navigates to the real SearchScreen.
**Use when:** HomeScreen — the search bar is just a navigation shortcut.
**Why not SearchInputField?** HomeScreen doesn't handle search. It just needs to look like a search bar and navigate on click.

### `SearchInputField` — editable text field
```kotlin
SearchInputField(
    value = uiState.searchQuery,
    onValueChange = viewModel::onSearchQueryChanged,
)
```
**What it is:** Material3 `TextField` with custom colors (dark background, transparent indicator, custom cursor color). Includes search icon as trailing icon.
**Use when:** SearchScreen — actual text input that triggers search.
**Why not `OutlinedTextField`?** `OutlinedTextField` has a visible border that doesn't match the design (dark rounded container without border). `TextField` with `TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent)` removes the bottom line.

---

## Choosing tabs

### `CategoryTabs` — horizontal tabs with underline indicator
```kotlin
CategoryTabs(
    tabs = listOf(AppTab.NOW_PLAYING, AppTab.UPCOMING, AppTab.TOP_RATED, AppTab.POPULAR),
    selectedTab = uiState.selectedTab,
    onTabSelected = viewModel::onTabSelected,
)
```
**What it does:** Row of text tabs with dynamic-width underline indicator. Uses `onTextLayout` to measure text width and match the indicator exactly.
**Use when:** Category filtering (HomeScreen movie tabs).

**Why not Material3 `TabRow`?** Material3 `TabRow` divides width equally among tabs. Our design has tabs hugged to text width with custom indicator. Also, `TabRow` has a fixed background divider that doesn't match the design.

**Why not `ScrollableTabRow`?** Same issue — Material tab components enforce their own styling (equal widths, built-in indicator, material elevation). Custom is simpler for this design.

### Inline tab Row — for detail screen sections
DetailScreen builds its own tab row with `Row + horizontalScroll`:
```kotlin
Row(
    modifier = Modifier.horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(20.dp),
) {
    DetailSection.entries.forEach { tab ->
        Column(modifier = Modifier.clickable { section = tab }) {
            Text(text = tab.label, ...)
            Box(modifier = Modifier.width(indicatorWidth).height(4.dp).background(...))
        }
    }
}
```
**Why not `CategoryTabs`?** CategoryTabs is typed to `AppTab` (movie categories). DetailScreen has its own `DetailSection` enum with different labels. The pattern is the same, but the data type differs.

---

## Choosing bottom navigation

### `AppBottomNavBar` — the app's bottom nav
```kotlin
AppBottomNavBar(
    selected = BottomDestination.HOME,
    onSelect = onDestinationSelected,
)
```
**What it does:** 3 tabs (Home, Search, Watch list) with icon + label. Active tab has tinted icon/text, inactive is gray.
**Use when:** All main screens (Home, Search, WatchList).
**Don't use when:** Sub-screens (Detail, WebView, ImageViewer) — these don't have bottom nav.

**Why not Material3 `NavigationBar`?** Material NavigationBar enforces its own styling (elevated surface, animated indicator, minimum touch targets). Our design uses a flat bar with a divider line and no indicator pill.

---

## Choosing overlay/badge composables

### `RatingBadge` — star + rating number
```kotlin
RatingBadge(rating = movie.voteAverage)
```
**What it does:** Semi-transparent rounded chip with star icon + rating number (e.g., "7.5").
**Use when:** Overlaying on images (DetailScreen backdrop), or inline next to metadata.
**Colors:** Orange star + text on dark translucent background (`AppColors.RatingBadgeBackground = 0x99232D3B`).

### `DetailMetaItem` — icon + text chip
```kotlin
DetailMetaItem(text = "2024", icon = Res.drawable.detail_calendar_icon)
```
**What it does:** Small metadata display with drawable icon + text.
**Use when:** DetailScreen meta row (year, runtime, genre).

### `MetaInfoRow` / `MetaInfoRowDrawable` — icon + text inline
```kotlin
MetaInfoRow(icon = Icons.Outlined.StarBorder, text = "7.5", tint = AppColors.AccentOrange)
MetaInfoRowDrawable(icon = Res.drawable.detail_calendar_icon, text = "2024")
```
**What it does:** Horizontal icon + text. Two variants: one for `ImageVector` icons, one for `DrawableResource` icons.
**Use when:** Inside `MovieListItem` for rating, genre, year, runtime.
**Why two variants?** Material icons (`Icons.Outlined.StarBorder`) are `ImageVector`. Custom icons from resources (`Res.drawable.*`) are `DrawableResource`. Different composable APIs.

---

## Pagination pattern (HomeScreen)

```kotlin
LaunchedEffect(gridState, uiState.movies.size, uiState.isListLoading, uiState.isPagingLoading) {
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
**Why `snapshotFlow` + `LaunchedEffect`?** `snapshotFlow` converts Compose snapshot state (scroll position) into a Kotlin Flow. `distinctUntilChanged` prevents duplicate triggers. `LaunchedEffect` with grid state as key restarts when the grid changes.

**Alternative — `LazyListState.isScrolledToEnd()` extension:** Simpler but less flexible. The `totalItems - 6` threshold pre-fetches 6 items before the end for smooth scrolling.

**Paging overlay:** When loading more pages, HomeScreen shows a centered "Loading more..." badge:
```kotlin
if (uiState.isPagingLoading) {
    Box(modifier = Modifier.fillMaxSize().background(AppColors.PagingOverlay), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(12.dp), color = AppColors.PagingBadge) {
            Text("Loading more...", color = Color.White, modifier = Modifier.padding(16.dp, 10.dp))
        }
    }
}
```

---

## Full screen layout comparison

| Screen | Root | Why |
|--------|------|-----|
| HomeScreen | `Scaffold` + `LazyVerticalGrid` | Grid layout, bottom nav, pagination |
| SearchScreen | `Scaffold` + `AnimatedContent` + `LazyColumn` | Bottom nav, animated state transitions |
| WatchListScreen | `Scaffold` + `AnimatedContent` + `LazyColumn` | Bottom nav, empty↔list transition |
| DetailScreen | `Column` + `verticalScroll` | Overlapping header, no bottom nav, mixed content |
| ImageViewerScreen | `Box` (no Scaffold) | Full-screen immersive, edge-to-edge |
| WebViewScreen | `Column` (no Scaffold) | Simple header + full-size WebView |
