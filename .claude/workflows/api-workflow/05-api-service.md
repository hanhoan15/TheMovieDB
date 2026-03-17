# Step 4: Add API Call to TmdbApiService

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/remote/TmdbApiService.kt`

## Current TmdbApiService

```kotlin
package com.example.themoviedb.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class TmdbApiService(private val httpClient: HttpClient) {

    suspend fun getMovies(category: MovieCategory, page: Int = 1): MoviesResponseDto { ... }
    suspend fun getMovieDetail(movieId: Int): MovieDetailDto { ... }
    suspend fun getMovieReviews(movieId: Int): MovieReviewsResponseDto { ... }
    suspend fun getMovieCredits(movieId: Int): MovieCreditsResponseDto { ... }
    suspend fun getMovieVideos(movieId: Int): MovieVideosResponseDto { ... }
    suspend fun getMovieImages(movieId: Int): MovieImagesResponseDto { ... }
    suspend fun getSimilarMovies(movieId: Int, page: Int = 1): MoviesResponseDto { ... }
    suspend fun searchMovies(query: String, page: Int = 1): MoviesResponseDto { ... }
}
```

## How to add a new endpoint

### GET with path parameter
```kotlin
suspend fun getNewThing(id: Int): NewThingDto {
    return httpClient.get("${ApiConstants.BASE_URL}movie/$id/new_thing") {
        parameter("api_key", ApiConstants.API_KEY)
        parameter("language", "en-US")
    }.body()
}
```

### GET with query parameters (search/filter)
```kotlin
suspend fun searchNewThing(query: String, page: Int = 1): NewThingResponseDto {
    return httpClient.get("${ApiConstants.BASE_URL}search/new_thing") {
        parameter("query", query)
        parameter("page", page)
        parameter("language", "en-US")
        parameter("include_adult", false)
    }.body()
}
```

### GET paginated list
```kotlin
suspend fun getNewThingList(page: Int = 1): NewThingResponseDto {
    return httpClient.get("${ApiConstants.BASE_URL}new_thing/popular") {
        parameter("language", "en-US")
        parameter("page", page)
    }.body()
}
```

## Real examples

### Simple GET — movie detail
```kotlin
suspend fun getMovieDetail(movieId: Int): MovieDetailDto {
    return httpClient.get("${ApiConstants.BASE_URL}movie/$movieId") {
        parameter("api_key", ApiConstants.API_KEY)
        parameter("language", "en-US")
    }.body()
}
```

### GET with sub-resource — movie reviews
```kotlin
suspend fun getMovieReviews(movieId: Int): MovieReviewsResponseDto {
    return httpClient.get("${ApiConstants.BASE_URL}movie/$movieId/reviews") {
        parameter("api_key", ApiConstants.API_KEY)
        parameter("language", "en-US")
        parameter("page", 1)
    }.body()
}
```

### GET with category mapping — movies by type
```kotlin
suspend fun getMovies(category: MovieCategory, page: Int = 1): MoviesResponseDto {
    val path = when (category) {
        MovieCategory.NOW_PLAYING -> "movie/now_playing"
        MovieCategory.TOP_RATED -> "movie/top_rated"
        MovieCategory.POPULAR -> "movie/popular"
        MovieCategory.UPCOMING -> "movie/upcoming"
    }
    return httpClient.get("${ApiConstants.BASE_URL}$path") {
        parameter("language", "en-US")
        parameter("page", page)
    }.body()
}
```

### Search endpoint
```kotlin
suspend fun searchMovies(query: String, page: Int = 1): MoviesResponseDto {
    return httpClient.get("${ApiConstants.BASE_URL}search/movie") {
        parameter("query", query)
        parameter("language", "en-US")
        parameter("page", page)
        parameter("include_adult", false)
    }.body()
}
```

## How auth works — you don't need to add it

Auth is already configured globally in `NetworkModule.kt`:
```kotlin
single {
    createPlatformHttpClient().config {
        install(ContentNegotiation) {
            json(get<Json>())
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            headers.append("Authorization", "Bearer ${ApiConstants.ACCESS_TOKEN}")
            headers.append("accept", "application/json")
        }
    }
}
```

Every request automatically includes:
- `Authorization: Bearer <token>` header
- `Content-Type: application/json` header
- JSON serialization/deserialization via `ContentNegotiation`

## How deserialization works — automatic

`.body<T>()` automatically deserializes the JSON response into your DTO type.
The `ContentNegotiation` plugin with `json(get<Json>())` handles this.

The `Json` instance is configured with:
```kotlin
Json {
    ignoreUnknownKeys = true    // new API fields don't crash
    isLenient = true            // tolerates minor JSON quirks
    coerceInputValues = true    // null for non-nullable fields → use default
}
```

## Rules
- All methods are `suspend` functions
- Use `httpClient.get(url) { parameter(...) }.body<DtoType>()`
- Base URL: `ApiConstants.BASE_URL` = `https://api.themoviedb.org/3/`
- No try/catch here — let the Repository handle errors
- Return the DTO type directly
- Don't add auth headers — they're global
- Import the DTO types at the top of the file

---

## Why these choices?

### Why Ktor over alternatives?

| HTTP Client | KMP support | Style | Verdict |
|-------------|-------------|-------|---------|
| **Ktor Client** | Full (all platforms via engine plugins) | Kotlin-first, suspend functions, plugin-based | **Use this** |
| Retrofit | Android-only (JVM) | Annotation-based interfaces | Can't use in KMP commonMain |
| OkHttp | Android/JVM only | Java-style, needs adapters for coroutines | Used as Ktor engine on Android only |
| NSURLSession | iOS only | Swift/ObjC interop | Used as Ktor engine on iOS only |
| Fuel | Partial KMP | Older, less maintained | Less ecosystem support |

Ktor Client is the standard for KMP networking:
- **Platform engines:** OkHttp on Android, Darwin on iOS — best native performance on each
- **Plugin system:** `ContentNegotiation` (JSON), `defaultRequest` (auth), `Logging` — composable
- **Suspend-native:** Every call is a suspend function — no callbacks, no adapters
- **Type-safe body:** `.body<MovieDetailDto>()` auto-deserializes using kotlinx.serialization

### Why one `TmdbApiService` class (not interfaces)?
```kotlin
// Our pattern — concrete class:
class TmdbApiService(private val httpClient: HttpClient) {
    suspend fun getMovieDetail(movieId: Int): MovieDetailDto { ... }
}

// Alternative — Retrofit-style interface:
interface TmdbApi {
    @GET("movie/{id}")
    suspend fun getMovieDetail(@Path("id") movieId: Int): MovieDetailDto
}
```
We use a concrete class because:
- Ktor doesn't support annotation-based interfaces like Retrofit
- The class is a thin wrapper — each method is 3-5 lines
- No need for an interface here — the Repository interface is the abstraction boundary
- Testing: we test at the Repository level with `FakeMovieRepository`, not at the API level

### Why no error handling in ApiService?
```kotlin
// ApiService — no try/catch:
suspend fun getMovieDetail(movieId: Int): MovieDetailDto {
    return httpClient.get("${ApiConstants.BASE_URL}movie/$movieId") { ... }.body()
}

// Repository — handles errors:
override suspend fun getMovieDetail(movieId: Int): MovieDetailItem? {
    return runCatching { apiService.getMovieDetail(movieId) }.getOrNull()?.toMovieDetailItem(...)
}
```
Separation of concerns:
- **ApiService** = "make the HTTP call, deserialize the response" (pure networking)
- **Repository** = "call the API, handle failures, map to domain models" (business logic)
- If ApiService caught errors, the Repository couldn't distinguish between "API returned empty" and "network failed"

### Why `parameter()` builder over string interpolation?
```kotlin
// Our pattern — parameter builder:
httpClient.get("${ApiConstants.BASE_URL}movie/$movieId") {
    parameter("language", "en-US")
    parameter("page", page)
}

// Alternative — manual URL building:
httpClient.get("${ApiConstants.BASE_URL}movie/$movieId?language=en-US&page=$page")
```
- `parameter()` auto-encodes special characters (spaces, unicode)
- Cleaner for optional parameters (just don't call `parameter()`)
- Easier to read with many query params
- Path params (`$movieId`) are fine as string interpolation since they're simple IDs
