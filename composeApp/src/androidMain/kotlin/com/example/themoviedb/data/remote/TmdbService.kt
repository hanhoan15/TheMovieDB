package com.example.themoviedb.data.remote

import com.example.themoviedb.data.AppConfig
import com.example.themoviedb.data.model.MovieCreditsResponse
import com.example.themoviedb.data.model.MovieDetailResponse
import com.example.themoviedb.data.model.MovieImagesResponse
import com.example.themoviedb.data.model.MovieReviewsResponse
import com.example.themoviedb.data.model.MovieVideosResponse
import com.example.themoviedb.data.model.MoviesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbService {
    @GET(AppConfig.PATH_MOVIE_NOW_PLAYING)
    suspend fun getNowPlaying(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): MoviesResponse

    @GET(AppConfig.PATH_MOVIE_TOP_RATED)
    suspend fun getTopRated(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): MoviesResponse

    @GET(AppConfig.PATH_MOVIE_POPULAR)
    suspend fun getPopular(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): MoviesResponse

    @GET(AppConfig.PATH_MOVIE_UPCOMING)
    suspend fun getUpcoming(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): MoviesResponse

    @GET(AppConfig.PATH_MOVIE_DETAIL)
    suspend fun movieDetail(
        @Path("movieId") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
    ): MovieDetailResponse

    @GET(AppConfig.PATH_MOVIE_REVIEWS)
    suspend fun movieReviews(
        @Path("movieId") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): MovieReviewsResponse

    @GET(AppConfig.PATH_MOVIE_CREDITS)
    suspend fun movieCredits(
        @Path("movieId") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
    ): MovieCreditsResponse

    @GET(AppConfig.PATH_MOVIE_VIDEOS)
    suspend fun movieVideos(
        @Path("movieId") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
    ): MovieVideosResponse

    @GET(AppConfig.PATH_MOVIE_IMAGES)
    suspend fun movieImages(
        @Path("movieId") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("include_image_language") includeImageLanguage: String = "en,null",
    ): MovieImagesResponse

    @GET(AppConfig.PATH_MOVIE_SIMILAR)
    suspend fun movieSimilar(
        @Path("movieId") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): MoviesResponse

    @GET(AppConfig.PATH_SEARCH_MOVIE)
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
    ): MoviesResponse
}
