package com.example.themoviedb.core.data.mapper

import com.example.themoviedb.core.data.model.MovieCastItem
import com.example.themoviedb.core.data.model.MovieDetailItem
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.data.model.MovieReviewItem
import com.example.themoviedb.core.data.model.MovieTrailerItem
import com.example.themoviedb.core.data.model.dto.MovieCastResultDto
import com.example.themoviedb.core.data.model.dto.MovieDetailDto
import com.example.themoviedb.core.data.model.dto.MovieImageResultDto
import com.example.themoviedb.core.data.model.dto.MovieResultDto
import com.example.themoviedb.core.data.model.dto.MovieReviewResultDto
import com.example.themoviedb.core.data.model.dto.MovieVideoResultDto
import kotlin.math.round
import kotlin.math.roundToInt

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

fun MovieCastResultDto.toMovieCastItem(imageBaseUrl: String): MovieCastItem {
    return MovieCastItem(
        name = name,
        profileUrl = buildImageUrl(imageBaseUrl, profilePath),
    )
}

fun MovieVideoResultDto.toMovieTrailerItem(): MovieTrailerItem? {
    val watchUrl = buildTrailerWatchUrl(site, key) ?: return null
    return MovieTrailerItem(
        name = name,
        type = type,
        watchUrl = watchUrl,
        thumbnailUrl = buildTrailerThumbnailUrl(site, key),
    )
}

fun MovieImageResultDto.toImageUrl(imageBaseUrl: String): String? {
    return buildImageUrl(imageBaseUrl, filePath?.takeIf { it.isNotBlank() })
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

fun buildAvatarUrl(imageBaseUrl: String, path: String?): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("/http")) return path.removePrefix("/")
    if (path.startsWith("http")) return path
    return "$imageBaseUrl$path"
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
