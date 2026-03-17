package com.example.themoviedb.core.di

import com.example.themoviedb.feature.detail.DetailViewModel
import com.example.themoviedb.feature.home.HomeViewModel
import com.example.themoviedb.feature.search.SearchViewModel
import com.example.themoviedb.feature.watchlist.WatchListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { params -> DetailViewModel(params.get(), get(), get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { WatchListViewModel(get()) }
}
