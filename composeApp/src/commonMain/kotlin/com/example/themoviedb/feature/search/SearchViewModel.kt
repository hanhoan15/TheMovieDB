package com.example.themoviedb.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<MovieItem> = emptyList(),
    val isSearchLoading: Boolean = false,
    val hasSearchAttempted: Boolean = false,
)

class SearchViewModel(
    private val repository: MovieRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

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

        searchJob = viewModelScope.launch {
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
}
