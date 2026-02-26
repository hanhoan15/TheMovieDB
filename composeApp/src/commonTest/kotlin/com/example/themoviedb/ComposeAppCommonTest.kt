package com.example.themoviedb

import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeAppCommonTest {

    @Test
    fun init_loadsNowPlayingMovies() = runTest {
        val repository = FakeMovieRepository().apply {
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 1] = listOf(movie(id = 1, title = "Now"))
        }
        val viewModel = MoviesViewModel(repository = repository, scope = this)

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
        val viewModel = MoviesViewModel(repository = repository, scope = this)
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
    fun onMovieSelected_loadsDetailSuccessfully() = runTest {
        val selected = movie(id = 42, title = "Selected")
        val detail = detail(id = 42, title = "Detail Title")
        val repository = FakeMovieRepository().apply {
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 1] = listOf(selected)
            detailById[42] = detail
        }
        val viewModel = MoviesViewModel(repository = repository, scope = this)
        advanceUntilIdle()

        viewModel.onMovieSelected(selected)
        val loadingState = viewModel.uiState.value
        assertTrue(loadingState.isDetailLoading)
        assertEquals("Selected", loadingState.detailMovie?.title)
        assertEquals(0, loadingState.detailMovie?.voteCount)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDetailLoading)
        assertEquals(detail, state.detailMovie)
        assertEquals(listOf(42), repository.requestedDetailIds)
    }

    @Test
    fun onMovieSelected_usesFallbackWhenDetailMissing() = runTest {
        val selected = movie(id = 99, title = "Fallback")
        val repository = FakeMovieRepository().apply {
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 1] = listOf(selected)
        }
        val viewModel = MoviesViewModel(repository = repository, scope = this)
        advanceUntilIdle()

        viewModel.onMovieSelected(selected)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Fallback", state.detailMovie?.title)
        assertEquals(0, state.detailMovie?.voteCount)
        assertFalse(state.isDetailLoading)
    }

    @Test
    fun onBackFromDetail_clearsDetailState() = runTest {
        val selected = movie(id = 13, title = "Back Test")
        val repository = FakeMovieRepository().apply {
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 1] = listOf(selected)
        }
        val viewModel = MoviesViewModel(repository = repository, scope = this)
        advanceUntilIdle()
        viewModel.onMovieSelected(selected)
        advanceUntilIdle()

        viewModel.onBackFromDetail()

        val state = viewModel.uiState.value
        assertNull(state.selectedMovie)
        assertNull(state.detailMovie)
        assertFalse(state.isDetailLoading)
    }

    @Test
    fun onToggleWatchListForSelectedMovie_addsMovie() = runTest {
        val selected = movie(id = 77, title = "Saved")
        val repository = FakeMovieRepository().apply {
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 1] = listOf(selected)
        }
        val viewModel = MoviesViewModel(repository = repository, scope = this)
        advanceUntilIdle()

        viewModel.onMovieSelected(selected)
        advanceUntilIdle()
        viewModel.onToggleWatchListForSelectedMovie()

        val state = viewModel.uiState.value
        assertEquals(1, state.watchList.size)
        assertEquals(77, state.watchList.first().id)
    }

    @Test
    fun onToggleWatchListForSelectedMovie_removesMovieWhenAlreadySaved() = runTest {
        val selected = movie(id = 78, title = "Unsaved")
        val repository = FakeMovieRepository().apply {
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 1] = listOf(selected)
        }
        val viewModel = MoviesViewModel(repository = repository, scope = this)
        advanceUntilIdle()

        viewModel.onMovieSelected(selected)
        advanceUntilIdle()
        viewModel.onToggleWatchListForSelectedMovie()
        viewModel.onToggleWatchListForSelectedMovie()

        val state = viewModel.uiState.value
        assertTrue(state.watchList.isEmpty())
    }

    @Test
    fun loadMovies_usesFallbackWhenRepositoryFails() = runTest {
        val repository = FakeMovieRepository().apply {
            throwOnGetMovies = true
        }
        val viewModel = MoviesViewModel(repository = repository, scope = this)
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
        val viewModel = MoviesViewModel(repository = repository, scope = this)
        advanceUntilIdle()

        viewModel.onLoadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentPage)
        assertEquals(2, state.movies.size)
        assertEquals(listOf("First", "Second"), state.movies.map { it.title })
        assertFalse(state.isPagingLoading)
        assertEquals(
            listOf(
                MovieCategory.NOW_PLAYING to 1,
                MovieCategory.NOW_PLAYING to 2,
            ),
            repository.requestedPages,
        )
    }

    @Test
    fun onLoadMore_stopsWhenNoMoreData() = runTest {
        val repository = FakeMovieRepository().apply {
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 1] = listOf(movie(id = 1, title = "First"))
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 2] = emptyList()
        }
        val viewModel = MoviesViewModel(repository = repository, scope = this)
        advanceUntilIdle()

        viewModel.onLoadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentPage)
        assertFalse(state.canLoadMore)
        assertFalse(state.isPagingLoading)
    }

    @Test
    fun onSearchQueryChanged_loadsSearchResults() = runTest {
        val repository = FakeMovieRepository().apply {
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 1] = listOf(movie(id = 1, title = "Now"))
            searchResultsByQuery["spiderman"] = listOf(movie(id = 10, title = "Spider-Man"))
        }
        val viewModel = MoviesViewModel(repository = repository, scope = this)
        advanceUntilIdle()

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
    fun onSearchQueryChanged_blankQueryClearsSearchState() = runTest {
        val repository = FakeMovieRepository().apply {
            moviesByCategoryAndPage[MovieCategory.NOW_PLAYING to 1] = listOf(movie(id = 1, title = "Now"))
            searchResultsByQuery["spiderman"] = listOf(movie(id = 10, title = "Spider-Man"))
        }
        val viewModel = MoviesViewModel(repository = repository, scope = this)
        advanceUntilIdle()

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

private class FakeMovieRepository : MovieRepository {
    val moviesByCategoryAndPage = mutableMapOf<Pair<MovieCategory, Int>, List<MovieItem>>()
    val detailById = mutableMapOf<Int, MovieDetailItem>()
    val searchResultsByQuery = mutableMapOf<String, List<MovieItem>>()
    val requestedCategories = mutableListOf<MovieCategory>()
    val requestedPages = mutableListOf<Pair<MovieCategory, Int>>()
    val requestedDetailIds = mutableListOf<Int>()
    val requestedSearchQueries = mutableListOf<String>()
    var throwOnGetMovies = false

    override suspend fun getMovies(category: MovieCategory, page: Int): List<MovieItem> {
        requestedCategories += category
        requestedPages += category to page
        if (throwOnGetMovies) error("Repository failure")
        return moviesByCategoryAndPage[category to page].orEmpty()
    }

    override suspend fun getMovieDetail(movieId: Int): MovieDetailItem? {
        requestedDetailIds += movieId
        return detailById[movieId]
    }

    override suspend fun searchMovies(query: String, page: Int): List<MovieItem> {
        requestedSearchQueries += query
        return searchResultsByQuery[query].orEmpty()
    }
}

private fun movie(id: Int, title: String): MovieItem {
    return MovieItem(
        id = id,
        title = title,
        rating = 7.0,
        posterUrl = "https://example.com/poster.jpg",
        backdropUrl = "https://example.com/backdrop.jpg",
    )
}

private fun detail(id: Int, title: String): MovieDetailItem {
    return MovieDetailItem(
        id = id,
        title = title,
        overview = "Overview",
        posterUrl = "https://example.com/poster.jpg",
        backdropUrl = "https://example.com/backdrop.jpg",
        originalLanguage = "en",
        releaseDate = "2024-01-01",
        voteAverage = 8.2,
        voteCount = 1200,
        budget = 100L,
        revenue = 200L,
    )
}
