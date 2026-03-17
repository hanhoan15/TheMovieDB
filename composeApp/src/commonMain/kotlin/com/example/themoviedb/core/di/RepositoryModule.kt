package com.example.themoviedb.core.di

import com.example.themoviedb.core.data.repository.MovieRepository
import com.example.themoviedb.core.data.repository.TmdbMovieRepository
import com.example.themoviedb.core.data.repository.WatchListRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<MovieRepository> { TmdbMovieRepository(apiService = get()) }
    single { WatchListRepository() }
}
