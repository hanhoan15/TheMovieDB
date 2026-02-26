package com.example.themoviedb

interface MovieRepository {
    suspend fun getMovies(category: MovieCategory, page: Int): List<MovieItem>
    suspend fun getMovieDetail(movieId: Int): MovieDetailItem?
    suspend fun searchMovies(query: String, page: Int): List<MovieItem>
}

class TmdbMovieRepository(
    private val accessToken: String,
    private val apiKey: String,
    private val imageBaseUrl: String,
) : MovieRepository {
    override suspend fun getMovies(category: MovieCategory, page: Int): List<MovieItem> {
        return fetchMovies(
            category = category,
            accessToken = accessToken,
            page = page,
        ).mapNotNull { summary ->
            val posterUrl = buildImageUrl(imageBaseUrl, summary.posterPath) ?: return@mapNotNull null
            MovieItem(
                id = summary.id,
                title = summary.title,
                rating = summary.rating,
                posterUrl = posterUrl,
                backdropUrl = buildImageUrl(imageBaseUrl, summary.backdropPath),
                releaseDate = summary.releaseDate,
                genreIds = summary.genreIds,
            )
        }
    }

    override suspend fun getMovieDetail(movieId: Int): MovieDetailItem? {
        val detail = fetchMovieDetail(
            movieId = movieId,
            accessToken = accessToken,
            apiKey = apiKey,
            language = "en-US",
        )?.toUiMovieDetail(imageBaseUrl) ?: return null

        val reviews = runCatching {
            fetchMovieReviews(
                movieId = movieId,
                accessToken = accessToken,
                apiKey = apiKey,
                language = "en-US",
            )
        }.getOrDefault(emptyList()).map { review ->
            MovieReviewItem(
                author = review.author,
                content = review.content,
                rating = review.rating,
                avatarUrl = buildAvatarUrl(imageBaseUrl, review.avatarPath),
            )
        }

        val cast = runCatching {
            fetchMovieCast(
                movieId = movieId,
                accessToken = accessToken,
                apiKey = apiKey,
                language = "en-US",
            )
        }.getOrDefault(emptyList()).map { cast ->
            MovieCastItem(
                name = cast.name,
                profileUrl = buildImageUrl(imageBaseUrl, cast.profilePath),
            )
        }

        val trailers = runCatching {
            fetchMovieVideos(
                movieId = movieId,
                accessToken = accessToken,
                apiKey = apiKey,
                language = "en-US",
            )
        }.getOrDefault(emptyList())
            .mapNotNull { video ->
                val watchUrl = buildTrailerWatchUrl(video.site, video.key) ?: return@mapNotNull null
                MovieTrailerItem(
                    name = video.name,
                    type = video.type,
                    watchUrl = watchUrl,
                    thumbnailUrl = buildTrailerThumbnailUrl(video.site, video.key),
                )
            }

        val images = runCatching {
            fetchMovieImages(
                movieId = movieId,
                accessToken = accessToken,
                apiKey = apiKey,
                language = "en-US",
            )
        }.getOrDefault(emptyList())
            .mapNotNull { image -> buildImageUrl(imageBaseUrl, image.filePath) }

        val similarMovies = runCatching {
            fetchMovieSimilar(
                movieId = movieId,
                accessToken = accessToken,
                apiKey = apiKey,
                page = 1,
                language = "en-US",
            )
        }.getOrDefault(emptyList()).mapNotNull { summary ->
            val posterUrl = buildImageUrl(imageBaseUrl, summary.posterPath) ?: return@mapNotNull null
            MovieItem(
                id = summary.id,
                title = summary.title,
                rating = summary.rating,
                posterUrl = posterUrl,
                backdropUrl = buildImageUrl(imageBaseUrl, summary.backdropPath),
                releaseDate = summary.releaseDate,
                genreIds = summary.genreIds,
            )
        }

        return detail.copy(
            images = images,
            reviews = reviews,
            cast = cast,
            trailers = trailers,
            similarMovies = similarMovies,
        )
    }

    override suspend fun searchMovies(query: String, page: Int): List<MovieItem> {
        return fetchSearchMovies(
            accessToken = accessToken,
            query = query,
            page = page,
            language = "en-US",
        ).mapNotNull { summary ->
            val posterUrl = buildImageUrl(imageBaseUrl, summary.posterPath) ?: return@mapNotNull null
            MovieItem(
                id = summary.id,
                title = summary.title,
                rating = summary.rating,
                posterUrl = posterUrl,
                backdropUrl = buildImageUrl(imageBaseUrl, summary.backdropPath),
                releaseDate = summary.releaseDate,
                genreIds = summary.genreIds,
            )
        }
    }
}

private fun buildAvatarUrl(imageBaseUrl: String, path: String?): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("/http")) return path.removePrefix("/")
    if (path.startsWith("http")) return path
    return "$imageBaseUrl$path"
}
