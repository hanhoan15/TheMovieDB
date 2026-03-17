package com.example.themoviedb.feature.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.ui.components.AppBottomNavBar
import com.example.themoviedb.core.ui.components.BottomDestination
import com.example.themoviedb.core.ui.components.EmptyStateView
import com.example.themoviedb.core.ui.components.MovieListItem
import com.example.themoviedb.core.ui.components.MovieListItemPlaceholder
import com.example.themoviedb.core.ui.components.SearchInputField
import com.example.themoviedb.core.ui.theme.AppColors
import org.koin.compose.viewmodel.koinViewModel
import themoviedb.composeapp.generated.resources.Res
import themoviedb.composeapp.generated.resources.no_results

private enum class SearchContentState {
    BLANK,
    LOADING,
    EMPTY,
    RESULTS,
}

@Composable
fun SearchScreen(
    onMovieClick: (MovieItem) -> Unit,
    onBack: () -> Unit,
    onDestinationSelected: (BottomDestination) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppColors.ScreenBackground,
        bottomBar = {
            AppBottomNavBar(
                selected = BottomDestination.SEARCH,
                onSelect = onDestinationSelected,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.ScreenBackground)
                .padding(innerPadding)
                .padding(horizontal = 25.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.DetailBackButton,
                    modifier = Modifier.size(22.dp).clickable(onClick = onBack),
                )
                Box(modifier = Modifier.weight(1f), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(
                        text = "Search",
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = com.example.themoviedb.core.ui.theme.AppTypography.montserratSemiBold(),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            fontSize = 16.sp,
                        ),
                    )
                }
                Spacer(modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))

            SearchInputField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
            )

            Spacer(modifier = Modifier.height(18.dp))

            val contentState = when {
                uiState.searchQuery.isBlank() -> SearchContentState.BLANK
                uiState.isSearchLoading -> SearchContentState.LOADING
                uiState.hasSearchAttempted && uiState.searchResults.isEmpty() -> SearchContentState.EMPTY
                else -> SearchContentState.RESULTS
            }

            AnimatedContent(
                targetState = contentState,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(250))).togetherWith(
                        fadeOut(animationSpec = tween(180)),
                    )
                },
                label = "search_content_transition",
            ) { state ->
                when (state) {
                    SearchContentState.LOADING -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                        ) {
                            items(5) { MovieListItemPlaceholder() }
                        }
                    }
                    SearchContentState.EMPTY -> {
                        EmptyStateView(
                            imageRes = Res.drawable.no_results,
                            title = "We Are Sorry, We Can\nNot Find The Movie :(",
                            subtitle = "Find your movie by Type title,\ncategories, years, etc",
                            titleColor = AppColors.NoResultTitle,
                            subtitleColor = AppColors.NoResultSubtitle,
                        )
                    }
                    SearchContentState.BLANK -> {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                    SearchContentState.RESULTS -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                        ) {
                            items(uiState.searchResults, key = { movie -> movie.id }) { movie ->
                                MovieListItem(movie = movie, onClick = { onMovieClick(movie) })
                            }
                        }
                    }
                }
            }
        }
    }
}
