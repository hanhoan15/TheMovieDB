package com.example.themoviedb

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MoviesUiState(
    val selectedTab: AppTab = AppTab.NOW_PLAYING,
    val movies: List<MovieItem> = fallbackMovies,
    val isListLoading: Boolean = false,
    val isPagingLoading: Boolean = false,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val selectedMovie: MovieItem? = null,
    val detailMovie: MovieDetailItem? = null,
    val isDetailLoading: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<MovieItem> = emptyList(),
    val isSearchLoading: Boolean = false,
    val hasSearchAttempted: Boolean = false,
    val watchList: List<MovieItem> = emptyList(),
)

class MoviesViewModel(
    private val repository: MovieRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    private var loadMoviesJob: Job? = null
    private var loadDetailJob: Job? = null
    private var searchJob: Job? = null

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
        if (state.selectedMovie != null) return
        loadNextPage(
            tab = state.selectedTab,
            page = state.currentPage + 1,
        )
    }

    fun onMovieSelected(movie: MovieItem) {
        _uiState.update {
            it.copy(
                selectedMovie = movie,
                detailMovie = movie.toFallbackDetail(),
                isDetailLoading = true,
            )
        }

        loadDetailJob?.cancel()
        loadDetailJob = scope.launch {
            val detail = repository.getMovieDetail(movie.id) ?: movie.toFallbackDetail()
            _uiState.update {
                it.copy(
                    detailMovie = detail,
                    isDetailLoading = false,
                )
            }
        }
    }

    fun onBackFromDetail() {
        loadDetailJob?.cancel()
        _uiState.update {
            it.copy(
                selectedMovie = null,
                detailMovie = null,
                isDetailLoading = false,
            )
        }
    }

    fun onToggleWatchListForSelectedMovie() {
        _uiState.update { current ->
            val selected = current.selectedMovie ?: current.detailMovie?.toWatchListMovie() ?: return@update current
            val isSaved = current.watchList.any { it.id == selected.id }
            if (isSaved) {
                current.copy(
                    watchList = current.watchList.filterNot { it.id == selected.id },
                )
            } else {
                current.copy(
                    watchList = listOf(selected) + current.watchList,
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                isSearchLoading = query.isNotBlank(),
                hasSearchAttempted = false,
                searchResults = if (query.isBlank()) emptyList() else current.searchResults,
            )
        }

        searchJob?.cancel()
        if (query.isBlank()) return

        searchJob = scope.launch {
            delay(350)
            val results = runCatching {
                repository.searchMovies(query = query, page = 1)
            }.getOrDefault(emptyList())

            _uiState.update { current ->
                if (current.searchQuery != query) {
                    current
                } else {
                    current.copy(
                        isSearchLoading = false,
                        hasSearchAttempted = true,
                        searchResults = results,
                    )
                }
            }
        }
    }

    fun clear() {
        searchJob?.cancel()
        scope.cancel()
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

        loadMoviesJob = scope.launch {
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

        loadMoviesJob = scope.launch {
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
