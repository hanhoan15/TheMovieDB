package com.example.themoviedb.feature

import com.example.themoviedb.fake.FakeMovieRepository
import com.example.themoviedb.fake.movie
import com.example.themoviedb.feature.search.SearchViewModel
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
class SearchViewModelTest {

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
    fun searchQueryChangedLoadsResults() = runTest {
        val repository = FakeMovieRepository().apply {
            searchResultsByQuery["spiderman"] = listOf(movie(id = 10, title = "Spider-Man"))
        }
        val viewModel = SearchViewModel(repository)

        viewModel.onSearchQueryChanged("spiderman")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("spiderman", state.searchQuery)
        assertEquals(1, state.searchResults.size)
        assertEquals("Spider-Man", state.searchResults.first().title)
        assertTrue(state.hasSearchAttempted)
        assertFalse(state.isSearchLoading)
    }

    @Test
    fun blankQueryClearsSearchState() = runTest {
        val repository = FakeMovieRepository().apply {
            searchResultsByQuery["spiderman"] = listOf(movie(id = 10, title = "Spider-Man"))
        }
        val viewModel = SearchViewModel(repository)

        viewModel.onSearchQueryChanged("spiderman")
        advanceUntilIdle()
        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertTrue(state.searchResults.isEmpty())
        assertFalse(state.hasSearchAttempted)
        assertFalse(state.isSearchLoading)
    }
}
