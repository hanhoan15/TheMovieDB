package com.example.themoviedb.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MovieDetailResponse(
    val adult: Boolean = false,
    @Json(name = "backdrop_path")
    val backdropPath: String? = null,
    val budget: Long = 0,
    val genres: List<MovieGenreResponse> = emptyList(),
    val id: Int,
    @Json(name = "original_language")
    val originalLanguage: String = "",
    val overview: String = "",
    @Json(name = "poster_path")
    val posterPath: String? = null,
    @Json(name = "release_date")
    val releaseDate: String = "",
    val revenue: Long = 0,
    val runtime: Int = 0,
    val title: String = "",
    @Json(name = "vote_average")
    val voteAverage: Double = 0.0,
    @Json(name = "vote_count")
    val voteCount: Int = 0,
)

@JsonClass(generateAdapter = true)
data class MovieGenreResponse(
    val id: Int,
    val name: String,
)
