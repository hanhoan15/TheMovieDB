# Step 7: Update Test Fake

**File:** `composeApp/src/commonTest/kotlin/com/example/themoviedb/fake/FakeMovieRepository.kt`

When you add a new method to `MovieRepository`, you must also add it to `FakeMovieRepository`.

## Current FakeMovieRepository

```kotlin
package com.example.themoviedb.fake

import com.example.themoviedb.core.data.model.MovieCategory
import com.example.themoviedb.core.data.model.MovieDetailItem
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.data.repository.MovieRepository

class FakeMovieRepository : MovieRepository {

    val moviesByCategory = mutableMapOf<MovieCategory, List<MovieItem>>()
    val detailById = mutableMapOf<Int, MovieDetailItem?>()
    var searchResults: List<MovieItem> = emptyList()
    var throwOnGetMovies = false

    override suspend fun getMovies(category: MovieCategory, page: Int): List<MovieItem> {
        if (throwOnGetMovies) throw RuntimeException("Fake error")
        return moviesByCategory[category].orEmpty()
    }

    override suspend fun getMovieDetail(movieId: Int): MovieDetailItem? {
        return detailById[movieId]
    }

    override suspend fun searchMovies(query: String, page: Int): List<MovieItem> {
        return searchResults
    }
}
```

## How to add your new method

```kotlin
class FakeMovieRepository : MovieRepository {
    // existing...

    // Add configurable result:
    var newThingResult: NewThingItem? = null

    // Implement interface method:
    override suspend fun getNewThing(id: Int): NewThingItem? = newThingResult

    // For list methods:
    var newThingListResult: List<NewThingItem> = emptyList()

    override suspend fun getNewThingList(page: Int): List<NewThingItem> = newThingListResult
}
```

## Patterns

### Configurable by ID (map)
```kotlin
val thingById = mutableMapOf<Int, ThingItem?>()
override suspend fun getThing(id: Int): ThingItem? = thingById[id]
```
Usage in test:
```kotlin
val repo = FakeMovieRepository()
repo.thingById[42] = myThingItem
```

### Simple return value
```kotlin
var searchResults: List<MovieItem> = emptyList()
override suspend fun searchMovies(query: String, page: Int) = searchResults
```
Usage in test:
```kotlin
val repo = FakeMovieRepository()
repo.searchResults = listOf(movie(1, "Found"))
```

### Simulate failure
```kotlin
var throwOnGetThing = false
override suspend fun getThing(id: Int): ThingItem? {
    if (throwOnGetThing) throw RuntimeException("Fake error")
    return thingById[id]
}
```

## Test factory functions

**File:** `commonTest/.../fake/TestHelpers.kt`

```kotlin
fun movie(id: Int, title: String) = MovieItem(
    id = id, title = title, rating = 7.5,
    posterUrl = "https://example.com/poster.jpg",
    backdropUrl = "https://example.com/backdrop.jpg",
    releaseDate = "2024-01-01",
)

fun detail(id: Int, title: String) = MovieDetailItem(
    id = id, title = title, overview = "Overview",
    posterUrl = "https://example.com/poster.jpg",
    backdropUrl = "https://example.com/backdrop.jpg",
    originalLanguage = "en", releaseDate = "2024-01-01",
    voteAverage = 8.2, voteCount = 1200, budget = 100L, revenue = 200L,
)
```

Add similar factory functions if you create new domain models:
```kotlin
fun newThing(id: Int, name: String) = NewThingItem(
    id = id, name = name, ...
)
```

---

## Why these choices?

### Why manual fake (not mock)?
See [screen-workflow/08-test.md](../screen-workflow/08-test.md#why-manual-fakes-over-mocking-libraries) — manual fakes work on all KMP targets, have no plugin compatibility issues, and make test behavior explicit.

### Why mutable properties (not constructor params)?
```kotlin
// Our pattern — mutable properties:
class FakeMovieRepository : MovieRepository {
    var searchResults: List<MovieItem> = emptyList()
    override suspend fun searchMovies(query: String, page: Int) = searchResults
}

// Alternative — constructor config:
class FakeMovieRepository(
    private val searchResults: List<MovieItem> = emptyList()
) : MovieRepository { ... }
```
Mutable properties are better because:
- Can reconfigure mid-test without creating a new instance
- Matches the pattern of "set up → act → assert → reconfigure → act again"
- Default values mean most tests only set the fields they care about

### Why `MutableMap` for ID-based lookups?
```kotlin
val detailById = mutableMapOf<Int, MovieDetailItem?>()
override suspend fun getMovieDetail(movieId: Int) = detailById[movieId]
```
- Maps natural ID-based API behavior (GET `/movie/{id}`)
- Tests can pre-populate: `repo.detailById[42] = detail(42, "Title")`
- Returns `null` for unconfigured IDs — same as "not found" in real API
