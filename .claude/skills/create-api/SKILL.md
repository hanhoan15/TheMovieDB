---
name: create-api
description: Step-by-step flow to add a new TMDB API endpoint with DTO, mapper, and repository integration
user-invocable: true
argument-hint: "<endpointName>"
---

# Flow: Add a New API Endpoint

Add a new TMDB API endpoint: **$ARGUMENTS**

---

## Step 1: Create the DTO (Data Transfer Object)

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/model/dto/<Name>Dto.kt`

```kotlin
package com.example.themoviedb.core.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ${0}ResponseDto(
    val page: Int? = null,
    val results: List<${0}ItemDto> = emptyList(),
    @SerialName("total_pages") val totalPages: Int? = null,
    @SerialName("total_results") val totalResults: Int? = null,
)

@Serializable
data class ${0}ItemDto(
    val id: Int,
    val name: String? = null,
    // Map JSON fields to Kotlin properties
    // Use @SerialName for snake_case JSON keys:
    @SerialName("some_field") val someField: String? = null,
)
```

**Key rules:**
- Always `@Serializable` (kotlinx.serialization, NOT Moshi/Gson)
- Use `@SerialName("snake_case")` when JSON key differs from Kotlin property name
- Make all fields nullable with defaults (`= null`, `= emptyList()`) for API resilience
- DTOs live in `core/data/model/dto/` — they are raw API shapes, not domain models
- One file per response type

**Existing DTOs for reference:**
- `MoviesResponseDto` — paginated movie list
- `MovieDetailDto` — single movie detail with genres
- `MovieExtrasDto` — reviews, credits, videos, images responses

---

## Step 2: Add the API call to TmdbApiService

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/remote/TmdbApiService.kt`

Add a new suspend function:

```kotlin
// GET request with path parameter:
suspend fun get${0}(id: Int): ${0}ResponseDto {
    return client.get("${ApiConstants.BASE_URL}/3/movie/$id/<endpoint>") {
        parameter("language", "en-US")
    }.body()
}

// GET request with query parameters:
suspend fun search${0}(query: String, page: Int = 1): ${0}ResponseDto {
    return client.get("${ApiConstants.BASE_URL}/3/search/<resource>") {
        parameter("query", query)
        parameter("page", page)
        parameter("language", "en-US")
    }.body()
}

// GET request for a list with pagination:
suspend fun get${0}(page: Int = 1): ${0}ResponseDto {
    return client.get("${ApiConstants.BASE_URL}/3/<resource>/<endpoint>") {
        parameter("page", page)
        parameter("language", "en-US")
    }.body()
}
```

**Key rules:**
- All API methods are `suspend` functions
- Use `client.get(url) { parameter(...) }.body<T>()` — Ktor auto-deserializes via ContentNegotiation
- Auth header is already configured globally in `NetworkModule.kt` (`Bearer` token in `defaultRequest`)
- Base URL: `ApiConstants.BASE_URL` = `https://api.themoviedb.org`
- Return the DTO type directly (deserialization is automatic)
- No try/catch here — let the caller (Repository) handle errors

**Existing methods for reference:**
```kotlin
suspend fun getMovies(category: String, page: Int): MoviesResponseDto
suspend fun getMovieDetail(movieId: Int): MovieDetailDto
suspend fun getMovieReviews(movieId: Int): ReviewsResponseDto
suspend fun getMovieCredits(movieId: Int): CreditsResponseDto
suspend fun getMovieVideos(movieId: Int): VideosResponseDto
suspend fun getMovieImages(movieId: Int): ImagesResponseDto
suspend fun getSimilarMovies(movieId: Int): MoviesResponseDto
suspend fun searchMovies(query: String, page: Int): MoviesResponseDto
```

---

## Step 3: Create the domain model

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/model/<Name>Item.kt`

```kotlin
package com.example.themoviedb.core.data.model

data class ${0}Item(
    val id: Int,
    val name: String,
    // Clean domain fields — no nullability where not needed,
    // no @SerialName, no serialization annotations
)
```

**Key rules:**
- Domain models are plain `data class` — NO `@Serializable` annotation
- Non-nullable where the app requires the data (mapper provides defaults)
- Live in `core/data/model/` (not in `dto/`)
- These are what ViewModels and Screens work with

**Existing domain models:**
- `MovieItem` — id, title, rating, posterUrl, backdropUrl, releaseDate
- `MovieDetailItem` — full detail with overview, genres, cast, reviews, etc.
- `MovieReviewItem`, `MovieCastItem`, `MovieTrailerItem`

---

## Step 4: Create the mapper

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/mapper/MovieMapper.kt`

Add extension functions to the existing mapper file:

```kotlin
// DTO -> Domain mapping as extension function:
fun ${0}ItemDto.to${0}Item(): ${0}Item {
    return ${0}Item(
        id = id,
        name = name.orEmpty(),          // provide defaults for nullables
    )
}

// List mapping helper:
fun List<${0}ItemDto>.to${0}Items(): List<${0}Item> {
    return map { it.to${0}Item() }
}
```

**Key rules:**
- Mappers are extension functions on the DTO type
- Always provide sensible defaults for nullable DTO fields (`.orEmpty()`, `?: 0`, `?: "Unknown"`)
- Use `buildImageUrl(ApiConstants.IMAGE_BASE_URL, dto.posterPath)` for image URLs
- Add to the existing `MovieMapper.kt` file — don't create a new mapper file unless it's a completely different domain

**Existing mapper patterns:**
```kotlin
fun MovieResultDto.toMovieItem() = MovieItem(...)
fun MovieDetailDto.toMovieDetailItem(...) = MovieDetailItem(...)
fun ReviewDto.toMovieReviewItem() = MovieReviewItem(...)
fun CastDto.toMovieCastItem() = MovieCastItem(...)
fun VideoDto.toMovieTrailerItem() = MovieTrailerItem(...)
fun buildImageUrl(base: String, path: String?): String?
fun buildAvatarUrl(path: String?): String?
```

---

## Step 5: Add to Repository interface

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/repository/MovieRepository.kt`

```kotlin
interface MovieRepository {
    // existing methods...
    suspend fun get${0}(id: Int): ${0}Item?
}
```

---

## Step 6: Implement in TmdbMovieRepository

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/repository/TmdbMovieRepository.kt`

```kotlin
override suspend fun get${0}(id: Int): ${0}Item? {
    return try {
        val response = apiService.get${0}(id)
        response.to${0}Item()  // or response.results.to${0}Items()
    } catch (e: Exception) {
        null  // return null on failure, let ViewModel handle fallback
    }
}
```

**Key rules:**
- Wrap API calls in try/catch — return `null` or `emptyList()` on failure
- Map DTOs to domain models here (not in ViewModel)
- Repository is the boundary between network and domain layers

**Existing pattern from TmdbMovieRepository:**
```kotlin
class TmdbMovieRepository(
    private val apiService: TmdbApiService,
) : MovieRepository {
    override suspend fun getMovies(category: MovieCategory, page: Int): List<MovieItem> {
        return try {
            apiService.getMovies(category.apiPath, page)
                .results.map { it.toMovieItem() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
```

---

## Step 7: Update FakeMovieRepository (for tests)

**File:** `composeApp/src/commonTest/kotlin/com/example/themoviedb/fake/FakeMovieRepository.kt`

Add the new method:

```kotlin
class FakeMovieRepository : MovieRepository {
    // existing fakes...

    var ${0}Result: ${0}Item? = null

    override suspend fun get${0}(id: Int): ${0}Item? = ${0}Result
}
```

---

## Step 8: Verify

```bash
./gradlew composeApp:compileDebugKotlinAndroid composeApp:compileKotlinIosSimulatorArm64
./gradlew composeApp:testDebugUnitTest
```

---

## Summary: Data flows from API to Screen

```
TMDB API (JSON)
    ↓ Ktor HttpClient (auto-deserialize)
TmdbApiService (returns DTO)
    ↓
TmdbMovieRepository (maps DTO → Domain, handles errors)
    ↓
ViewModel (calls repository, updates StateFlow)
    ↓
Screen (collects StateFlow, renders UI)
```
