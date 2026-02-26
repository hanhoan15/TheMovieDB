package com.example.themoviedb.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MovieReviewsResponse(
    val id: Int,
    val page: Int,
    val results: List<MovieReviewResult>,
)

@JsonClass(generateAdapter = true)
data class MovieReviewResult(
    val author: String,
    val content: String,
    @Json(name = "author_details")
    val authorDetails: MovieReviewAuthorDetails,
)

@JsonClass(generateAdapter = true)
data class MovieReviewAuthorDetails(
    val name: String? = null,
    val username: String? = null,
    @Json(name = "avatar_path")
    val avatarPath: String? = null,
    val rating: Double? = null,
)

@JsonClass(generateAdapter = true)
data class MovieCreditsResponse(
    val id: Int,
    val cast: List<MovieCastResult>,
)

@JsonClass(generateAdapter = true)
data class MovieCastResult(
    val id: Int,
    val name: String,
    @Json(name = "profile_path")
    val profilePath: String? = null,
)

@JsonClass(generateAdapter = true)
data class MovieVideosResponse(
    val id: Int,
    val results: List<MovieVideoResult>,
)

@JsonClass(generateAdapter = true)
data class MovieVideoResult(
    val name: String,
    val key: String,
    val site: String,
    val type: String,
)

@JsonClass(generateAdapter = true)
data class MovieImagesResponse(
    val id: Int,
    val backdrops: List<MovieImageResult>,
    val posters: List<MovieImageResult>,
)

@JsonClass(generateAdapter = true)
data class MovieImageResult(
    @Json(name = "file_path")
    val filePath: String? = null,
)
