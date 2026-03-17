package com.example.themoviedb.core.data.model

data class MovieItem(
    val id: Int,
    val title: String,
    val rating: Double,
    val posterUrl: String,
    val backdropUrl: String?,
    val releaseDate: String = "",
    val genreIds: List<Int> = emptyList(),
)
