package com.example.themoviedb.fake

import com.example.themoviedb.core.data.model.MovieDetailItem
import com.example.themoviedb.core.data.model.MovieItem

fun movie(id: Int, title: String): MovieItem {
    return MovieItem(
        id = id,
        title = title,
        rating = 7.0,
        posterUrl = "https://example.com/poster.jpg",
        backdropUrl = "https://example.com/backdrop.jpg",
    )
}

fun detail(id: Int, title: String): MovieDetailItem {
    return MovieDetailItem(
        id = id,
        title = title,
        overview = "Overview",
        posterUrl = "https://example.com/poster.jpg",
        backdropUrl = "https://example.com/backdrop.jpg",
        originalLanguage = "en",
        releaseDate = "2024-01-01",
        voteAverage = 8.2,
        voteCount = 1200,
        budget = 100L,
        revenue = 200L,
    )
}
