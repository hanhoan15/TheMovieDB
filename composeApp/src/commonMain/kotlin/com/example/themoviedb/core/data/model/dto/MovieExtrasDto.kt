package com.example.themoviedb.core.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieReviewsResponseDto(
    val id: Int,
    val page: Int,
    val results: List<MovieReviewResultDto>,
)

@Serializable
data class MovieReviewResultDto(
    val author: String,
    val content: String,
    @SerialName("author_details") val authorDetails: MovieReviewAuthorDetailsDto,
)

@Serializable
data class MovieReviewAuthorDetailsDto(
    val name: String? = null,
    val username: String? = null,
    @SerialName("avatar_path") val avatarPath: String? = null,
    val rating: Double? = null,
)

@Serializable
data class MovieCreditsResponseDto(
    val id: Int,
    val cast: List<MovieCastResultDto>,
)

@Serializable
data class MovieCastResultDto(
    val id: Int,
    val name: String,
    @SerialName("profile_path") val profilePath: String? = null,
)

@Serializable
data class MovieVideosResponseDto(
    val id: Int,
    val results: List<MovieVideoResultDto>,
)

@Serializable
data class MovieVideoResultDto(
    val name: String,
    val key: String,
    val site: String,
    val type: String,
)

@Serializable
data class MovieImagesResponseDto(
    val id: Int,
    val backdrops: List<MovieImageResultDto> = emptyList(),
    val posters: List<MovieImageResultDto> = emptyList(),
)

@Serializable
data class MovieImageResultDto(
    @SerialName("file_path") val filePath: String? = null,
)
