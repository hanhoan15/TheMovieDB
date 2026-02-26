package com.example.themoviedb

object AppContainer {
    private const val accessToken =
        "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzOTRjYmE5YWNjNmY0NDNiMWFiZmM3NTA4NWI4OWFkYyIsIm5iZiI6MTUyMDM0NjU0Mi4xNzEsInN1YiI6IjVhOWVhNWFlOTI1MTQxMTAzNDAxMThkMSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.9WGldYXTk_wgxTYcUcDwyE2djALioxK39j7DAExdUXE"
    private const val apiKey = "394cba9acc6f443b1abfc75085b89adc"
    private const val imageBaseUrl = "https://image.tmdb.org/t/p/w500"

    private val movieRepository: MovieRepository by lazy {
        TmdbMovieRepository(
            accessToken = accessToken,
            apiKey = apiKey,
            imageBaseUrl = imageBaseUrl,
        )
    }

    fun provideMoviesViewModel(): MoviesViewModel {
        return MoviesViewModel(repository = movieRepository)
    }
}
