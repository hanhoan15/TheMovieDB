package com.example.themoviedb.core.data.remote

import com.example.themoviedb.core.data.model.MovieCategory
import com.example.themoviedb.core.data.model.dto.MovieCreditsResponseDto
import com.example.themoviedb.core.data.model.dto.MovieDetailDto
import com.example.themoviedb.core.data.model.dto.MovieImagesResponseDto
import com.example.themoviedb.core.data.model.dto.MovieReviewsResponseDto
import com.example.themoviedb.core.data.model.dto.MovieVideosResponseDto
import com.example.themoviedb.core.data.model.dto.MoviesResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class TmdbApiService(private val httpClient: HttpClient) {

    suspend fun getMovies(category: MovieCategory, page: Int = 1): MoviesResponseDto {
        val path = when (category) {
            MovieCategory.NOW_PLAYING -> "movie/now_playing"
            MovieCategory.TOP_RATED -> "movie/top_rated"
            MovieCategory.POPULAR -> "movie/popular"
            MovieCategory.UPCOMING -> "movie/upcoming"
        }
        return httpClient.get("${ApiConstants.BASE_URL}$path") {
            parameter("language", "en-US")
            parameter("page", page)
        }.body()
    }

    suspend fun getMovieDetail(movieId: Int): MovieDetailDto {
        return httpClient.get("${ApiConstants.BASE_URL}movie/$movieId") {
            parameter("api_key", ApiConstants.API_KEY)
            parameter("language", "en-US")
        }.body()
    }

    suspend fun getMovieReviews(movieId: Int): MovieReviewsResponseDto {
        return httpClient.get("${ApiConstants.BASE_URL}movie/$movieId/reviews") {
            parameter("api_key", ApiConstants.API_KEY)
            parameter("language", "en-US")
            parameter("page", 1)
        }.body()
    }

    suspend fun getMovieCredits(movieId: Int): MovieCreditsResponseDto {
        return httpClient.get("${ApiConstants.BASE_URL}movie/$movieId/credits") {
            parameter("api_key", ApiConstants.API_KEY)
            parameter("language", "en-US")
        }.body()
    }

    suspend fun getMovieVideos(movieId: Int): MovieVideosResponseDto {
        return httpClient.get("${ApiConstants.BASE_URL}movie/$movieId/videos") {
            parameter("api_key", ApiConstants.API_KEY)
            parameter("language", "en-US")
        }.body()
    }

    suspend fun getMovieImages(movieId: Int): MovieImagesResponseDto {
        return httpClient.get("${ApiConstants.BASE_URL}movie/$movieId/images") {
            parameter("api_key", ApiConstants.API_KEY)
            parameter("include_image_language", "en,null")
        }.body()
    }

    suspend fun getSimilarMovies(movieId: Int, page: Int = 1): MoviesResponseDto {
        return httpClient.get("${ApiConstants.BASE_URL}movie/$movieId/similar") {
            parameter("api_key", ApiConstants.API_KEY)
            parameter("language", "en-US")
            parameter("page", page)
        }.body()
    }

    suspend fun searchMovies(query: String, page: Int = 1): MoviesResponseDto {
        return httpClient.get("${ApiConstants.BASE_URL}search/movie") {
            parameter("query", query)
            parameter("language", "en-US")
            parameter("page", page)
            parameter("include_adult", false)
        }.body()
    }
}
