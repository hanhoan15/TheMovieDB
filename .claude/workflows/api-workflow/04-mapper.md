# Step 3: Write the Mapper

**File:** `composeApp/src/commonMain/kotlin/com/example/themoviedb/core/data/mapper/MovieMapper.kt`

Add extension functions to the existing mapper file. Mappers convert DTOs to domain models.

## Template

```kotlin
fun <Name>ResultDto.to<Name>Item(imageBaseUrl: String): <Name>Item? {
    val posterUrl = buildImageUrl(imageBaseUrl, posterPath) ?: return null
    return <Name>Item(
        id = id,
        title = title.orEmpty(),
        imageUrl = posterUrl,
        rating = voteAverage ?: 0.0,
    )
}
```

## Real mappers from MovieMapper.kt

### MovieResultDto → MovieItem
```kotlin
fun MovieResultDto.toMovieItem(imageBaseUrl: String): MovieItem? {
    val poster = buildImageUrl(imageBaseUrl, posterPath) ?: return null
    return MovieItem(
        id = id,
        title = title,
        rating = voteAverage,
        posterUrl = poster,
        backdropUrl = buildImageUrl(imageBaseUrl, backdropPath),
        releaseDate = releaseDate,
        genreIds = genreIds,
    )
}
```

### MovieDetailDto → MovieDetailItem
```kotlin
fun MovieDetailDto.toMovieDetailItem(imageBaseUrl: String): MovieDetailItem {
    return MovieDetailItem(
        id = id,
        title = title,
        overview = overview,
        posterUrl = buildImageUrl(imageBaseUrl, posterPath),
        backdropUrl = buildImageUrl(imageBaseUrl, backdropPath),
        originalLanguage = originalLanguage,
        releaseDate = releaseDate,
        runtime = runtime,
        genres = genres.map { it.name },
        voteAverage = voteAverage,
        voteCount = voteCount,
        budget = budget,
        revenue = revenue,
    )
}
```

### MovieReviewResultDto → MovieReviewItem
```kotlin
fun MovieReviewResultDto.toMovieReviewItem(imageBaseUrl: String): MovieReviewItem {
    val author = authorDetails.name?.takeIf { it.isNotBlank() }
        ?: authorDetails.username?.takeIf { it.isNotBlank() }
        ?: author
    return MovieReviewItem(
        author = author,
        content = content,
        rating = authorDetails.rating,
        avatarUrl = buildAvatarUrl(imageBaseUrl, authorDetails.avatarPath),
    )
}
```

## Utility functions already available

```kotlin
// Build full image URL from base + path:
fun buildImageUrl(baseUrl: String, path: String?): String?
// "https://image.tmdb.org/t/p/w500" + "/abc.jpg" → "https://image.tmdb.org/t/p/w500/abc.jpg"
// null path → null

// Build avatar URL (handles TMDB mixed formats):
fun buildAvatarUrl(imageBaseUrl: String, path: String?): String?
// "/https://example.com/avatar.jpg" → "https://example.com/avatar.jpg"
// "/abc.jpg" → "https://image.tmdb.org/t/p/w500/abc.jpg"

// Build YouTube/Vimeo URLs:
fun buildTrailerWatchUrl(site: String, key: String): String?
fun buildTrailerThumbnailUrl(site: String, key: String): String?

// Formatting:
fun Double.toOneDecimalString(): String    // 7.456 → "7.5"
fun Double.toFiveStarString(): String      // 7.0 → "★★★★☆"

// Extraction:
fun MovieItem.releaseYear(): String        // "2024-01-15" → "2024"
fun MovieItem.primaryGenreLabel(): String  // genreId 28 → "Action"
```

## Mapper conventions

### Return nullable to filter bad data
```kotlin
fun Dto.toDomain(): DomainModel? {
    val imageUrl = buildImageUrl(base, path) ?: return null   // skip items without images
    return DomainModel(...)
}
```
Then use `mapNotNull` in repository:
```kotlin
response.results.mapNotNull { it.toDomain(imageBaseUrl) }
```

### Provide safe defaults for nullable DTO fields
```kotlin
title.orEmpty()                    // String? → String
voteAverage ?: 0.0                 // Double? → Double
runtime ?: 0                       // Int? → Int
genres.map { it.name.orEmpty() }   // List<GenreDto> → List<String>
```

### Pass imageBaseUrl through the chain
Every mapper that builds image URLs receives `imageBaseUrl: String` as a parameter.
The base URL comes from `ApiConstants.IMAGE_BASE_URL` and is passed from the Repository.

## Rules
- Extension functions on DTO types: `fun DtoType.toDomainType()`
- Add to the existing `MovieMapper.kt` — don't create separate mapper files unless it's a completely different domain
- Always provide defaults for nullable DTO fields
- Return `null` to skip items with missing critical data (e.g., no poster)
- Keep mappers pure (no side effects, no API calls)

---

## Why these choices?

### Why extension functions over mapper classes?

| Approach | Example | Pros | Cons | Verdict |
|----------|---------|------|------|---------|
| **Extension function** | `dto.toMovieItem(baseUrl)` | Concise, discoverable via IDE autocomplete, no DI needed, pure function | Can't inject dependencies | **Use this** |
| Mapper class | `MovieMapper().map(dto)` | Can inject dependencies, easier to mock | Boilerplate, needs DI registration, class per mapping | Overkill for pure transforms |
| Constructor mapping | `MovieItem(dto.title, ...)` | Simple | Mixes concerns, domain model knows about DTO | Avoid |
| Auto-mapping library (MapStruct) | `@Mapper interface MovieMapper` | Zero boilerplate | JVM-only, no KMP support, magic | Can't use |

Extension functions are the Kotlin-idiomatic approach for pure data transformations. Since our mappers only do field mapping + URL construction (no database calls, no network), they don't need dependency injection.

### Why return nullable (`T?`) from mappers?
```kotlin
fun MovieResultDto.toMovieItem(imageBaseUrl: String): MovieItem? {
    val poster = buildImageUrl(imageBaseUrl, posterPath) ?: return null  // skip bad data
    return MovieItem(...)
}
```
This enables `mapNotNull` in the repository:
```kotlin
response.results.mapNotNull { it.toMovieItem(imageBaseUrl) }
```
Items without posters are silently filtered out — no placeholder images, no broken cards in the UI.

**Alternative:** Return non-null with a placeholder URL. We avoid this because:
- A movie card with a broken/missing image looks worse than no card at all
- The UI has shimmer loading, so fewer items still look polished

### Why one `MovieMapper.kt` file (not one file per mapper)?
- All mappers share utility functions (`buildImageUrl`, `buildAvatarUrl`, `toOneDecimalString`)
- Related mappers are often created/modified together (e.g., adding a new endpoint)
- One file is easier to search and maintain than 10 scattered mapper files
- **When to split:** If you add a completely different domain (e.g., `TvShowMapper.kt` for TV shows)

### Why pass `imageBaseUrl` as parameter (not hardcode)?
```kotlin
fun MovieResultDto.toMovieItem(imageBaseUrl: String): MovieItem?  // parameter
// NOT:
fun MovieResultDto.toMovieItem(): MovieItem?  // hardcoded ApiConstants.IMAGE_BASE_URL
```
- Makes mappers testable with any base URL
- Tests don't depend on `ApiConstants`
- If the CDN URL changes per environment (dev/staging/prod), the mapper still works
