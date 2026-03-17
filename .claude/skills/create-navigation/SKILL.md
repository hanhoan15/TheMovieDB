---
name: create-navigation
description: Step-by-step guide to add a new navigation route with transitions and type-safe arguments
user-invocable: true
argument-hint: "<RouteName>"
---

# Flow: Add Navigation Route

Add navigation for: **$ARGUMENTS**

---

## Step 1: Define the route

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/navigation/AppRoutes.kt`

```kotlin
@Serializable
sealed interface AppRoutes {
    // Existing routes...

    // No parameters:
    @Serializable
    data object $ARGUMENTS : AppRoutes

    // With simple parameters:
    @Serializable
    data class $ARGUMENTS(val itemId: Int) : AppRoutes

    // With complex parameters (lists → JSON string):
    @Serializable
    data class $ARGUMENTS(
        val itemsJson: String,
        val initialIndex: Int,
    ) : AppRoutes
}
```

**Key rules:**
- Every route must be `@Serializable` (kotlinx.serialization)
- `data object` for routes with no params
- `data class` for routes with params
- Only primitive types + String allowed as params
- For complex data (lists, objects), JSON-encode to String:
  ```kotlin
  val json = Json.encodeToString(myList)
  navController.navigate(AppRoutes.MyRoute(itemsJson = json))
  ```

**Existing routes:**
```kotlin
@Serializable data object Home : AppRoutes
@Serializable data object Search : AppRoutes
@Serializable data object WatchList : AppRoutes
@Serializable data class Detail(val movieId: Int) : AppRoutes
@Serializable data class Web(val url: String) : AppRoutes
@Serializable data class ImageViewer(val imagesJson: String, val initialIndex: Int) : AppRoutes
```

---

## Step 2: Add composable to NavGraph

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/navigation/AppNavGraph.kt`

### No parameters:
```kotlin
composable<AppRoutes.$ARGUMENTS> {
    ${ARGUMENTS}Screen(
        onBack = { navController.popBackStack() },
        onDestinationSelected = { dest ->
            navigateToDestination(navController, dest)
        },
    )
}
```

### With parameters:
```kotlin
composable<AppRoutes.$ARGUMENTS> { backStackEntry ->
    val route = backStackEntry.toRoute<AppRoutes.$ARGUMENTS>()
    ${ARGUMENTS}Screen(
        itemId = route.itemId,
        onBack = { navController.popBackStack() },
    )
}
```

### With JSON-decoded complex params:
```kotlin
composable<AppRoutes.$ARGUMENTS> { backStackEntry ->
    val route = backStackEntry.toRoute<AppRoutes.$ARGUMENTS>()
    val items = runCatching {
        Json.decodeFromString<List<String>>(route.itemsJson)
    }.getOrDefault(emptyList())
    ${ARGUMENTS}Screen(
        items = items,
        initialIndex = route.initialIndex,
        onBack = { navController.popBackStack() },
    )
}
```

---

## Step 3: Choose transition animations

Add transition parameters to `composable<>`:

### Default (slide forward/back) — inherited from NavHost:
```kotlin
// No need to specify — uses NavHost defaults:
// enter: slideInForward, exit: slideOutForward
// popEnter: slideInBack, popExit: slideOutBack
composable<AppRoutes.$ARGUMENTS> { ... }
```

### Crossfade (for bottom nav tabs):
```kotlin
composable<AppRoutes.$ARGUMENTS>(
    enterTransition = { NavTransitions.crossfadeIn() },
    exitTransition = { NavTransitions.crossfadeOut() },
) { ... }
```

**Available transitions in `NavTransitions.kt`:**
```kotlin
NavTransitions.slideInForward()   // slide from right + fade in
NavTransitions.slideOutForward()  // slide to left + fade out
NavTransitions.slideInBack()      // slide from left + fade in
NavTransitions.slideOutBack()     // slide to right + fade out
NavTransitions.crossfadeIn()      // fade in (300ms)
NavTransitions.crossfadeOut()     // fade out (300ms)
```

**When to use which:**
- **Slide** (default): Detail screens, push navigation
- **Crossfade**: Bottom nav tabs (Home, Search, WatchList)
- Custom: Override per-route as needed

---

## Step 4: Navigate TO this route

From another screen's NavGraph wiring:

```kotlin
// Simple navigation:
navController.navigate(AppRoutes.$ARGUMENTS)

// With parameters:
navController.navigate(AppRoutes.$ARGUMENTS(itemId = 42))

// With JSON-encoded list:
val json = Json.encodeToString(myList)
navController.navigate(AppRoutes.$ARGUMENTS(itemsJson = json, initialIndex = 0))

// Bottom nav style (saves/restores state):
navController.navigate(AppRoutes.$ARGUMENTS) {
    popUpTo(AppRoutes.Home) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
```

**Key rules:**
- `popBackStack()` to go back
- For bottom nav tabs, use the `navigateToDestination()` helper which handles `popUpTo`, `launchSingleTop`, and `restoreState`
- If adding a new bottom tab, also update `BottomDestination` enum in `core/ui/components/AppBottomNavBar.kt`

---

## Step 5: Verify

```bash
./gradlew composeApp:compileDebugKotlinAndroid composeApp:compileKotlinIosSimulatorArm64
```

Navigate to the new screen in the running app to verify transitions work.
