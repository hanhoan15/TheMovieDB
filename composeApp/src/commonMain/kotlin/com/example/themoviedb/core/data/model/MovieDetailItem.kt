package com.example.themoviedb.core.data.model

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
