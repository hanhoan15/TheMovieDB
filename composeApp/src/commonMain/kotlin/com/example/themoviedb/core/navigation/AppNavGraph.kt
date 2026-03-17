package com.example.themoviedb.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.themoviedb.core.ui.components.BottomDestination
import com.example.themoviedb.feature.detail.DetailScreen
import com.example.themoviedb.feature.home.HomeScreen
import com.example.themoviedb.feature.imageviewer.ImageViewerScreen
import com.example.themoviedb.feature.search.SearchScreen
import com.example.themoviedb.feature.watchlist.WatchListScreen
import com.example.themoviedb.feature.webview.WebViewScreen
import kotlinx.serialization.json.Json

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.Home,
        enterTransition = { NavTransitions.slideInForward() },
        exitTransition = { NavTransitions.slideOutForward() },
        popEnterTransition = { NavTransitions.slideInBack() },
        popExitTransition = { NavTransitions.slideOutBack() },
    ) {
        composable<AppRoutes.Home>(
            enterTransition = { NavTransitions.crossfadeIn() },
            exitTransition = { NavTransitions.crossfadeOut() },
        ) {
            HomeScreen(
                onMovieClick = { movie ->
                    navController.navigate(AppRoutes.Detail(movieId = movie.id))
                },
                onDestinationSelected = { dest ->
                    navigateToDestination(navController, dest)
                },
            )
        }

        composable<AppRoutes.Search>(
            enterTransition = { NavTransitions.crossfadeIn() },
            exitTransition = { NavTransitions.crossfadeOut() },
        ) {
            SearchScreen(
                onMovieClick = { movie ->
                    navController.navigate(AppRoutes.Detail(movieId = movie.id))
                },
                onBack = { navController.popBackStack() },
                onDestinationSelected = { dest ->
                    navigateToDestination(navController, dest)
                },
            )
        }

        composable<AppRoutes.WatchList>(
            enterTransition = { NavTransitions.crossfadeIn() },
            exitTransition = { NavTransitions.crossfadeOut() },
        ) {
            WatchListScreen(
                onMovieClick = { movie ->
                    navController.navigate(AppRoutes.Detail(movieId = movie.id))
                },
                onBack = { navController.popBackStack() },
                onDestinationSelected = { dest ->
                    navigateToDestination(navController, dest)
                },
            )
        }

        composable<AppRoutes.Detail> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoutes.Detail>()
            DetailScreen(
                movieId = route.movieId,
                onMovieClick = { movie ->
                    navController.navigate(AppRoutes.Detail(movieId = movie.id))
                },
                onOpenLink = { url ->
                    navController.navigate(AppRoutes.Web(url = url))
                },
                onImageClick = { images, index ->
                    if (images.isNotEmpty()) {
                        val imagesJson = Json.encodeToString(images)
                        navController.navigate(
                            AppRoutes.ImageViewer(
                                imagesJson = imagesJson,
                                initialIndex = index.coerceIn(0, images.lastIndex),
                            ),
                        )
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable<AppRoutes.Web> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoutes.Web>()
            WebViewScreen(
                url = route.url,
                onBack = { navController.popBackStack() },
            )
        }

        composable<AppRoutes.ImageViewer> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoutes.ImageViewer>()
            val images = runCatching {
                Json.decodeFromString<List<String>>(route.imagesJson)
            }.getOrDefault(emptyList())
            ImageViewerScreen(
                images = images,
                initialIndex = route.initialIndex,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private fun navigateToDestination(navController: NavHostController, dest: BottomDestination) {
    val route: AppRoutes = when (dest) {
        BottomDestination.HOME -> AppRoutes.Home
        BottomDestination.SEARCH -> AppRoutes.Search
        BottomDestination.WATCH_LIST -> AppRoutes.WatchList
    }
    navController.navigate(route) {
        popUpTo(AppRoutes.Home) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
