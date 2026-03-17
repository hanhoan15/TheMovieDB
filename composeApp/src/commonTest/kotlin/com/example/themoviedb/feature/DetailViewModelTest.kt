package com.example.themoviedb.feature

import com.example.themoviedb.core.data.repository.WatchListRepository
import com.example.themoviedb.fake.FakeMovieRepository
import com.example.themoviedb.fake.detail
import com.example.themoviedb.fake.movie
import com.example.themoviedb.feature.detail.DetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsDetailSuccessfully() = runTest {
        val movieDetail = detail(id = 42, title = "Detail Title")
        val repository = FakeMovieRepository().apply {
            detailById[42] = movieDetail
        }
        val watchListRepository = WatchListRepository()
        val viewModel = DetailViewModel(42, repository, watchListRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(movieDetail, state.detailMovie)
    }

    @Test
    fun returnsNullWhenDetailMissing() = runTest {
        val repository = FakeMovieRepository()
        val watchListRepository = WatchListRepository()
        val viewModel = DetailViewModel(99, repository, watchListRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(null, state.detailMovie)
    }

    @Test
    fun toggleBookmarkAddsAndRemoves() = runTest {
        val movieDetail = detail(id = 42, title = "Bookmark Test")
        val repository = FakeMovieRepository().apply {
            detailById[42] = movieDetail
        }
        val watchListRepository = WatchListRepository()
        val viewModel = DetailViewModel(42, repository, watchListRepository)
        advanceUntilIdle()

        viewModel.toggleBookmark()
        advanceUntilIdle()
        assertEquals(1, watchListRepository.watchList.value.size)
        assertTrue(watchListRepository.isBookmarked(42))

        viewModel.toggleBookmark()
        advanceUntilIdle()
        assertTrue(watchListRepository.watchList.value.isEmpty())
        assertFalse(watchListRepository.isBookmarked(42))
    }
}
