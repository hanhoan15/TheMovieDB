# Step 4: Add a Navigation Route

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/navigation/AppRoutes.kt`

## Current routes in the app

```kotlin
package com.example.themoviedb.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoutes {
    @Serializable data object Home : AppRoutes
    @Serializable data object Search : AppRoutes
    @Serializable data object WatchList : AppRoutes
    @Serializable data class Detail(val movieId: Int) : AppRoutes
    @Serializable data class Web(val url: String) : AppRoutes
    @Serializable data class ImageViewer(val imagesJson: String, val initialIndex: Int) : AppRoutes
}
```

## How to add your route

### No parameters — use `data object`
```kotlin
@Serializable
data object MyScreen : AppRoutes
```

### With parameters — use `data class`
```kotlin
@Serializable
data class MyScreen(val itemId: Int) : AppRoutes
```

### With complex parameters — JSON-encode to String
```kotlin
@Serializable
data class MyScreen(
    val itemsJson: String,      // JSON string of List<String>
    val initialIndex: Int,
) : AppRoutes
```

Encode before navigating:
```kotlin
val json = Json.encodeToString(myList)
navController.navigate(AppRoutes.MyScreen(itemsJson = json, initialIndex = 0))
```

Decode in NavGraph:
```kotlin
val items = runCatching {
    Json.decodeFromString<List<String>>(route.itemsJson)
}.getOrDefault(emptyList())
```

## Rules
- Every route must have `@Serializable` annotation
- Only primitive types (`Int`, `String`, `Boolean`, `Long`, `Double`) as params
- For lists or objects: serialize to JSON String
- Route goes inside the `sealed interface AppRoutes` block

---

## Why these choices?

### Why `@Serializable` sealed interface routes over alternatives?

| Approach | Type-safety | KMP support | Verdict |
|----------|-------------|-------------|---------|
| **`@Serializable` sealed interface** | Full compile-time safety | Yes (Compose Navigation 2.9+) | **Use this** |
| String routes (`"detail/{movieId}"`) | None — runtime crashes on typo | Yes | Legacy, error-prone |
| Enum routes | Limited — can't pass parameters | Yes | Only for parameterless screens |
| Voyager/Decomposition | Full | Yes | Different navigation library entirely |

`@Serializable` routes give compile-time guarantees:
- `AppRoutes.Detail(movieId = 42)` — IDE autocomplete, type-checked params
- No string parsing, no `navArgument` boilerplate, no runtime type mismatches
- Refactoring a param name updates all call sites automatically

### Why `data object` vs `data class`?
```kotlin
@Serializable data object Home : AppRoutes       // no parameters
@Serializable data class Detail(val movieId: Int) : AppRoutes  // with parameters
```
- `data object` = singleton, no constructor — used for screens with no input
- `data class` = constructor params — used when the screen needs input from navigation
- Never use `object` (non-data) — `data object` provides correct `equals`/`hashCode`/`toString`

### Why JSON-encode complex params (not Parcelable/custom serializer)?
```kotlin
@Serializable data class ImageViewer(val imagesJson: String, val initialIndex: Int) : AppRoutes
```
- Navigation args only support primitive types (`Int`, `String`, `Boolean`, `Long`, `Double`)
- `Parcelable` is Android-only — can't use in KMP commonMain
- JSON-encoding to `String` is KMP-compatible and uses the same `kotlinx.serialization` we already have
- The trade-off: slightly more code at navigation site, but fully cross-platform

### Why sealed interface (not sealed class)?
```kotlin
sealed interface AppRoutes { ... }   // interface
// NOT:
sealed class AppRoutes { ... }       // class
```
- `sealed interface` allows `data object` members (Kotlin 1.9+)
- No unnecessary base class constructor overhead
- Routes don't share behavior or state — they're just type markers with data
- `sealed interface` is the modern Kotlin idiom for closed type hierarchies
