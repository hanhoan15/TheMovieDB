# Step 5: Add to Repository

Two files to update: the interface and the implementation.

---

## Part A: Add to the interface

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/repository/MovieRepository.kt`

### Current interface
```kotlin
package com.example.themoviedb.core.data.repository

import com.example.themoviedb.core.data.model.MovieCategory
import com.example.themoviedb.core.data.model.MovieDetailItem
import com.example.themoviedb.core.data.model.MovieItem

interface MovieRepository {
    suspend fun getMovies(category: MovieCategory, page: Int): List<MovieItem>
    suspend fun getMovieDetail(movieId: Int): MovieDetailItem?
    suspend fun searchMovies(query: String, page: Int): List<MovieItem>
}
```

### Add your method
```kotlin
interface MovieRepository {
    // existing...
    suspend fun getNewThing(id: Int): NewThingItem?
    suspend fun getNewThingList(page: Int): List<NewThingItem>
}
```

**Rules:**
- Return `List<T>` for collections (empty list on failure, not null)
- Return `T?` for single items (null on failure)
- All methods are `suspend`
- Use domain model types (not DTOs)

---

## Part B: Implement in TmdbMovieRepository

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/repository/TmdbMovieRepository.kt`

### Current implementation
```kotlin
class TmdbMovieRepository(
    private val apiService: TmdbApiService,
    private val imageBaseUrl: String = ApiConstants.IMAGE_BASE_URL,
) : MovieRepository {

    override suspend fun getMovies(category: MovieCategory, page: Int): List<MovieItem> {
        return apiService.getMovies(category, page)
            .results.mapNotNull { it.toMovieItem(imageBaseUrl) }
    }

    override suspend fun getMovieDetail(movieId: Int): MovieDetailItem? {
        val detail = runCatching { apiService.getMovieDetail(movieId) }.getOrNull()
            ?: return null

        val detailItem = detail.toMovieDetailItem(imageBaseUrl)

        val reviews = runCatching {
            apiService.getMovieReviews(movieId).results
                .map { it.toMovieReviewItem(imageBaseUrl) }
        }.getOrDefault(emptyList())

        val cast = runCatching {
            apiService.getMovieCredits(movieId).cast
                .map { it.toMovieCastItem(imageBaseUrl) }
        }.getOrDefault(emptyList())

        // ... more optional data ...

        return detailItem.copy(
            reviews = reviews,
            cast = cast,
            // ...
        )
    }

    override suspend fun searchMovies(query: String, page: Int): List<MovieItem> {
        return apiService.searchMovies(query, page)
            .results.mapNotNull { it.toMovieItem(imageBaseUrl) }
    }
}
```

### Pattern: Simple list endpoint
```kotlin
override suspend fun getNewThingList(page: Int): List<NewThingItem> {
    return apiService.getNewThingList(page)
        .results.mapNotNull { it.toNewThingItem(imageBaseUrl) }
}
```

### Pattern: Single item with error handling
```kotlin
override suspend fun getNewThing(id: Int): NewThingItem? {
    return try {
        val dto = apiService.getNewThing(id)
        dto.toNewThingItem(imageBaseUrl)
    } catch (e: Exception) {
        null
    }
}
```

### Pattern: Aggregate multiple API calls
```kotlin
override suspend fun getNewThing(id: Int): NewThingItem? {
    val main = runCatching { apiService.getNewThing(id) }.getOrNull()
        ?: return null

    val extras = runCatching {
        apiService.getNewThingExtras(id).results.map { it.toExtraItem() }
    }.getOrDefault(emptyList())

    return main.toNewThingItem(imageBaseUrl).copy(
        extras = extras,
    )
}
```

## Error handling patterns

### `runCatching { }.getOrNull()` — for required data
If this fails, the whole operation fails (return null):
```kotlin
val detail = runCatching { apiService.getMovieDetail(movieId) }.getOrNull()
    ?: return null   // can't show detail without core data
```

### `runCatching { }.getOrDefault(emptyList())` — for optional data
If this fails, just skip it (return empty):
```kotlin
val reviews = runCatching {
    apiService.getMovieReviews(movieId).results.map { it.toReviewItem() }
}.getOrDefault(emptyList())   // screen works without reviews
```

### General rule
- **Required data** (main detail): return `null` on failure → ViewModel shows error state
- **Optional data** (reviews, cast, similar): return empty → screen just hides that section
- **Never** let exceptions propagate to ViewModel — always catch in repository

---

## Why these choices?

### Why Repository interface + implementation?

| Approach | Testability | Flexibility | Verdict |
|----------|-------------|-------------|---------|
| **Interface + Impl** | Swap with `FakeMovieRepository` in tests | Can swap API impl without changing ViewModels | **Use this** |
| Concrete class only | Need Mokkery/mocking to test | Changing data source = changing ViewModel | Tightly coupled |
| Abstract class | Partial implementation reuse | Less flexible than interface | Only if you need shared logic |

The interface is the key abstraction boundary:
- ViewModels depend on `MovieRepository` (interface) — never `TmdbMovieRepository`
- Tests inject `FakeMovieRepository` with no mocking framework needed
- Could swap to a local database implementation without touching any ViewModel

### Why `single` scope in Koin (not `factory`)?
```kotlin
single<MovieRepository> { TmdbMovieRepository(get()) }   // ONE shared instance
// NOT:
factory<MovieRepository> { TmdbMovieRepository(get()) }   // new instance per injection
```
- Repositories may hold caches or in-memory state
- `WatchListRepository` holds the bookmark list — multiple instances = data lost between screens
- `single` ensures all ViewModels share the same repository → consistent data

### Why `runCatching` over `try/catch` in repository?

Both are valid. We use `runCatching` for consistency and conciseness:

```kotlin
// Pattern A — runCatching for required data:
val detail = runCatching { apiService.getMovieDetail(id) }.getOrNull() ?: return null

// Pattern B — runCatching for optional data:
val reviews = runCatching { apiService.getMovieReviews(id).results.map { ... } }
    .getOrDefault(emptyList())
```

**When `try/catch` is better:** If you need to log the error or handle specific exception types:
```kotlin
try {
    apiService.getMovieDetail(id)
} catch (e: ClientRequestException) {
    if (e.response.status == HttpStatusCode.NotFound) { /* handle 404 */ }
    null
}
```

### Why aggregate calls in `getMovieDetail` (not separate repository methods)?
```kotlin
// Our pattern — one repository method makes 5 API calls:
override suspend fun getMovieDetail(movieId: Int): MovieDetailItem? {
    val detail = runCatching { apiService.getMovieDetail(movieId) }.getOrNull() ?: return null
    val reviews = runCatching { apiService.getMovieReviews(movieId)... }.getOrDefault(emptyList())
    val cast = runCatching { apiService.getMovieCredits(movieId)... }.getOrDefault(emptyList())
    // ...
    return detailItem.copy(reviews = reviews, cast = cast, ...)
}

// Alternative — separate methods in ViewModel:
val detail = repository.getDetail(id)
val reviews = repository.getReviews(id)
val cast = repository.getCast(id)
```

We aggregate because:
- The DetailScreen always needs all this data together
- One method call = one loading state in the ViewModel (simple)
- Optional data failures don't block the whole screen (each wrapped in `runCatching`)
- If another screen needs just reviews, add a separate `getReviews()` method then

### Why `List<T>` return (not `Result<List<T>>`)?
```kotlin
suspend fun getMovies(category: MovieCategory, page: Int): List<MovieItem>  // empty on failure
// NOT:
suspend fun getMovies(...): Result<List<MovieItem>>  // caller must handle Result
```
- Simpler ViewModel code: `val movies = repository.getMovies(...)` — no unwrapping
- Empty list is a natural "no data" state — the UI shows `EmptyStateView`
- For single items, `T?` serves the same purpose: `null` = failed/not found
