package com.example.themoviedb.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.themoviedb.core.data.model.AppTab
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.ui.components.AppBottomNavBar
import com.example.themoviedb.core.ui.components.BottomDestination
import com.example.themoviedb.core.ui.components.CategoryTabs
import com.example.themoviedb.core.ui.components.FeaturedMovieCard
import com.example.themoviedb.core.ui.components.MovieCard
import com.example.themoviedb.core.ui.components.SearchBarReadOnly
import com.example.themoviedb.core.ui.components.SectionHeader
import com.example.themoviedb.core.ui.components.ShimmerPlaceholder
import com.example.themoviedb.core.ui.theme.AppColors
import com.example.themoviedb.core.ui.theme.Dimensions
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onMovieClick: (MovieItem) -> Unit,
    onDestinationSelected: (BottomDestination) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()
    val tabs = listOf(
        AppTab.NOW_PLAYING,
        AppTab.UPCOMING,
        AppTab.TOP_RATED,
        AppTab.POPULAR,
    )

    LaunchedEffect(gridState, uiState.movies.size, uiState.isListLoading, uiState.isPagingLoading) {
        snapshotFlow {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val nearBottom = totalItems > 0 && lastVisible >= totalItems - 6
            nearBottom && !uiState.isListLoading && !uiState.isPagingLoading
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore) viewModel.onLoadMore()
            }
    }

    Scaffold(
        containerColor = AppColors.ScreenBackground,
        bottomBar = {
            AppBottomNavBar(
                selected = BottomDestination.HOME,
                onSelect = onDestinationSelected,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.ScreenBackground),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                state = gridState,
                contentPadding = PaddingValues(
                    start = Dimensions.ScreenHorizontalPadding,
                    end = Dimensions.ScreenHorizontalPadding,
                    top = Dimensions.ScreenVerticalPadding,
                    bottom = Dimensions.ScreenVerticalPadding,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        SectionHeader(title = "What do you want to watch?")
                        Spacer(modifier = Modifier.height(14.dp))
                        SearchBarReadOnly(
                            onClick = { onDestinationSelected(BottomDestination.SEARCH) },
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        if (uiState.isListLoading) {
                            FeaturedMoviesRowPlaceholder()
                        } else {
                            FeaturedMoviesRow(
                                movies = uiState.movies,
                                onMovieClick = onMovieClick,
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        CategoryTabs(
                            tabs = tabs,
                            selectedTab = uiState.selectedTab,
                            onTabSelected = viewModel::onTabSelected,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (uiState.isListLoading) {
                    items(12) {
                        PosterGridPlaceholder()
                    }
                } else {
                    items(uiState.movies.size, key = { index -> uiState.movies[index].id }) { index ->
                        MovieCard(
                            movie = uiState.movies[index],
                            onClick = { onMovieClick(uiState.movies[index]) },
                        )
                    }
                }
            }

            if (uiState.isPagingLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.PagingOverlay),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.PagingBadge,
                    ) {
                        Text(
                            text = "Loading more...",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedMoviesRow(
    movies: List<MovieItem>,
    onMovieClick: (MovieItem) -> Unit,
) {
    val featuredMovies = movies.take(8)
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
    ) {
        itemsIndexed(featuredMovies) { index, movie ->
            FeaturedMovieCard(
                movie = movie,
                index = index,
                onClick = { onMovieClick(movie) },
            )
        }
    }
}

@Composable
private fun FeaturedMoviesRowPlaceholder() {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
    ) {
        items(4) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(Dimensions.FeaturedCardSize),
            ) {
                ShimmerPlaceholder(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun PosterGridPlaceholder() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.GridPosterHeight),
    ) {
        ShimmerPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.GridPosterHeight),
        )
    }
}
