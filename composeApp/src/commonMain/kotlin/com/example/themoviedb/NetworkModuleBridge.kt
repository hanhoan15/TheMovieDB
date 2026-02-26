package com.example.themoviedb

enum class MovieCategory {
    NOW_PLAYING,
    TOP_RATED,
    POPULAR,
    UPCOMING,
}

data class MovieSummary(
    val id: Int,
    val title: String,
    val rating: Double,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val genreIds: List<Int>,
)

expect suspend fun fetchMovies(
    category: MovieCategory,
    accessToken: String,
    page: Int = 1,
): List<MovieSummary>

expect suspend fun fetchSearchMovies(
    accessToken: String,
    query: String,
    page: Int = 1,
    language: String = "en-US",
): List<MovieSummary>

data class MovieDetailSummary(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val originalLanguage: String,
    val releaseDate: String,
    val runtime: Int,
    val genres: List<String>,
    val voteAverage: Double,
    val voteCount: Int,
    val budget: Long,
    val revenue: Long,
)

expect suspend fun fetchMovieDetail(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String = "en-US",
): MovieDetailSummary?

data class MovieReviewSummary(
    val author: String,
    val content: String,
    val rating: Double?,
    val avatarPath: String?,
)

expect suspend fun fetchMovieReviews(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String = "en-US",
): List<MovieReviewSummary>

data class MovieCastSummary(
    val name: String,
    val profilePath: String?,
)

expect suspend fun fetchMovieCast(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String = "en-US",
): List<MovieCastSummary>

data class MovieVideoSummary(
    val name: String,
    val key: String,
    val site: String,
    val type: String,
)

expect suspend fun fetchMovieVideos(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String = "en-US",
): List<MovieVideoSummary>

data class MovieImageSummary(
    val filePath: String,
)

expect suspend fun fetchMovieImages(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String = "en-US",
): List<MovieImageSummary>

expect suspend fun fetchMovieSimilar(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    page: Int = 1,
    language: String = "en-US",
): List<MovieSummary>
