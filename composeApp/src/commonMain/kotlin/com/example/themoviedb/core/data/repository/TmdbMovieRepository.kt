package com.example.themoviedb.core.data.repository

import com.example.themoviedb.core.data.mapper.buildAvatarUrl
import com.example.themoviedb.core.data.mapper.buildImageUrl
import com.example.themoviedb.core.data.mapper.toImageUrl
import com.example.themoviedb.core.data.mapper.toMovieCastItem
import com.example.themoviedb.core.data.mapper.toMovieDetailItem
import com.example.themoviedb.core.data.mapper.toMovieItem
import com.example.themoviedb.core.data.mapper.toMovieReviewItem
import com.example.themoviedb.core.data.mapper.toMovieTrailerItem
import com.example.themoviedb.core.data.model.MovieCategory
import com.example.themoviedb.core.data.model.MovieDetailItem
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.data.remote.ApiConstants
import com.example.themoviedb.core.data.remote.TmdbApiService

class TmdbMovieRepository(
    private val apiService: TmdbApiService,
    private val imageBaseUrl: String = ApiConstants.IMAGE_BASE_URL,
) : MovieRepository {

    override suspend fun getMovies(category: MovieCategory, page: Int): List<MovieItem> {
        return apiService.getMovies(category, page).results.mapNotNull { it.toMovieItem(imageBaseUrl) }
    }

    override suspend fun getMovieDetail(movieId: Int): MovieDetailItem? {
        val detail = runCatching { apiService.getMovieDetail(movieId) }.getOrNull()
            ?: return null

        val detailItem = detail.toMovieDetailItem(imageBaseUrl)

        val reviews = runCatching {
            apiService.getMovieReviews(movieId).results.map { it.toMovieReviewItem(imageBaseUrl) }
        }.getOrDefault(emptyList())

        val cast = runCatching {
            apiService.getMovieCredits(movieId).cast.map { it.toMovieCastItem(imageBaseUrl) }
        }.getOrDefault(emptyList())

        val trailers = runCatching {
            apiService.getMovieVideos(movieId).results.mapNotNull { it.toMovieTrailerItem() }
        }.getOrDefault(emptyList())

        val images = runCatching {
            val response = apiService.getMovieImages(movieId)
            (response.backdrops + response.posters)
                .mapNotNull { it.toImageUrl(imageBaseUrl) }
                .distinct()
        }.getOrDefault(emptyList())

        val similarMovies = runCatching {
            apiService.getSimilarMovies(movieId).results.mapNotNull { it.toMovieItem(imageBaseUrl) }
        }.getOrDefault(emptyList())

        return detailItem.copy(
            images = images,
            reviews = reviews,
            cast = cast,
            trailers = trailers,
            similarMovies = similarMovies,
        )
    }

    override suspend fun searchMovies(query: String, page: Int): List<MovieItem> {
        return apiService.searchMovies(query, page).results.mapNotNull { it.toMovieItem(imageBaseUrl) }
    }
}
