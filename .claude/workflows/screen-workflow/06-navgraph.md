# Step 5: Wire Up in AppNavGraph

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/navigation/AppNavGraph.kt`

Add a new `composable<>` block inside the `NavHost` block.

## Pattern A: Simple screen (no route params)

```kotlin
composable<AppRoutes.MyScreen> {
    MyScreenScreen(
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

## Pattern B: Screen with route parameters

```kotlin
composable<AppRoutes.MyScreen> { backStackEntry ->
    val route = backStackEntry.toRoute<AppRoutes.MyScreen>()
    MyScreenScreen(
        itemId = route.itemId,      // pass to screen or let ViewModel handle it
        onBack = { navController.popBackStack() },
        onDestinationSelected = { dest ->
            navigateToDestination(navController, dest)
        },
    )
}
```

## Pattern C: Screen with JSON-decoded complex params

```kotlin
composable<AppRoutes.ImageViewer> { backStackEntry ->
    val route = backStackEntry.toRoute<AppRoutes.ImageViewer>()
    val images = runCatching {
        Json.decodeFromString<List<String>>(route.imagesJson)
    }.getOrDefault(emptyList())
    ImageViewerScreen(
        images = images,
        initialIndex = route.initialIndex,
        onBack = { navController.popBackStack() },
    )
}
```

## Transition animations

### Default — slide (inherited from NavHost)
```kotlin
// Just declare composable<> without transition params.
// Uses NavHost defaults: slide right on enter, slide left on exit.
composable<AppRoutes.MyScreen> { ... }
```

### Crossfade — for bottom nav tabs
```kotlin
composable<AppRoutes.MyScreen>(
    enterTransition = { NavTransitions.crossfadeIn() },
    exitTransition = { NavTransitions.crossfadeOut() },
) { ... }
```

### Current app transitions
| Route | Enter | Exit | Use case |
|-------|-------|------|----------|
| Home | crossfadeIn | crossfadeOut | Bottom nav tab |
| Search | crossfadeIn | crossfadeOut | Bottom nav tab |
| WatchList | crossfadeIn | crossfadeOut | Bottom nav tab |
| Detail | slideInForward | slideOutForward | Push navigation |
| Web | slideInForward | slideOutForward | Push navigation |
| ImageViewer | slideInForward | slideOutForward | Push navigation |

## How to navigate TO your screen

From another screen's NavGraph wiring:

```kotlin
// Simple:
navController.navigate(AppRoutes.MyScreen)

// With params:
navController.navigate(AppRoutes.MyScreen(itemId = 42))

// Bottom nav style (preserves back stack):
navController.navigate(AppRoutes.MyScreen) {
    popUpTo(AppRoutes.Home) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
```

## If adding a new bottom nav tab

1. Add entry to `BottomDestination` enum in `core/ui/components/AppBottomNavBar.kt`
2. Add case to `navigateToDestination()` in `AppNavGraph.kt`:
   ```kotlin
   BottomDestination.MY_TAB -> AppRoutes.MyScreen
   ```
3. Use crossfade transitions for the composable

---

## Why these choices?

### Why `AnimatedNavHost` over alternatives?

| Component | Animations | KMP support | Verdict |
|-----------|-----------|-------------|---------|
| **`AnimatedNavHost`** | Slide, fade, crossfade per-route | Yes (navigation-compose 2.9+) | **Use this** |
| `NavHost` (no animation) | None — instant swap | Yes | Feels jarring, avoid |
| `AnimatedContent` manually | Full control | Yes | Reinvents navigation, no back stack |
| Voyager Navigator | Built-in transitions | Yes | Different library |

`AnimatedNavHost` extends `NavHost` with enter/exit transition hooks per `composable<>` block. Default transitions apply to all routes; per-route overrides customize specific screens.

### Why crossfade for tabs, slide for push?
```kotlin
// Bottom nav tabs — crossfade:
composable<AppRoutes.Home>(
    enterTransition = { NavTransitions.crossfadeIn() },
    exitTransition = { NavTransitions.crossfadeOut() },
)

// Detail screen — slide (uses NavHost defaults):
composable<AppRoutes.Detail> { ... }
```
- **Crossfade for tabs:** Switching tabs is a lateral move — not forward/back. Slide would imply hierarchy. Crossfade feels instant and equal.
- **Slide for push:** Going to Detail/Web/ImageViewer is forward navigation. Slide right conveys "going deeper." Pop (back) slides left — spatial metaphor.

### Why `popUpTo` + `saveState` + `restoreState` for bottom nav?
```kotlin
navController.navigate(destination) {
    popUpTo(AppRoutes.Home) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
```
- `popUpTo(Home) { saveState = true }` — clears the back stack above Home, saving each tab's state
- `launchSingleTop = true` — prevents duplicate instances of the same tab
- `restoreState = true` — restores scroll position and data when returning to a tab
- Without this: every tab switch creates a new instance, losing scroll position and data

### Why `backStackEntry.toRoute<T>()` (not `arguments.getInt()`)?
```kotlin
// Our pattern — type-safe:
val route = backStackEntry.toRoute<AppRoutes.Detail>()
val movieId = route.movieId

// Legacy pattern — string-based:
val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
```
- `toRoute<T>()` returns the `@Serializable` data class directly — fully typed
- No string keys, no null checks, no default values for missing args
- Refactoring a parameter name in the route class updates automatically
