package com.example.themoviedb

import kotlin.math.round
import kotlin.math.roundToInt

data class MovieItem(
    val id: Int,
    val title: String,
    val rating: Double,
    val posterUrl: String,
    val backdropUrl: String?,
    val releaseDate: String = "",
    val genreIds: List<Int> = emptyList(),
)

data class MovieDetailItem(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val originalLanguage: String,
    val releaseDate: String,
    val runtime: Int = 0,
    val genres: List<String> = emptyList(),
    val voteAverage: Double,
    val voteCount: Int,
    val budget: Long,
    val revenue: Long,
    val images: List<String> = emptyList(),
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

enum class AppTab(val icon: String, val label: String, val category: MovieCategory) {
    NOW_PLAYING(icon = "⌂", label = "Now playing", category = MovieCategory.NOW_PLAYING),
    TOP_RATED(icon = "★", label = "Top rated", category = MovieCategory.TOP_RATED),
    POPULAR(icon = "↗", label = "Popular", category = MovieCategory.POPULAR),
    UPCOMING(icon = "⌄", label = "Upcoming", category = MovieCategory.UPCOMING),
}

fun MovieDetailSummary.toUiMovieDetail(imageBaseUrl: String): MovieDetailItem {
    return MovieDetailItem(
        id = id,
        title = title,
        overview = overview,
        posterUrl = buildImageUrl(imageBaseUrl, posterPath),
        backdropUrl = buildImageUrl(imageBaseUrl, backdropPath),
        originalLanguage = originalLanguage,
        releaseDate = releaseDate,
        runtime = runtime,
        genres = genres,
        voteAverage = voteAverage,
        voteCount = voteCount,
        budget = budget,
        revenue = revenue,
    )
}

fun MovieItem.toFallbackDetail(): MovieDetailItem {
    return MovieDetailItem(
        id = id,
        title = title,
        overview = "No synopsis available.",
        posterUrl = posterUrl,
        backdropUrl = backdropUrl ?: posterUrl,
        originalLanguage = "en",
        releaseDate = "-",
        runtime = 0,
        genres = genreIds.takeIf { it.isNotEmpty() }?.let { listOf(primaryGenreLabel()) } ?: emptyList(),
        voteAverage = rating,
        voteCount = 0,
        budget = 0,
        revenue = 0,
    )
}

fun MovieDetailItem.toWatchListMovie(): MovieItem {
    return MovieItem(
        id = id,
        title = title,
        rating = voteAverage,
        posterUrl = posterUrl ?: backdropUrl.orEmpty(),
        backdropUrl = backdropUrl ?: posterUrl,
        releaseDate = releaseDate,
    )
}

fun buildTrailerWatchUrl(site: String, key: String): String? {
    if (key.isBlank()) return null
    return when (site.lowercase()) {
        "youtube" -> "https://www.youtube.com/watch?v=$key"
        "vimeo" -> "https://vimeo.com/$key"
        else -> null
    }
}

fun buildTrailerThumbnailUrl(site: String, key: String): String? {
    if (key.isBlank()) return null
    return when (site.lowercase()) {
        "youtube" -> "https://img.youtube.com/vi/$key/hqdefault.jpg"
        else -> null
    }
}

fun Double.toOneDecimalString(): String {
    val rounded = round(this * 10.0) / 10.0
    return rounded.toString()
}

fun Double.toFiveStarString(): String {
    val filled = (this / 2.0).roundToInt().coerceIn(0, 5)
    val empty = 5 - filled
    return "★".repeat(filled) + "☆".repeat(empty)
}

fun buildImageUrl(baseUrl: String, path: String?): String? {
    if (path.isNullOrBlank()) return null
    return "$baseUrl$path"
}

fun MovieItem.releaseYear(): String {
    return releaseDate.takeIf { it.length >= 4 }?.substring(0, 4).orEmpty().ifBlank { "-" }
}

fun MovieItem.primaryGenreLabel(): String {
    val genreId = genreIds.firstOrNull() ?: return "Unknown"
    return when (genreId) {
        28 -> "Action"
        12 -> "Adventure"
        16 -> "Animation"
        35 -> "Comedy"
        80 -> "Crime"
        18 -> "Drama"
        14 -> "Fantasy"
        27 -> "Horror"
        9648 -> "Mystery"
        10749 -> "Romance"
        878 -> "Sci-Fi"
        53 -> "Thriller"
        else -> "Movie"
    }
}
