package com.example.themoviedb.core.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailDto(
    val adult: Boolean = false,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    val budget: Long = 0,
    val genres: List<GenreDto> = emptyList(),
    val id: Int,
    @SerialName("original_language") val originalLanguage: String = "",
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("release_date") val releaseDate: String = "",
    val revenue: Long = 0,
    val runtime: Int = 0,
    val title: String = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
)

@Serializable
data class GenreDto(
    val id: Int,
    val name: String,
)
