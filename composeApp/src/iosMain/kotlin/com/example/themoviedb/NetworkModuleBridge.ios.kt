package com.example.themoviedb

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.BetaInteropApi
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import platform.Foundation.*

private const val TMDB_BASE_URL = "https://api.themoviedb.org/3"
private const val TMDB_API_KEY = "394cba9acc6f443b1abfc75085b89adc"

actual suspend fun fetchMovies(
    category: MovieCategory,
    accessToken: String,
    page: Int,
): List<MovieSummary> {
    val path = when (category) {
        MovieCategory.NOW_PLAYING -> "movie/now_playing"
        MovieCategory.TOP_RATED -> "movie/top_rated"
        MovieCategory.POPULAR -> "movie/popular"
        MovieCategory.UPCOMING -> "movie/upcoming"
    }
    val payload = tmdbGet(
        path = path,
        accessToken = accessToken,
        query = mapOf(
            "api_key" to TMDB_API_KEY,
            "page" to page.toString(),
            "language" to "en-US",
        ),
    ) as? Map<*, *> ?: return emptyList()

    val results = payload["results"] as? List<*> ?: return emptyList()
    return results.mapNotNull { (it as? Map<*, *>)?.toMovieSummary() }
}

actual suspend fun fetchSearchMovies(
    accessToken: String,
    query: String,
    page: Int,
    language: String,
): List<MovieSummary> {
    if (query.isBlank()) return emptyList()
    val payload = tmdbGet(
        path = "search/movie",
        accessToken = accessToken,
        query = mapOf(
            "api_key" to TMDB_API_KEY,
            "query" to query,
            "page" to page.toString(),
            "language" to language,
        ),
    ) as? Map<*, *> ?: return emptyList()

    val results = payload["results"] as? List<*> ?: return emptyList()
    return results.mapNotNull { (it as? Map<*, *>)?.toMovieSummary() }
}

actual suspend fun fetchMovieDetail(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String,
): MovieDetailSummary? {
    val payload = tmdbGet(
        path = "movie/$movieId",
        accessToken = accessToken,
        query = mapOf(
            "api_key" to apiKey,
            "language" to language,
        ),
    ) as? Map<*, *> ?: return null

    val id = (payload["id"] as? Number)?.toInt() ?: return null
    val title = payload["title"] as? String ?: return null
    val overview = payload["overview"] as? String ?: ""
    val posterPath = payload["poster_path"] as? String
    val backdropPath = payload["backdrop_path"] as? String
    val originalLanguage = payload["original_language"] as? String ?: ""
    val releaseDate = payload["release_date"] as? String ?: ""
    val runtime = (payload["runtime"] as? Number)?.toInt() ?: 0
    val voteAverage = (payload["vote_average"] as? Number)?.toDouble() ?: 0.0
    val voteCount = (payload["vote_count"] as? Number)?.toInt() ?: 0
    val budget = (payload["budget"] as? Number)?.toLong() ?: 0L
    val revenue = (payload["revenue"] as? Number)?.toLong() ?: 0L
    val genres = (payload["genres"] as? List<*>)
        ?.mapNotNull { (it as? Map<*, *>)?.get("name") as? String }
        ?: emptyList()

    return MovieDetailSummary(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
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

actual suspend fun fetchMovieReviews(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String,
): List<MovieReviewSummary> {
    val payload = tmdbGet(
        path = "movie/$movieId/reviews",
        accessToken = accessToken,
        query = mapOf(
            "api_key" to apiKey,
            "language" to language,
            "page" to "1",
        ),
    ) as? Map<*, *> ?: return emptyList()

    val results = payload["results"] as? List<*> ?: return emptyList()
    return results.mapNotNull { reviewAny ->
        val review = reviewAny as? Map<*, *> ?: return@mapNotNull null
        val authorDetails = review["author_details"] as? Map<*, *>
        val fallbackAuthor = review["author"] as? String
        val author =
            (authorDetails?.get("name") as? String)?.takeIf { it.isNotBlank() }
                ?: (authorDetails?.get("username") as? String)?.takeIf { it.isNotBlank() }
                ?: fallbackAuthor
                ?: "Anonymous"
        val content = review["content"] as? String ?: ""
        val rating = (authorDetails?.get("rating") as? Number)?.toDouble()
        val avatarPath = authorDetails?.get("avatar_path") as? String
        MovieReviewSummary(
            author = author,
            content = content,
            rating = rating,
            avatarPath = avatarPath,
        )
    }
}

actual suspend fun fetchMovieCast(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String,
): List<MovieCastSummary> {
    val payload = tmdbGet(
        path = "movie/$movieId/credits",
        accessToken = accessToken,
        query = mapOf(
            "api_key" to apiKey,
            "language" to language,
        ),
    ) as? Map<*, *> ?: return emptyList()

    val cast = payload["cast"] as? List<*> ?: return emptyList()
    return cast.mapNotNull { castAny ->
        val member = castAny as? Map<*, *> ?: return@mapNotNull null
        val name = member["name"] as? String ?: return@mapNotNull null
        val profilePath = member["profile_path"] as? String
        MovieCastSummary(
            name = name,
            profilePath = profilePath,
        )
    }
}

actual suspend fun fetchMovieVideos(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String,
): List<MovieVideoSummary> {
    val payload = tmdbGet(
        path = "movie/$movieId/videos",
        accessToken = accessToken,
        query = mapOf(
            "api_key" to apiKey,
            "language" to language,
        ),
    ) as? Map<*, *> ?: return emptyList()

    val videos = payload["results"] as? List<*> ?: return emptyList()
    return videos.mapNotNull { videoAny ->
        val video = videoAny as? Map<*, *> ?: return@mapNotNull null
        val name = video["name"] as? String ?: return@mapNotNull null
        val key = video["key"] as? String ?: return@mapNotNull null
        val site = video["site"] as? String ?: return@mapNotNull null
        val type = video["type"] as? String ?: ""
        MovieVideoSummary(
            name = name,
            key = key,
            site = site,
            type = type,
        )
    }
}

actual suspend fun fetchMovieImages(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    language: String,
): List<MovieImageSummary> {
    val payload = tmdbGet(
        path = "movie/$movieId/images",
        accessToken = accessToken,
        query = mapOf(
            "api_key" to apiKey,
            "include_image_language" to "${language.take(2)},null",
        ),
    ) as? Map<*, *> ?: return emptyList()

    val backdrops = (payload["backdrops"] as? List<*>).orEmpty()
    val posters = (payload["posters"] as? List<*>).orEmpty()
    return (backdrops + posters).mapNotNull { imageAny ->
        val image = imageAny as? Map<*, *> ?: return@mapNotNull null
        val filePath = image["file_path"] as? String ?: return@mapNotNull null
        filePath.takeIf { it.isNotBlank() }?.let { MovieImageSummary(filePath = it) }
    }.distinctBy { it.filePath }
}

actual suspend fun fetchMovieSimilar(
    movieId: Int,
    accessToken: String,
    apiKey: String,
    page: Int,
    language: String,
): List<MovieSummary> {
    val payload = tmdbGet(
        path = "movie/$movieId/similar",
        accessToken = accessToken,
        query = mapOf(
            "api_key" to apiKey,
            "page" to page.toString(),
            "language" to language,
        ),
    ) as? Map<*, *> ?: return emptyList()

    val results = payload["results"] as? List<*> ?: return emptyList()
    return results.mapNotNull { (it as? Map<*, *>)?.toMovieSummary() }
}

private fun Map<*, *>.toMovieSummary(): MovieSummary? {
    val id = (this["id"] as? Number)?.toInt() ?: return null
    val title = this["title"] as? String ?: return null
    val rating = (this["vote_average"] as? Number)?.toDouble() ?: 0.0
    val posterPath = this["poster_path"] as? String
    val backdropPath = this["backdrop_path"] as? String
    val releaseDate = this["release_date"] as? String ?: ""
    val genreIds = (this["genre_ids"] as? List<*>)
        ?.mapNotNull { (it as? Number)?.toInt() }
        ?: emptyList()
    return MovieSummary(
        id = id,
        title = title,
        rating = rating,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        genreIds = genreIds,
    )
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private suspend fun tmdbGet(
    path: String,
    accessToken: String,
    query: Map<String, String> = emptyMap(),
): Any? = suspendCoroutine { continuation ->
    val components = NSURLComponents(string = "$TMDB_BASE_URL/$path")
    components.queryItems = query.map { (key, value) ->
        NSURLQueryItem(name = key, value = value)
    }

    val url = components.URL
    if (url == null) {
        continuation.resume(null)
        return@suspendCoroutine
    }

    val data = NSData.Companion.create(contentsOfURL = url) ?: run {
        continuation.resume(null)
        return@suspendCoroutine
    }

    val json = runCatching {
        NSJSONSerialization.JSONObjectWithData(
            data = data,
            options = 0u,
            error = null,
        )
    }.getOrNull()
    continuation.resume(json)
}
