package com.example.themoviedb.fake

import com.example.themoviedb.core.data.model.MovieCategory
import com.example.themoviedb.core.data.model.MovieDetailItem
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.data.repository.MovieRepository

class FakeMovieRepository : MovieRepository {
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
