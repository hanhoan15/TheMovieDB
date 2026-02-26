package com.example.themoviedb.data

object AppConfig {
    const val BASE_URL = "https://api.themoviedb.org/3/"

    const val PATH_MOVIE_NOW_PLAYING = "movie/now_playing"
    const val PATH_MOVIE_TOP_RATED = "movie/top_rated"
    const val PATH_MOVIE_POPULAR = "movie/popular"
    const val PATH_MOVIE_UPCOMING = "movie/upcoming"
    const val PATH_MOVIE_DETAIL = "movie/{movieId}"
    const val PATH_MOVIE_REVIEWS = "movie/{movieId}/reviews"
    const val PATH_MOVIE_CREDITS = "movie/{movieId}/credits"
    const val PATH_MOVIE_VIDEOS = "movie/{movieId}/videos"
    const val PATH_MOVIE_IMAGES = "movie/{movieId}/images"
    const val PATH_MOVIE_SIMILAR = "movie/{movieId}/similar"
    const val PATH_SEARCH_MOVIE = "search/movie"
}
