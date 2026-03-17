package com.example.themoviedb.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.themoviedb.core.data.mapper.toFallbackDetail
import com.example.themoviedb.core.data.mapper.toWatchListMovie
import com.example.themoviedb.core.data.model.MovieDetailItem
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.data.repository.MovieRepository
import com.example.themoviedb.core.data.repository.WatchListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val detailMovie: MovieDetailItem? = null,
    val isLoading: Boolean = true,
)

class DetailViewModel(
    private val movieId: Int,
    private val repository: MovieRepository,
    private val watchListRepository: WatchListRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    val isBookmarked: StateFlow<Boolean> = watchListRepository.watchList
        .map { list -> list.any { it.id == movieId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadDetail()
    }

    fun toggleBookmark() {
        val movie = _uiState.value.detailMovie?.toWatchListMovie() ?: return
        watchListRepository.toggle(movie)
    }

    private fun loadDetail() {
        viewModelScope.launch {
            val detail = repository.getMovieDetail(movieId)
            _uiState.update {
                it.copy(
                    detailMovie = detail,
                    isLoading = false,
                )
            }
        }
    }
}
