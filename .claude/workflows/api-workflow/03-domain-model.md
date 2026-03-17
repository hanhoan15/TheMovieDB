# Step 2: Create the Domain Model

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/model/<Name>Item.kt`

Domain models are what ViewModels and Screens work with. They are clean, typed, and decoupled from the API shape.

## Template

```kotlin
package com.example.themoviedb.core.data.model

data class <Name>Item(
    val id: Int,
    val title: String,
    val imageUrl: String?,
    val rating: Double,
)
```

## Real examples from codebase

### MovieItem.kt — Simple list item
```kotlin
data class MovieItem(
    val id: Int,
    val title: String,
    val rating: Double,
    val posterUrl: String,          // full URL, not a path
    val backdropUrl: String?,       // nullable: not all movies have backdrops
    val releaseDate: String = "",
    val genreIds: List<Int> = emptyList(),
)
```

### MovieDetailItem.kt — Rich detail with nested lists
```kotlin
data class MovieDetailItem(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val originalLanguage: String,
    val releaseDate: String,
    val runtime: Int = 0,
    val genres: List<String> = emptyList(),      // mapped from GenreDto.name
    val voteAverage: Double,
    val voteCount: Int,
    val budget: Long,
    val revenue: Long,
    val images: List<String> = emptyList(),      // full image URLs
    val reviews: List<MovieReviewItem> = emptyList(),
    val cast: List<MovieCastItem> = emptyList(),
    val trailers: List<MovieTrailerItem> = emptyList(),
    val similarMovies: List<MovieItem> = emptyList(),
)

data class MovieReviewItem(
    val author: String,
    val content: String,
    val rating: Double?,
    val avatarUrl: String?,
)

data class MovieCastItem(
    val name: String,
    val profileUrl: String?,
)

data class MovieTrailerItem(
    val name: String,
    val type: String,
    val watchUrl: String,
    val thumbnailUrl: String?,
)
```

## DTO vs Domain Model — key differences

| Aspect | DTO | Domain Model |
|--------|-----|-------------|
| Annotation | `@Serializable` | None |
| Nullability | Everything nullable with defaults | Non-null where UI requires it |
| Naming | Matches JSON (`posterPath`, `voteAverage`) | App-friendly (`posterUrl`, `rating`) |
| URLs | Raw path (`/abc.jpg`) | Full URL (`https://image.tmdb.org/t/p/w500/abc.jpg`) |
| Nested types | Other DTOs (`List<GenreDto>`) | Simple types (`List<String>`) |
| Location | `core/data/model/dto/` | `core/data/model/` |

## Rules
- **No** `@Serializable` annotation — domain models are never serialized directly
- Non-nullable for fields the UI always displays (mapper provides defaults)
- Nullable only for truly optional data (`avatarUrl`, `backdropUrl`)
- Use full URLs (mapper prepends base URL to paths)
- Flatten nested DTO structures (e.g., `List<GenreDto>` → `List<String>`)
- Keep in `core/data/model/` — one file per model (or group related models)

---

## Why these choices?

### Why separate domain models from DTOs?

| Approach | Pros | Cons | Verdict |
|----------|------|------|---------|
| **Separate DTO + Domain** | API changes don't affect UI, clean types, testable mappers | More files, more boilerplate | **Use this** |
| Use DTOs everywhere | Less code, fewer files | `@Serializable` on everything, nullable fields leak into UI, API change = UI change | Only OK for tiny apps |
| Single model with `@Serializable` | Compromise — one model for both | Conflicting concerns (API nullability vs UI requirements) | Avoid |

### Why no `@Serializable` on domain models?
- Domain models are never sent over the wire or saved to disk
- Adding `@Serializable` would require the serialization plugin to process them — unnecessary compile overhead
- It would also force all fields to be serializable (no custom types like `Color`, `ImageVector`, etc.)
- **Exception:** Navigation route args use `@Serializable`, but those are route objects, not domain models

### Why full URLs in domain models (not paths)?
```kotlin
// Domain model:
val posterUrl: String    // "https://image.tmdb.org/t/p/w500/abc.jpg"

// NOT:
val posterPath: String?  // "/abc.jpg"  (DTO-style)
```
- The Screen layer should never construct URLs — that's data layer logic
- If the image CDN changes, only the mapper needs updating
- Composables just pass `posterUrl` to `AsyncImageWithPlaceholder` — clean and simple
- The mapper handles null paths → null URLs, so missing images are caught early

### Why flatten nested DTOs?
```kotlin
// DTO: genres: List<GenreDto>  where GenreDto has id + name
// Domain: genres: List<String>  just the names

// DTO: authorDetails: AuthorDetailsDto  with username, name, avatarPath
// Domain: author: String, avatarUrl: String?  flattened fields
```
- Screens only need the display value, not the full nested structure
- Reduces coupling: if the API nests differently, only the mapper changes
- Simpler to use in Compose: `Text(movie.genres.joinToString())` vs `Text(movie.genres.map { it.name }.joinToString())`
