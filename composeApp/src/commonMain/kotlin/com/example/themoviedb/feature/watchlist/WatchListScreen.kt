package com.example.themoviedb.feature.watchlist

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.ui.components.AppBottomNavBar
import com.example.themoviedb.core.ui.components.BottomDestination
import com.example.themoviedb.core.ui.components.EmptyStateView
import com.example.themoviedb.core.ui.components.MovieListItem
import com.example.themoviedb.core.ui.theme.AppTypography
import com.example.themoviedb.core.ui.theme.AppColors
import org.koin.compose.viewmodel.koinViewModel
import themoviedb.composeapp.generated.resources.Res
import themoviedb.composeapp.generated.resources.watch_list_empty

@Composable
fun WatchListScreen(
    onMovieClick: (MovieItem) -> Unit,
    onBack: () -> Unit,
    onDestinationSelected: (BottomDestination) -> Unit,
    viewModel: WatchListViewModel = koinViewModel(),
) {
    val watchList by viewModel.watchList.collectAsState()

    Scaffold(
        containerColor = AppColors.ScreenBackground,
        bottomBar = {
            AppBottomNavBar(
                selected = BottomDestination.WATCH_LIST,
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.DetailBackButton,
                    modifier = Modifier.size(22.dp).clickable(onClick = onBack),
                )
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Watch list",
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = AppTypography.montserratSemiBold(),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        ),
                    )
                }
                Spacer(modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = watchList.isEmpty(),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(280))).togetherWith(
                        fadeOut(animationSpec = tween(200)),
                    )
                },
                label = "watchlist_content_transition",
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyStateView(
                        imageRes = Res.drawable.watch_list_empty,
                        title = "There Is No Movie Yet!",
                        subtitle = "Find your movie by Type title,\ncategories, years, etc",
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        items(watchList, key = { movie -> movie.id }) { movie ->
                            MovieListItem(movie = movie, onClick = { onMovieClick(movie) })
                        }
                    }
                }
            }
        }
    }
}
