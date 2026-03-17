package com.example.themoviedb.core.data.repository

import com.example.themoviedb.core.data.model.MovieItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WatchListRepository {
    private val _watchList = MutableStateFlow<List<MovieItem>>(emptyList())
    val watchList: StateFlow<List<MovieItem>> = _watchList.asStateFlow()

    fun toggle(movie: MovieItem) {
        _watchList.update { current ->
            val isSaved = current.any { it.id == movie.id }
            if (isSaved) {
                current.filterNot { it.id == movie.id }
            } else {
                listOf(movie) + current
            }
        }
    }

    fun isBookmarked(movieId: Int): Boolean {
        return _watchList.value.any { it.id == movieId }
    }
}
