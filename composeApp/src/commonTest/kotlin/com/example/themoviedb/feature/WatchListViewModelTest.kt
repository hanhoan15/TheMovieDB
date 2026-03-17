package com.example.themoviedb.feature

import com.example.themoviedb.core.data.repository.WatchListRepository
import com.example.themoviedb.fake.movie
import com.example.themoviedb.feature.watchlist.WatchListViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WatchListViewModelTest {

    @Test
    fun watchListReflectsRepositoryState() {
        val watchListRepository = WatchListRepository()
        val viewModel = WatchListViewModel(watchListRepository)

        assertTrue(viewModel.watchList.value.isEmpty())

        val movieItem = movie(id = 1, title = "Test Movie")
        watchListRepository.toggle(movieItem)

        assertEquals(1, viewModel.watchList.value.size)
        assertEquals("Test Movie", viewModel.watchList.value.first().title)

        watchListRepository.toggle(movieItem)
        assertTrue(viewModel.watchList.value.isEmpty())
    }
}
