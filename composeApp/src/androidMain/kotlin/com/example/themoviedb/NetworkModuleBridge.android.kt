package com.example.themoviedb

import com.example.themoviedb.data.remote.NetworkModule

actual suspend fun fetchMovies(
    category: MovieCategory,
    accessToken: String,
    page: Int,
): List<MovieSummary> {
    val service = NetworkModule.createTmdbService(accessToken)
    val response = when (category) {
        MovieCategory.NOW_PLAYING -> service.getNowPlaying(page = page)
        MovieCategory.TOP_RATED -> service.getTopRated(page = page)
        MovieCategory.POPULAR -> service.getPopular(page = page)
        MovieCategory.UPCOMING -> service.getUpcoming(page = page)
    }

    return response.results.map { movie ->
        MovieSummary(
            id = movie.id,
            title = movie.title,
            rating = movie.voteAverage,
            posterPath = movie.posterPath,
            backdropPath = movie.backdropPath,
            releaseDate = movie.releaseDate,
            genreIds = movie.genreIds,
        )
    }
}

actual suspend fun fetchSearchMovies(
    accessToken: String,
    query: String,
    page: Int,
    language: String,
): List<MovieSummary> {
    if (query.isBlank()) return emptyList()
    val service = NetworkModule.createTmdbService(accessToken)
    val response = service.searchMovies(
        query = query,
        page = page,
        language = language,
    )
    return response.results.map { movie ->
        MovieSummary(
            id = movie.id,
            title = movie.title,
            rating = movie.voteAverage,
            posterPath = movie.posterPath,
            backdropPath = movie.backdropPath,
            releaseDate = movie.releaseDate,
            genreIds = movie.genreIds,
        )
    }
}

actual suspend fun fetchMovieDetail(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String,
): MovieDetailSummary? {
    val service = NetworkModule.createTmdbService(accessToken)
    return runCatching {
        val detail = service.movieDetail(
            movieId = movieId,
            apiKey = apiKey,
            language = language,
        )
        MovieDetailSummary(
            id = detail.id,
            title = detail.title,
            overview = detail.overview,
            posterPath = detail.posterPath,
            backdropPath = detail.backdropPath,
            originalLanguage = detail.originalLanguage,
            releaseDate = detail.releaseDate,
            runtime = detail.runtime,
            genres = detail.genres.map { genre -> genre.name },
            voteAverage = detail.voteAverage,
            voteCount = detail.voteCount,
            budget = detail.budget,
            revenue = detail.revenue,
        )
    }.getOrNull()
}

actual suspend fun fetchMovieReviews(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String,
): List<MovieReviewSummary> {
    val service = NetworkModule.createTmdbService(accessToken)
    return runCatching {
        service.movieReviews(
            movieId = movieId,
            apiKey = apiKey,
            language = language,
        ).results.map { review ->
            MovieReviewSummary(
                author = review.authorDetails.name
                    ?.takeIf { it.isNotBlank() }
                    ?: review.authorDetails.username
                    ?.takeIf { it.isNotBlank() }
                    ?: review.author,
                content = review.content,
                rating = review.authorDetails.rating,
                avatarPath = review.authorDetails.avatarPath,
            )
        }
    }.getOrDefault(emptyList())
}

actual suspend fun fetchMovieCast(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String,
): List<MovieCastSummary> {
    val service = NetworkModule.createTmdbService(accessToken)
    return runCatching {
        service.movieCredits(
            movieId = movieId,
            apiKey = apiKey,
            language = language,
        ).cast.map { cast ->
            MovieCastSummary(
                name = cast.name,
                profilePath = cast.profilePath,
            )
        }
    }.getOrDefault(emptyList())
}

actual suspend fun fetchMovieVideos(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String,
): List<MovieVideoSummary> {
    val service = NetworkModule.createTmdbService(accessToken)
    return runCatching {
        service.movieVideos(
            movieId = movieId,
            apiKey = apiKey,
            language = language,
        ).results.map { video ->
            MovieVideoSummary(
                name = video.name,
                key = video.key,
                site = video.site,
                type = video.type,
            )
        }
    }.getOrDefault(emptyList())
}

actual suspend fun fetchMovieImages(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String,
): List<MovieImageSummary> {
    val service = NetworkModule.createTmdbService(accessToken)
    return runCatching {
        val images = service.movieImages(
            movieId = movieId,
            apiKey = apiKey,
            includeImageLanguage = "${language.take(2)},null",
        )
        (images.backdrops + images.posters)
            .mapNotNull { image -> image.filePath?.takeIf { it.isNotBlank() } }
            .distinct()
            .map { path -> MovieImageSummary(filePath = path) }
    }.getOrDefault(emptyList())
}

actual suspend fun fetchMovieSimilar(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    page: Int,
    language: String,
): List<MovieSummary> {
    val service = NetworkModule.createTmdbService(accessToken)
    return runCatching {
        service.movieSimilar(
            movieId = movieId,
            apiKey = apiKey,
            language = language,
            page = page,
        ).results.map { movie ->
            MovieSummary(
                id = movie.id,
                title = movie.title,
                rating = movie.voteAverage,
                posterPath = movie.posterPath,
                backdropPath = movie.backdropPath,
                releaseDate = movie.releaseDate,
                genreIds = movie.genreIds,
            )
        }
    }.getOrDefault(emptyList())
}
