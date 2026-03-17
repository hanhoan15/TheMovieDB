# Pattern: Full-Screen Viewer Screen

> Reference implementation: `feature/imageviewer/ImageViewerScreen.kt`

## When to use

- Screen is a **full-screen immersive** experience (edge-to-edge, no chrome)
- Displays **pageable media** (images, cards, documents)
- Has **no ViewModel** — stateless composable with data from route params
- Has **no bottom nav**, no Scaffold
- Uses **complex route params** (JSON-encoded lists)

---

## File checklist

```
feature/<name>/
└── <Name>Screen.kt             ← Stateless composable only (NO ViewModel)
core/navigation/AppRoutes.kt     ← data class with JSON-encoded params
core/navigation/AppNavGraph.kt   ← composable with slide transitions, JSON decoding
```

**No ViewModel, no Koin registration, no test file.** The screen is purely presentational.

---

## Layout blueprint

```
Box (fillMaxSize, background = Color.Black)
│
├── AsyncImage (current image, blurred 24dp — background layer)
│
├── Box (semi-transparent overlay, Color(0x66000000))
│
└── Column (fillMaxSize)
    │
    ├── Row (top bar)
    │   ├── IconButton (back arrow, Color.White)
    │   ├── Spacer
    │   └── Text ("1/10" counter, Color.White)
    │
    └── HorizontalPager (pageCount = Int.MAX_VALUE)
        └── page { AsyncImage(contentScale = Fit) }
```

### Why `Box` (not `Scaffold`)

| Layout | Chrome | Use case |
|--------|--------|----------|
| **Raw `Box`** | None — edge-to-edge black background | Full-screen immersive (image/video viewers) |
| `Scaffold` | Status bar insets, bottom bar slot | Standard screens with system chrome |

`Scaffold` adds padding for system bars and provides slots for top/bottom bars. A viewer screen wants the image to fill the entire screen with no padding. `Box` with `Color.Black` background provides a clean canvas.

### Why `HorizontalPager` (not `LazyRow`)

| Component | Behavior | Use case |
|-----------|----------|----------|
| **`HorizontalPager`** | Snap-to-page, one item at a time | Media viewers, onboarding carousels |
| `LazyRow` | Free-scroll, items stream by | Horizontal lists (featured carousel) |
| `VerticalPager` | Snap-to-page, vertical | TikTok-style vertical media feed |

`HorizontalPager` snaps to exactly one page — essential for image viewing. `LazyRow` would let images free-scroll past each other, which feels wrong for a gallery.

---

## Component choices

| Component | Why | Alternative | When to switch |
|-----------|-----|-------------|---------------|
| `AsyncImage` (raw Coil) | Full image display, no shimmer needed | `AsyncImageWithPlaceholder` | When loading states matter (detail, grid) |
| `HorizontalPager` | Snap-to-page horizontal swiping | `VerticalPager` | When media scrolls vertically (video feed) |
| Material `Icon` (back arrow) | Simple back navigation | `ScreenTopBar` | When you need a full top bar with title |

**Why raw `AsyncImage` (not `AsyncImageWithPlaceholder`)?**
- Viewer images are usually already cached from the previous screen (detail)
- The blurred background layer provides a natural loading placeholder
- Shimmer animation would look wrong on a full-screen black background

---

## Infinite pager pattern

```kotlin
val images: List<String>    // passed from route
val size = images.size.coerceAtLeast(1)

// Calculate start page near middle of Int range, aligned to image index
val startPage = remember {
    val base = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % size)
    base + initialIndex.coerceIn(0, size - 1)
}

val pagerState = rememberPagerState(
    initialPage = startPage,
    pageCount = { Int.MAX_VALUE },
)

// Current image index (modulo for circular wrapping)
val currentPosition = ((pagerState.currentPage % size) + size) % size

HorizontalPager(state = pagerState) { page ->
    val index = ((page % size) + size) % size
    AsyncImage(
        model = images[index],
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize(),
    )
}
```

**Why `Int.MAX_VALUE` pages?**
- Creates the illusion of infinite scrolling in both directions
- User can swipe left from image 0 to reach the last image
- Starting near `Int.MAX_VALUE / 2` provides ~1 billion pages in each direction

**Why the double-modulo `((page % size) + size) % size`?**
- `page % size` can return negative values for negative numbers
- Adding `size` then taking modulo again guarantees a positive index
- Example: `(-1 % 3) = -1`, but `((-1 % 3) + 3) % 3 = 2` (correct)

---

## Blur background layer

```kotlin
// Background: blurred version of current image
AsyncImage(
    model = images[currentPosition],
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = Modifier
        .fillMaxSize()
        .blur(24.dp),                           // Gaussian blur
)

// Semi-transparent overlay to darken blurred background
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(Color(0x66000000)),         // 40% black overlay
)
```

**Why blurred background?** Without it, the space around `ContentScale.Fit` images would be pure black. The blurred background adds depth and context — the dominant colors of the current image fill the screen.

---

## No ViewModel — stateless pattern

```kotlin
@Composable
fun <Name>Screen(
    items: List<String>,       // decoded from JSON route param
    initialIndex: Int,
    onBack: () -> Unit,
) {
    // All state is local (pagerState)
    val pagerState = rememberPagerState(...)

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // ... purely presentational
    }
}
```

**Why no ViewModel?**
- No API calls — data arrives fully resolved from the route
- No business logic — just displaying images
- No state mutations — read-only viewing
- `pagerState` is the only state, and it's purely UI (current page index)

**When to add a ViewModel:** If the viewer needs to load data (e.g., fetch high-res version on demand), save state (e.g., "last viewed" tracking), or perform actions (e.g., download, share).

---

## Navigation wiring — JSON route params

```kotlin
// AppRoutes.kt
@Serializable
data class <Name>(
    val itemsJson: String,      // JSON-encoded List<String>
    val initialIndex: Int,
) : AppRoutes

// Navigating TO the viewer (from DetailScreen):
val json = Json.encodeToString(imageUrls)
navController.navigate(AppRoutes.<Name>(itemsJson = json, initialIndex = clickedIndex))

// AppNavGraph.kt — decode JSON, pass to Screen
composable<AppRoutes.<Name>> { backStackEntry ->
    val route = backStackEntry.toRoute<AppRoutes.<Name>>()
    val items = runCatching {
        Json.decodeFromString<List<String>>(route.itemsJson)
    }.getOrDefault(emptyList())

    <Name>Screen(
        items = items,
        initialIndex = route.initialIndex,
        onBack = { navController.popBackStack() },
    )
}
```

**Why JSON-encode the list?**
- Navigation route params only support primitives (`Int`, `String`, `Boolean`, `Long`, `Double`)
- `List<String>` can't be a route param directly
- `Json.encodeToString()` converts the list to a single String param
- `runCatching { Json.decodeFromString() }.getOrDefault(emptyList())` handles malformed JSON gracefully

---

## Common pitfalls

| Pitfall | Symptom | Fix |
|---------|---------|-----|
| Using `Scaffold` | System bar padding ruins edge-to-edge layout | Use raw `Box(background = Color.Black)` |
| `LazyRow` instead of `HorizontalPager` | Images free-scroll instead of snapping | Use `HorizontalPager` for snap-to-page |
| `pageCount = images.size` (not infinite) | Can't swipe left from first image | Use `Int.MAX_VALUE` with modulo indexing |
| Simple `page % size` without double-modulo | Negative index crash for edge cases | Use `((page % size) + size) % size` |
| `AsyncImageWithPlaceholder` in pager | Shimmer animation on every page swipe | Use raw `AsyncImage` — images usually cached |
| `ContentScale.Crop` in pager | Image edges clipped | Use `ContentScale.Fit` — show full image |
| Not wrapping JSON decode in `runCatching` | Crash on malformed route param | Always `.getOrDefault(emptyList())` |
