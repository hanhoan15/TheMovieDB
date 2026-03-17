package com.example.themoviedb.core.data

import com.example.themoviedb.core.data.mapper.buildAvatarUrl
import com.example.themoviedb.core.data.mapper.buildImageUrl
import com.example.themoviedb.core.data.mapper.buildTrailerThumbnailUrl
import com.example.themoviedb.core.data.mapper.buildTrailerWatchUrl
import com.example.themoviedb.core.data.mapper.primaryGenreLabel
import com.example.themoviedb.core.data.mapper.releaseYear
import com.example.themoviedb.core.data.mapper.toFallbackDetail
import com.example.themoviedb.core.data.mapper.toMovieDetailItem
import com.example.themoviedb.core.data.mapper.toMovieItem
import com.example.themoviedb.core.data.mapper.toOneDecimalString
import com.example.themoviedb.core.data.mapper.toWatchListMovie
import com.example.themoviedb.core.data.model.MovieDetailItem
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.data.model.dto.GenreDto
import com.example.themoviedb.core.data.model.dto.MovieDetailDto
import com.example.themoviedb.core.data.model.dto.MovieResultDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MovieMapperTest {

    @Test
    fun movieResultDto_toMovieItem_returnsNullWithoutPoster() {
        val dto = MovieResultDto(id = 1, posterPath = null)
        assertNull(dto.toMovieItem("https://image.tmdb.org/t/p/w500"))
    }

    @Test
    fun movieResultDto_toMovieItem_mapsCorrectly() {
        val dto = MovieResultDto(
            id = 42,
            title = "Test",
            voteAverage = 7.5,
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2024-06-15",
            genreIds = listOf(28, 12),
        )
        val item = dto.toMovieItem("https://image.tmdb.org/t/p/w500")
        assertNotNull(item)
        assertEquals(42, item.id)
        assertEquals("Test", item.title)
        assertEquals(7.5, item.rating)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", item.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w500/backdrop.jpg", item.backdropUrl)
    }

    @Test
    fun movieDetailDto_toMovieDetailItem_mapsGenres() {
        val dto = MovieDetailDto(
            id = 1,
            title = "Detail",
            genres = listOf(GenreDto(28, "Action"), GenreDto(12, "Adventure")),
            voteAverage = 8.0,
            voteCount = 100,
            budget = 1000,
            revenue = 2000,
        )
        val item = dto.toMovieDetailItem("https://image.tmdb.org/t/p/w500")
        assertEquals(listOf("Action", "Adventure"), item.genres)
    }

    @Test
    fun buildTrailerWatchUrl_youtube() {
        assertEquals("https://www.youtube.com/watch?v=abc123", buildTrailerWatchUrl("YouTube", "abc123"))
    }

    @Test
    fun buildTrailerWatchUrl_vimeo() {
        assertEquals("https://vimeo.com/abc123", buildTrailerWatchUrl("Vimeo", "abc123"))
    }

    @Test
    fun buildTrailerWatchUrl_unknown_returnsNull() {
        assertNull(buildTrailerWatchUrl("Dailymotion", "abc123"))
    }

    @Test
    fun buildTrailerWatchUrl_blankKey_returnsNull() {
        assertNull(buildTrailerWatchUrl("YouTube", ""))
    }

    @Test
    fun buildTrailerThumbnailUrl_youtube() {
        assertEquals("https://img.youtube.com/vi/abc123/hqdefault.jpg", buildTrailerThumbnailUrl("YouTube", "abc123"))
    }

    @Test
    fun toOneDecimalString_roundsCorrectly() {
        assertEquals("7.5", 7.46.toOneDecimalString())
        assertEquals("8.0", 8.0.toOneDecimalString())
    }

    @Test
    fun buildImageUrl_nullPath_returnsNull() {
        assertNull(buildImageUrl("https://base", null))
    }

    @Test
    fun buildImageUrl_blankPath_returnsNull() {
        assertNull(buildImageUrl("https://base", ""))
    }

    @Test
    fun buildAvatarUrl_httpPath_returnsDirectly() {
        assertEquals("https://example.com/avatar.jpg", buildAvatarUrl("https://base", "/https://example.com/avatar.jpg"))
    }

    @Test
    fun releaseYear_extracts4Chars() {
        val movie = MovieItem(id = 1, title = "T", rating = 0.0, posterUrl = "", backdropUrl = null, releaseDate = "2024-06-15")
        assertEquals("2024", movie.releaseYear())
    }

    @Test
    fun releaseYear_blank_returnsDash() {
        val movie = MovieItem(id = 1, title = "T", rating = 0.0, posterUrl = "", backdropUrl = null, releaseDate = "")
        assertEquals("-", movie.releaseYear())
    }

    @Test
    fun primaryGenreLabel_actionGenre() {
        val movie = MovieItem(id = 1, title = "T", rating = 0.0, posterUrl = "", backdropUrl = null, genreIds = listOf(28))
        assertEquals("Action", movie.primaryGenreLabel())
    }

    @Test
    fun primaryGenreLabel_noGenres() {
        val movie = MovieItem(id = 1, title = "T", rating = 0.0, posterUrl = "", backdropUrl = null, genreIds = emptyList())
        assertEquals("Unknown", movie.primaryGenreLabel())
    }

    @Test
    fun toFallbackDetail_usesMovieItemFields() {
        val movie = MovieItem(id = 1, title = "Fallback", rating = 7.5, posterUrl = "poster", backdropUrl = "backdrop", releaseDate = "2024-01-01", genreIds = listOf(28))
        val detail = movie.toFallbackDetail()
        assertEquals(1, detail.id)
        assertEquals("Fallback", detail.title)
        assertEquals(7.5, detail.voteAverage)
        assertEquals("poster", detail.posterUrl)
        assertEquals("backdrop", detail.backdropUrl)
    }

    @Test
    fun toWatchListMovie_usesDetailFields() {
        val detail = MovieDetailItem(
            id = 1, title = "Detail", overview = "", posterUrl = "poster", backdropUrl = "backdrop",
            originalLanguage = "en", releaseDate = "2024", voteAverage = 8.0, voteCount = 100, budget = 0, revenue = 0,
        )
        val movie = detail.toWatchListMovie()
        assertEquals(1, movie.id)
        assertEquals("Detail", movie.title)
        assertEquals(8.0, movie.rating)
    }
}
