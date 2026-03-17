package com.example.themoviedb.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoutes {
    @Serializable
    data object Home : AppRoutes

    @Serializable
    data object Search : AppRoutes

    @Serializable
    data object WatchList : AppRoutes

    @Serializable
    data class Detail(val movieId: Int) : AppRoutes

    @Serializable
    data class Web(val url: String) : AppRoutes

    @Serializable
    data class ImageViewer(val imagesJson: String, val initialIndex: Int) : AppRoutes
}
