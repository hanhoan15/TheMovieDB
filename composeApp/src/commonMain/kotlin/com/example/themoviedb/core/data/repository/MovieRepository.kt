package com.example.themoviedb.core.data.repository

import com.example.themoviedb.core.data.model.MovieCategory
import com.example.themoviedb.core.data.model.MovieDetailItem
import com.example.themoviedb.core.data.model.MovieItem

interface MovieRepository {
    suspend fun getMovies(category: MovieCategory, page: Int): List<MovieItem>
    suspend fun getMovieDetail(movieId: Int): MovieDetailItem?
    suspend fun searchMovies(query: String, page: Int): List<MovieItem>
}
