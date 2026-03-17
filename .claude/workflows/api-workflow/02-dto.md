# Step 1: Create the DTO

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/model/dto/<Name>Dto.kt`

DTOs (Data Transfer Objects) mirror the exact JSON structure from the API.

## Template — Paginated list response

```kotlin
package com.example.themoviedb.core.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class <Name>ResponseDto(
    val page: Int,
    val results: List<<Name>ResultDto>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int,
)

@Serializable
data class <Name>ResultDto(
    val id: Int,
    val name: String? = null,
    @SerialName("some_snake_field") val someField: String? = null,
)
```

## Template — Single object response

```kotlin
@Serializable
data class <Name>Dto(
    val id: Int,
    val title: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
)
```

## Real examples from codebase

### MoviesResponseDto.kt — Paginated movie list
```kotlin
@Serializable
data class MoviesResponseDto(
    val page: Int,
    val results: List<MovieResultDto>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int,
)

@Serializable
data class MovieResultDto(
    val adult: Boolean = false,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    val id: Int,
    @SerialName("original_language") val originalLanguage: String = "",
    @SerialName("original_title") val originalTitle: String = "",
    val overview: String = "",
    val popularity: Double = 0.0,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("release_date") val releaseDate: String = "",
    val title: String = "",
    val video: Boolean = false,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
)
```

### MovieDetailDto.kt — Single detail object
```kotlin
@Serializable
data class MovieDetailDto(
    val adult: Boolean = false,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    val budget: Long = 0,
    val genres: List<GenreDto> = emptyList(),
    val id: Int,
    @SerialName("original_language") val originalLanguage: String = "",
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("release_date") val releaseDate: String = "",
    val revenue: Long = 0,
    val runtime: Int = 0,
    val title: String = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
)

@Serializable
data class GenreDto(
    val id: Int,
    val name: String,
)
```

## Rules

### Annotations
- **Always** `@Serializable` on every DTO class (kotlinx.serialization)
- **Never** use Moshi (`@Json`), Gson (`@SerializedName`), or any other serializer
- Use `@SerialName("snake_case")` when JSON key differs from Kotlin property

### Defaults and nullability
- Give **all fields** safe defaults (`= null`, `= ""`, `= 0`, `= emptyList()`, `= false`)
- The only exception: `id: Int` can be non-nullable if the API always provides it
- This prevents crashes when the API adds/removes fields

### Naming
- File name: `<Name>Dto.kt`
- Response wrapper: `<Name>ResponseDto`
- Individual items: `<Name>ResultDto` or `<Name>Dto`
- Keep Kotlin property names camelCase, use `@SerialName` for the JSON mapping

### Location
- All DTOs go in `core/data/model/dto/`
- Group related DTOs in one file (e.g., `MovieExtrasDto.kt` has reviews, credits, videos, images DTOs together)

---

## Why these choices?

### Why `kotlinx.serialization` over alternatives?

| Serializer | KMP support | Code generation | Verdict |
|-----------|-------------|-----------------|---------|
| **`kotlinx.serialization`** | Full (all platforms) | Compile-time plugin (no reflection) | **Use this** |
| Moshi | Android-only | Reflection or codegen (JVM-only) | Can't use in KMP |
| Gson | Android-only | Reflection (slow, no compile-time safety) | Legacy, never use |
| Jackson | JVM-only | Reflection | No iOS/WASM support |

`kotlinx.serialization` is the only serializer that works across all Kotlin targets (Android, iOS, Desktop, WASM). It generates serializers at compile time via a compiler plugin — no reflection needed, which matters for iOS (no reflection) and performance.

### Why `@SerialName` over naming strategy?
```kotlin
// Our pattern — explicit per-field:
@SerialName("poster_path") val posterPath: String? = null

// Alternative — global naming strategy:
Json { namingStrategy = JsonNamingStrategy.SnakeCase }
```
We use explicit `@SerialName` because:
- Self-documenting: you see the exact JSON key in the code
- Safe against typos: the compiler checks the Kotlin name, the annotation maps the JSON key
- Partial: not all TMDB fields follow `snake_case` consistently
- `JsonNamingStrategy` is experimental as of kotlinx.serialization 1.8.x

### Why defaults on every field?
```kotlin
val overview: String = "",        // not: val overview: String
val posterPath: String? = null,   // not: val posterPath: String?  (no default)
```
Without defaults, if the API omits a field or sends `null` for a non-nullable field, the app crashes with `MissingFieldException`. Defaults + `coerceInputValues = true` in the `Json` config make deserialization resilient:
- Missing field → uses default value
- `null` for non-nullable field → uses default value
- Unknown field → ignored (`ignoreUnknownKeys = true`)

### Why separate DTO and Domain Model?
- DTOs mirror JSON exactly — tied to API contract
- Domain models are what the UI needs — decoupled from API shape
- If the API changes a field name, only the DTO and mapper change — screens untouched
- DTOs have `@Serializable` overhead; domain models are plain data classes
