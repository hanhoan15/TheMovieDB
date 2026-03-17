package com.example.themoviedb.feature.watchlist

import androidx.lifecycle.ViewModel
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.data.repository.WatchListRepository
import kotlinx.coroutines.flow.StateFlow

class WatchListViewModel(
    private val watchListRepository: WatchListRepository,
) : ViewModel() {
    val watchList: StateFlow<List<MovieItem>> = watchListRepository.watchList
}
