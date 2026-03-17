package com.example.themoviedb.feature

import com.example.themoviedb.core.data.model.AppTab
import com.example.themoviedb.core.data.model.MovieCategory
import com.example.themoviedb.fake.FakeMovieRepository
import com.example.themoviedb.fake.movie
import com.example.themoviedb.feature.home.HomeViewModel
import com.example.themoviedb.feature.home.fallbackMovies
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

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
    fun init_loadsNowPlayingMovies() = runTest {
        val repository = FakeMovieRepository().apply {
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 1] = listOf(movie(id = 1, title = "Now"))
        }
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppTab.NOW_PLAYING, state.selectedTab)
        assertEquals(1, state.movies.size)
        assertEquals("Now", state.movies.first().title)
        assertFalse(state.isListLoading)
        assertFalse(state.isPagingLoading)
        assertEquals(1, state.currentPage)
        assertTrue(state.canLoadMore)
        assertEquals(listOf(MovieCategory.NOW_PLAYING), repository.requestedCategories)
    }

    @Test
    fun onTabSelected_loadsRequestedTabAndSkipsDuplicateSelection() = runTest {
        val repository = FakeMovieRepository().apply {
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 1] = listOf(movie(id = 1, title = "Now"))
            moviesByCategoryAndPage[MovieCategory.TOP_RATED to 1] = listOf(movie(id = 2, title = "Top"))
        }
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.onTabSelected(AppTab.TOP_RATED)
        advanceUntilIdle()
        viewModel.onTabSelected(AppTab.TOP_RATED)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AppTab.TOP_RATED, state.selectedTab)
        assertEquals("Top", state.movies.first().title)
        assertEquals(
            listOf(MovieCategory.NOW_PLAYING, MovieCategory.TOP_RATED),
            repository.requestedCategories,
        )
    }

    @Test
    fun loadMovies_usesFallbackWhenRepositoryFails() = runTest {
        val repository = FakeMovieRepository().apply {
            throwOnGetMovies = true
        }
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(fallbackMovies, state.movies)
        assertFalse(state.isListLoading)
        assertFalse(state.canLoadMore)
    }

    @Test
    fun onLoadMore_fetchesNextPageAndAppends() = runTest {
        val repository = FakeMovieRepository().apply {
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 1] = listOf(movie(id = 1, title = "First"))
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 2] = listOf(movie(id = 2, title = "Second"))
        }
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.onLoadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentPage)
        assertEquals(2, state.movies.size)
        assertEquals(listOf("First", "Second"), state.movies.map { it.title })
        assertFalse(state.isPagingLoading)
    }

    @Test
    fun onLoadMore_stopsWhenNoMoreData() = runTest {
        val repository = FakeMovieRepository().apply {
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 1] = listOf(movie(id = 1, title = "First"))
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 2] = emptyList()
        }
        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.onLoadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentPage)
        assertFalse(state.canLoadMore)
    }
}
