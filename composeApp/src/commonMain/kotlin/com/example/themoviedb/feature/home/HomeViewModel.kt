package com.example.themoviedb.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.themoviedb.core.data.model.AppTab
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val fallbackMovies = listOf(
    MovieItem(
        id = 787699,
        title = "Wonka",
        rating = 7.2,
        posterUrl = "https://media.themoviedb.org/t/p/w440_and_h660_face/buPFnHZ3xQy6vZEHxbHgL1Pc6CR.jpg",
        backdropUrl = "https://media.themoviedb.org/t/p/w533_and_h300_bestv2/yOm993lsJyPmBodlYjgpPwBjXP9.jpg",
    ),
    MovieItem(
        id = 572802,
        title = "Aquaman et le",
        rating = 6.7,
        posterUrl = "https://media.themoviedb.org/t/p/w440_and_h660_face/7lTnXOy0iNtBAdRP3TZvaKJ77F6.jpg",
        backdropUrl = "https://media.themoviedb.org/t/p/w533_and_h300_bestv2/4HodYYKEIsGOdinkGi2Ucz6X9i0.jpg",
    ),
    MovieItem(
        id = 955916,
        title = "En plein vol",
        rating = 6.4,
        posterUrl = "https://media.themoviedb.org/t/p/w440_and_h660_face/jCzNO0qugdo4a0NRcRVg3M9mQ8D.jpg",
        backdropUrl = "https://media.themoviedb.org/t/p/w533_and_h300_bestv2/oBIQDKcqNxKckjugtmzpIIOgoc4.jpg",
    ),
    MovieItem(
        id = 906126,
        title = "Le Cercle des neiges",
        rating = 8.0,
        posterUrl = "https://media.themoviedb.org/t/p/w440_and_h660_face/2e853FDVSIso600RqAMunPxiZjq.jpg",
        backdropUrl = "https://media.themoviedb.org/t/p/w533_and_h300_bestv2/rbYxIOtqL5i4fUBQqvBf4QmJxRk.jpg",
    ),
)

data class HomeUiState(
    val selectedTab: AppTab = AppTab.NOW_PLAYING,
    val movies: List<MovieItem> = fallbackMovies,
    val isListLoading: Boolean = false,
    val isPagingLoading: Boolean = false,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
)

class HomeViewModel(
    private val repository: MovieRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadMoviesJob: Job? = null

    init {
        loadFirstPage(AppTab.NOW_PLAYING)
    }

    fun onTabSelected(tab: AppTab) {
        if (_uiState.value.selectedTab == tab) return
        loadFirstPage(tab)
    }

    fun onLoadMore() {
        val state = _uiState.value
        if (state.isListLoading || state.isPagingLoading || !state.canLoadMore) return
        loadNextPage(
            tab = state.selectedTab,
            page = state.currentPage + 1,
        )
    }

    private fun loadFirstPage(tab: AppTab) {
        loadMoviesJob?.cancel()
        _uiState.update {
            it.copy(
                selectedTab = tab,
                isListLoading = true,
                isPagingLoading = false,
                currentPage = 1,
                canLoadMore = true,
            )
        }

        loadMoviesJob = viewModelScope.launch {
            val fetchedMovies = runCatching {
                repository.getMovies(tab.category, page = 1)
            }.getOrDefault(emptyList())
            val useFallback = fetchedMovies.isEmpty()
            val movies = if (useFallback) fallbackMovies else fetchedMovies

            _uiState.update {
                it.copy(
                    movies = movies,
                    isListLoading = false,
                    isPagingLoading = false,
                    currentPage = 1,
                    canLoadMore = !useFallback,
                )
            }
        }
    }

    private fun loadNextPage(tab: AppTab, page: Int) {
        loadMoviesJob?.cancel()
        _uiState.update { it.copy(isPagingLoading = true) }

        loadMoviesJob = viewModelScope.launch {
            val fetchedMovies = runCatching {
                repository.getMovies(tab.category, page = page)
            }.getOrDefault(emptyList())

            _uiState.update { current ->
                if (fetchedMovies.isEmpty()) {
                    current.copy(
                        isPagingLoading = false,
                        canLoadMore = false,
                    )
                } else {
                    current.copy(
                        movies = (current.movies + fetchedMovies).distinctBy { movie -> movie.id },
                        isPagingLoading = false,
                        currentPage = page,
                        canLoadMore = true,
                    )
                }
            }
        }
    }
}
