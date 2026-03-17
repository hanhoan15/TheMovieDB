---
name: create-dto-mapper
description: Step-by-step flow to create a DTO for an API response and its mapper to a domain model
user-invocable: true
argument-hint: "<ModelName>"
---

# Flow: Create DTO + Domain Model + Mapper

Create data models for: **$ARGUMENTS**

---

## Step 1: Create the DTO (what the API returns)

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/model/dto/${0}Dto.kt`

```kotlin
package com.example.themoviedb.core.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ${0}Dto(
    val id: Int? = null,
    val name: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    // All fields nullable with defaults for API resilience
)
```

**Key rules:**
- `@Serializable` from `kotlinx.serialization` (never Moshi/Gson)
- `@SerialName("snake_case")` for JSON keys that differ from Kotlin property names
- **All fields nullable** with safe defaults (`= null`, `= emptyList()`, `= 0`)
- DTOs mirror the JSON structure exactly — don't rename/restructure
- File goes in `core/data/model/dto/`

**Existing DTO examples:**
```kotlin
// Paginated response wrapper:
@Serializable
data class MoviesResponseDto(
    val page: Int? = null,
    val results: List<MovieResultDto> = emptyList(),
    @SerialName("total_pages") val totalPages: Int? = null,
)

// Nested object:
@Serializable
data class MovieDetailDto(
    val id: Int,
    val title: String? = null,
    val genres: List<GenreDto> = emptyList(),
    val runtime: Int? = null,
)

@Serializable
data class GenreDto(val id: Int, val name: String? = null)
```

---

## Step 2: Create the domain model (what the app uses)

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/model/${0}Item.kt`

```kotlin
package com.example.themoviedb.core.data.model

data class ${0}Item(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val rating: Double,
    val releaseDate: String,
)
```

**Key rules:**
- Plain `data class` — NO `@Serializable`, no serialization annotations
- Non-nullable for fields the UI always needs (mapper provides defaults)
- Nullable only for truly optional fields (e.g., `imageUrl`)
- Clean, app-friendly names (not API names)
- File goes in `core/data/model/`

**Existing domain models:**
```kotlin
data class MovieItem(
    val id: Int, val title: String, val rating: Double,
    val posterUrl: String, val backdropUrl: String?, val releaseDate: String,
)

data class MovieDetailItem(
    val id: Int, val title: String, val overview: String,
    val posterUrl: String?, val backdropUrl: String?,
    val voteAverage: Double, val voteCount: Int, ...,
    val reviews: List<MovieReviewItem>, val cast: List<MovieCastItem>,
    val trailers: List<MovieTrailerItem>, val images: List<String>,
    val similarMovies: List<MovieItem>,
)
```

---

## Step 3: Write the mapper

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/mapper/MovieMapper.kt`

Add to the existing file:

```kotlin
fun ${0}Dto.to${0}Item(): ${0}Item {
    return ${0}Item(
        id = id ?: 0,
        name = name.orEmpty(),
        imageUrl = buildImageUrl(ApiConstants.IMAGE_BASE_URL, posterPath),
        rating = voteAverage ?: 0.0,
        releaseDate = releaseDate.orEmpty(),
    )
}
```

**Mapping patterns used in this project:**

```kotlin
// Null safety with defaults:
title.orEmpty()                    // String? → String
voteAverage ?: 0.0                 // Double? → Double
runtime ?: 0                       // Int? → Int
genres.map { it.name.orEmpty() }   // List<GenreDto> → List<String>

// Image URL construction:
buildImageUrl(ApiConstants.IMAGE_BASE_URL, posterPath)
// Returns: "https://image.tmdb.org/t/p/w500/abc.jpg" or null

// Avatar URL (handles TMDB's mixed format):
buildAvatarUrl(avatarPath)
// Strips leading "/" if path starts with "/http", else prepends IMAGE_BASE_URL

// Numeric formatting:
voteAverage.toOneDecimalString()   // 7.456 → "7.5"
voteAverage.toFiveStarString()     // 7.0 → "★★★★☆"

// Date extraction:
releaseDate.releaseYear()          // "2024-01-15" → "2024"

// Genre label:
genres.primaryGenreLabel()         // List<GenreDto> → "Action" (first genre or "Unknown")

// Trailer URL:
buildTrailerWatchUrl("YouTube", "abc123")  // → "https://www.youtube.com/watch?v=abc123"
buildTrailerThumbnailUrl("YouTube", "abc123")  // → "https://img.youtube.com/vi/abc123/hqdefault.jpg"
```

---

## Step 4: Write mapper tests

**File:** `composeApp/src/commonTest/kotlin/com/example/themoviedb/core/data/MovieMapperTest.kt`

Add to existing test file:

```kotlin
@Test
fun ${0}Dto_maps_correctly() {
    val dto = ${0}Dto(
        id = 1,
        name = "Test",
        posterPath = "/test.jpg",
        voteAverage = 7.5,
        releaseDate = "2024-06-15",
    )
    val item = dto.to${0}Item()
    assertEquals(1, item.id)
    assertEquals("Test", item.name)
    assertEquals(7.5, item.rating)
}

@Test
fun ${0}Dto_handles_nulls() {
    val dto = ${0}Dto()  // all defaults
    val item = dto.to${0}Item()
    assertEquals(0, item.id)
    assertEquals("", item.name)
    assertEquals(0.0, item.rating)
}
```

**Key rules:**
- Always test the happy path AND null/missing data
- Mapper tests are fast (no coroutines, no mocking)
- Run: `./gradlew composeApp:testDebugUnitTest --tests "*.MovieMapperTest"`

---

## Step 5: Verify

```bash
./gradlew composeApp:compileDebugKotlinAndroid
./gradlew composeApp:testDebugUnitTest --tests "*.MovieMapperTest"
```
