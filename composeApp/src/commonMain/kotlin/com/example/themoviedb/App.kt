package com.example.themoviedb

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import themoviedb.composeapp.generated.resources.Res
import themoviedb.composeapp.generated.resources.detail_calendar_icon
import themoviedb.composeapp.generated.resources.detail_clock_icon
import themoviedb.composeapp.generated.resources.detail_ticket_icon
import themoviedb.composeapp.generated.resources.home_icon
import themoviedb.composeapp.generated.resources.montserrat_medium
import themoviedb.composeapp.generated.resources.montserrat_regular
import themoviedb.composeapp.generated.resources.montserrat_semibold
import themoviedb.composeapp.generated.resources.no_results
import themoviedb.composeapp.generated.resources.poppins_medium
import themoviedb.composeapp.generated.resources.poppins_regular
import themoviedb.composeapp.generated.resources.poppins_semibold
import themoviedb.composeapp.generated.resources.review_avatar_default
import themoviedb.composeapp.generated.resources.roboto_medium
import themoviedb.composeapp.generated.resources.roboto_regular
import themoviedb.composeapp.generated.resources.save_icon
import themoviedb.composeapp.generated.resources.save_icon_detail
import themoviedb.composeapp.generated.resources.search_icon
import themoviedb.composeapp.generated.resources.search_textbox_icon
import themoviedb.composeapp.generated.resources.watch_list_empty

private val SCREEN_BG = Color(0xFF1A2232)
private val SEARCH_BG = Color(0xFF2B3443)
private val FEATURED_CARD_SIZE = DpSize(width = 132.dp, height = 190.dp)
private val GRID_POSTER_HEIGHT = 165.dp
private val DETAIL_BACKDROP_HEIGHT = 220.dp
private val DETAIL_POSTER_SIZE = DpSize(width = 145.dp, height = 210.dp)

@Composable
private fun AnimatedImagePlaceholder(modifier: Modifier = Modifier) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "image_placeholder")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1300, easing = LinearEasing)),
        label = "image_placeholder_progress",
    )
    val height = size.height.toFloat().coerceAtLeast(1f)
    val startY = (progress * 2f - 1f) * height
    val endY = startY + height
    val brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF232B3A),
            Color(0xFF3D495D),
            Color(0xFF232B3A),
        ),
        startY = startY,
        endY = endY,
    )

    Box(
        modifier = modifier
            .background(Color(0xFF1D2533))
            .onSizeChanged { size = it },
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush))
    }
}

@Composable
private fun AsyncImageWithAnimatedPlaceholder(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    Box(modifier = modifier.background(Color(0xFF1D2533))) {
        AnimatedImagePlaceholder(modifier = Modifier.fillMaxSize())
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun montserratFontFamily(): FontFamily = FontFamily(Font(Res.font.montserrat_regular))

@Composable
private fun montserratMediumFontFamily(): FontFamily = FontFamily(Font(Res.font.montserrat_medium))

@Composable
private fun montserratSemiBoldFontFamily(): FontFamily = FontFamily(Font(Res.font.montserrat_semibold))

@Composable
private fun poppinsMediumFontFamily(): FontFamily = FontFamily(Font(Res.font.poppins_medium))

@Composable
private fun poppinsRegularFontFamily(): FontFamily = FontFamily(Font(Res.font.poppins_regular))

@Composable
private fun poppinsSemiBoldFontFamily(): FontFamily = FontFamily(Font(Res.font.poppins_semibold))

@Composable
private fun robotoMediumFontFamily(): FontFamily = FontFamily(Font(Res.font.roboto_medium))

@Composable
private fun robotoRegularFontFamily(): FontFamily = FontFamily(Font(Res.font.roboto_regular))

enum class BottomDestination {
    HOME,
    SEARCH,
    WATCH_LIST,
}

private enum class SearchContentState {
    BLANK,
    LOADING,
    EMPTY,
    RESULTS,
}

private sealed interface AppRoute {
    data class Main(val destination: BottomDestination) : AppRoute
    data class Detail(val movieId: Int) : AppRoute
    data class Web(val url: String) : AppRoute
    data class ImageViewer(val images: List<String>, val initialIndex: Int) : AppRoute
}

private data class ImageViewerState(
    val images: List<String>,
    val initialIndex: Int,
)

@Composable
@Preview
fun App() {
    val viewModel = remember { AppContainer.provideMoviesViewModel() }
    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }

    val uiState by viewModel.uiState.collectAsState()
    var destination by remember { mutableStateOf(BottomDestination.HOME) }
    var openedUrl by remember { mutableStateOf<String?>(null) }
    var openedImageViewer by remember { mutableStateOf<ImageViewerState?>(null) }
    val route = when {
        openedImageViewer != null -> {
            AppRoute.ImageViewer(
                images = openedImageViewer!!.images,
                initialIndex = openedImageViewer!!.initialIndex,
            )
        }
        !openedUrl.isNullOrBlank() -> AppRoute.Web(openedUrl!!)
        uiState.selectedMovie != null -> AppRoute.Detail(uiState.selectedMovie!!.id)
        else -> AppRoute.Main(destination)
    }

    MaterialTheme {
        AnimatedContent(
            targetState = route,
            transitionSpec = {
                (
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth / 4 },
                        animationSpec = tween(280),
                    ) + fadeIn(animationSpec = tween(280))
                    ).togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 6 },
                        animationSpec = tween(220),
                    ) + fadeOut(animationSpec = tween(220)),
                )
            },
            label = "app_route_transition",
        ) { currentRoute ->
            when (currentRoute) {
                is AppRoute.ImageViewer -> {
                    ImageViewerScreen(
                        images = currentRoute.images,
                        initialIndex = currentRoute.initialIndex,
                        onBack = { openedImageViewer = null },
                    )
                }

                is AppRoute.Web -> {
                    InAppBrowserScreen(
                        url = currentRoute.url,
                        onBack = { openedUrl = null },
                    )
                }

                is AppRoute.Detail -> {
                    val detailMovie = when {
                        uiState.detailMovie?.id == currentRoute.movieId -> uiState.detailMovie
                        uiState.selectedMovie?.id == currentRoute.movieId -> uiState.selectedMovie?.toFallbackDetail()
                        else -> uiState.watchList.firstOrNull { it.id == currentRoute.movieId }?.toFallbackDetail()
                    }
                    if (detailMovie == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SCREEN_BG),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Loading details...",
                                color = Color(0xFFAEB8C9),
                            )
                        }
                        return@AnimatedContent
                    }
                    MovieDetailScreen(
                        movie = detailMovie,
                        isLoading = uiState.isDetailLoading,
                        isBookmarked = uiState.watchList.any { it.id == currentRoute.movieId },
                        onToggleBookmark = viewModel::onToggleWatchListForSelectedMovie,
                        onMovieClick = viewModel::onMovieSelected,
                        onOpenLink = { url -> openedUrl = url },
                        onImageClick = { images, index ->
                            if (images.isNotEmpty()) {
                                openedImageViewer = ImageViewerState(
                                    images = images,
                                    initialIndex = index.coerceIn(0, images.lastIndex),
                                )
                            }
                        },
                        onBack = viewModel::onBackFromDetail,
                    )
                }

                is AppRoute.Main -> when (currentRoute.destination) {
                    BottomDestination.HOME -> {
                        MovieListScreen(
                            movies = uiState.movies,
                            isLoading = uiState.isListLoading,
                            isPagingLoading = uiState.isPagingLoading,
                            selectedTab = uiState.selectedTab,
                            onMovieClick = viewModel::onMovieSelected,
                            onTabSelected = viewModel::onTabSelected,
                            onLoadMore = viewModel::onLoadMore,
                            onDestinationSelected = { destination = it },
                        )
                    }

                    BottomDestination.SEARCH -> {
                        SearchScreen(
                            query = uiState.searchQuery,
                            results = uiState.searchResults,
                            isLoading = uiState.isSearchLoading,
                            hasSearchAttempted = uiState.hasSearchAttempted,
                            onQueryChanged = viewModel::onSearchQueryChanged,
                            onMovieClick = viewModel::onMovieSelected,
                            onBack = { destination = BottomDestination.HOME },
                            onDestinationSelected = { destination = it },
                        )
                    }

                    BottomDestination.WATCH_LIST -> {
                        WatchListScreen(
                            watchList = uiState.watchList,
                            onMovieClick = viewModel::onMovieSelected,
                            onBack = { destination = BottomDestination.HOME },
                            onDestinationSelected = { destination = it },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieListScreen(
    movies: List<MovieItem>,
    isLoading: Boolean,
    isPagingLoading: Boolean,
    selectedTab: AppTab,
    onMovieClick: (MovieItem) -> Unit,
    onTabSelected: (AppTab) -> Unit,
    onLoadMore: () -> Unit,
    onDestinationSelected: (BottomDestination) -> Unit,
) {
    val homeFont = montserratFontFamily()
    val homeTitleFont = poppinsSemiBoldFontFamily()
    val sectionTitleFont = poppinsMediumFontFamily()
    val gridState = rememberLazyGridState()
    val tabs = listOf(
        AppTab.NOW_PLAYING,
        AppTab.UPCOMING,
        AppTab.TOP_RATED,
        AppTab.POPULAR,
    )

    LaunchedEffect(gridState, movies.size, isLoading, isPagingLoading) {
        snapshotFlow {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val nearBottom = totalItems > 0 && lastVisible >= totalItems - 6
            nearBottom && !isLoading && !isPagingLoading
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore) onLoadMore()
            }
    }

    Scaffold(
        containerColor = SCREEN_BG,
        bottomBar = {
            AppBottomNavigation(
                selected = BottomDestination.HOME,
                onSelect = onDestinationSelected,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SCREEN_BG),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                state = gridState,
                contentPadding = PaddingValues(
                    start = 25.dp,
                    end = 25.dp,
                    top = 20.dp,
                    bottom = 20.dp,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text(
                            text = "What do you want to watch?",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = homeTitleFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                            ),
                            color = Color(0xFFF4F7FD),
                            modifier = Modifier.padding(bottom = 14.dp),
                        )
                        HomeSearchField(
                            fontFamily = poppinsRegularFontFamily(),
                            onClick = { onDestinationSelected(BottomDestination.SEARCH) },
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        if (isLoading) {
                            FeaturedMoviesRowPlaceholder()
                        } else {
                            FeaturedMoviesRow(
                                movies = movies,
                                fontFamily = homeFont,
                                onMovieClick = onMovieClick,
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        CategoryTabs(
                            tabs = tabs,
                            fontFamily = sectionTitleFont,
                            selectedTab = selectedTab,
                            onTabSelected = onTabSelected,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (isLoading) {
                    items(12) {
                        PosterGridPlaceholder()
                    }
                } else {
                    items(movies.size, key = { index -> movies[index].id }) { index ->
                        PosterGridItem(
                            movie = movies[index],
                            onClick = { onMovieClick(movies[index]) },
                        )
                    }
                }
            }

            if (isPagingLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xCC151A25),
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
private fun HomeSearchField(fontFamily: FontFamily, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SEARCH_BG,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Search",
                color = Color(0xFF737C8D),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                ),
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(Res.drawable.search_textbox_icon),
                contentDescription = "Search",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun FeaturedMoviesRow(
    movies: List<MovieItem>,
    fontFamily: FontFamily,
    onMovieClick: (MovieItem) -> Unit,
) {
    val featuredMovies = movies.take(8)
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
    ) {
        itemsIndexed(featuredMovies) { index, movie ->
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(218.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .size(FEATURED_CARD_SIZE)
                        .align(Alignment.TopEnd)
                        .clickable { onMovieClick(movie) },
                ) {
                    AsyncImageWithAnimatedPlaceholder(
                        model = movie.posterUrl,
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 0.dp)
                        .offset(y = 10.dp),
                ) {
                    val number = "${index + 1}"
                    val outlineColor = Color(0xFF0296E5)
                    val coreStyle = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 96.sp,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(text = number, color = outlineColor, style = coreStyle, modifier = Modifier.offset(x = (-0.5).dp))
                    Text(text = number, color = outlineColor, style = coreStyle, modifier = Modifier.offset(x = 0.5.dp))
                    Text(text = number, color = outlineColor, style = coreStyle, modifier = Modifier.offset(y = (-0.5).dp))
                    Text(text = number, color = outlineColor, style = coreStyle, modifier = Modifier.offset(y = 0.5.dp))
                    Text(
                        text = number,
                        color = Color(0xFF242A32),
                        style = coreStyle,
                    )
                }
            }
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
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(218.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .size(FEATURED_CARD_SIZE)
                        .align(Alignment.TopEnd),
                ) {
                    AnimatedImagePlaceholder(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun CategoryTabs(
    tabs: List<AppTab>,
    fontFamily: FontFamily,
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        tabs.forEach { tab ->
            val density = LocalDensity.current
            var indicatorWidth by remember(tab) { mutableStateOf(0.dp) }
            val selected = selectedTab == tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(tab) },
            ) {
                Text(
                    text = tab.label,
                    color = if (selected) Color.White else Color(0xFFA9B3C1),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    ),
                    onTextLayout = { layoutResult ->
                        indicatorWidth = with(density) { layoutResult.size.width.toDp() }
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(indicatorWidth)
                        .height(4.dp)
                        .background(if (selected) Color(0xFF11A6FF) else Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun PosterGridItem(movie: MovieItem, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(GRID_POSTER_HEIGHT)
            .clickable(onClick = onClick),
    ) {
        AsyncImageWithAnimatedPlaceholder(
            model = movie.posterUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(GRID_POSTER_HEIGHT)
                .clip(RoundedCornerShape(14.dp)),
        )
    }
}

@Composable
private fun PosterGridPlaceholder() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(GRID_POSTER_HEIGHT),
    ) {
        AnimatedImagePlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(GRID_POSTER_HEIGHT)
                .clip(RoundedCornerShape(14.dp)),
        )
    }
}

@Composable
private fun SearchScreen(
    query: String,
    results: List<MovieItem>,
    isLoading: Boolean,
    hasSearchAttempted: Boolean,
    onQueryChanged: (String) -> Unit,
    onMovieClick: (MovieItem) -> Unit,
    onBack: () -> Unit,
    onDestinationSelected: (BottomDestination) -> Unit,
) {
    val searchTitleFont = montserratSemiBoldFontFamily()
    Scaffold(
        containerColor = SCREEN_BG,
        bottomBar = {
            AppBottomNavigation(
                selected = BottomDestination.SEARCH,
                onSelect = onDestinationSelected,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SCREEN_BG)
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
                    tint = Color(0xFFD5DAE3),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(onClick = onBack),
                )
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Search",
                        color = Color(0xFFEAF0FA),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = searchTitleFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        ),
                    )
                }
                Spacer(modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))

            SearchInputField(
                value = query,
                onValueChange = onQueryChanged,
            )

            Spacer(modifier = Modifier.height(18.dp))
            val contentState = when {
                query.isBlank() -> SearchContentState.BLANK
                isLoading -> SearchContentState.LOADING
                hasSearchAttempted && results.isEmpty() -> SearchContentState.EMPTY
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
                            items(5) {
                                SearchResultItemPlaceholder()
                            }
                        }
                    }

                    SearchContentState.EMPTY -> {
                        SearchNotFoundState()
                    }

                    SearchContentState.BLANK -> {
                        Box(modifier = Modifier.fillMaxSize())
                    }

                    SearchContentState.RESULTS -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                        ) {
                            items(results, key = { movie -> movie.id }) { movie ->
                                SearchResultItem(
                                    movie = movie,
                                    onClick = { onMovieClick(movie) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchInputField(value: String, onValueChange: (String) -> Unit) {
    val inputFont = poppinsRegularFontFamily()
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = inputFont,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
        ),
        placeholder = {
            Text(
                text = "Search",
                color = Color(0xFF8A94A6),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = inputFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                ),
            )
        },
        trailingIcon = {
            Icon(
                painter = painterResource(Res.drawable.search_textbox_icon),
                contentDescription = "Search",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp),
            )
        },
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SEARCH_BG,
            unfocusedContainerColor = SEARCH_BG,
            disabledContainerColor = SEARCH_BG,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = Color(0xFFEAF0FA),
            unfocusedTextColor = Color(0xFFEAF0FA),
            cursorColor = Color(0xFF11A6FF),
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SearchResultItem(movie: MovieItem, onClick: () -> Unit) {
    val poppinsRegular = poppinsRegularFontFamily()
    val montserratSemiBold = montserratSemiBoldFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        AsyncImageWithAnimatedPlaceholder(
            model = movie.posterUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .width(92.dp)
                .height(126.dp)
                .clip(RoundedCornerShape(12.dp)),
        )

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = movie.title,
                color = Color(0xFFEAF0FA),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = poppinsRegular,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(210.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            SearchMetaRow(
                icon = Icons.Outlined.StarBorder,
                text = movie.rating.toOneDecimalString(),
                tint = Color(0xFFFFA726),
                textColor = Color(0xFFFFA726),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = montserratSemiBold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                ),
            )
            SearchMetaRowDrawable(
                icon = Res.drawable.detail_ticket_icon,
                text = movie.primaryGenreLabel(),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = poppinsRegular,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                ),
            )
            SearchMetaRowDrawable(
                icon = Res.drawable.detail_calendar_icon,
                text = movie.releaseYear(),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = poppinsRegular,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                ),
            )
            SearchMetaRowDrawable(
                icon = Res.drawable.detail_clock_icon,
                text = "139 minutes",
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = poppinsRegular,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                ),
            )
        }
    }
}

@Composable
private fun SearchResultItemPlaceholder() {
    Row(modifier = Modifier.fillMaxWidth()) {
        AnimatedImagePlaceholder(
            modifier = Modifier
                .width(92.dp)
                .height(126.dp)
                .clip(RoundedCornerShape(12.dp)),
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            AnimatedImagePlaceholder(
                modifier = Modifier
                    .width(210.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedImagePlaceholder(
                modifier = Modifier
                    .width(72.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedImagePlaceholder(
                modifier = Modifier
                    .width(96.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedImagePlaceholder(
                modifier = Modifier
                    .width(88.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedImagePlaceholder(
                modifier = Modifier
                    .width(104.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )
        }
    }
}

@Composable
private fun SearchMetaRow(
    icon: ImageVector,
    text: String,
    tint: Color = Color(0xFF9AA5B7),
    textColor: Color = Color(0xFFBAC3D2),
    textStyle: TextStyle? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = textColor,
            style = textStyle ?: MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SearchMetaRowDrawable(
    icon: DrawableResource,
    text: String,
    textColor: Color = Color(0xFFBAC3D2),
    textStyle: TextStyle? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = textColor,
            style = textStyle ?: MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SearchNotFoundState() {
    val titleFont = montserratSemiBoldFontFamily()
    val subtitleFont = montserratMediumFontFamily()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.no_results),
            contentDescription = "No movie found",
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "We Are Sorry, We Can\nNot Find The Movie :(",
            color = Color(0xFFEBEBEF),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = titleFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 25.6.sp,
            ),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Find your movie by Type title,\ncategories, years, etc",
            color = Color(0xFF92929D),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = subtitleFont,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WatchListScreen(
    watchList: List<MovieItem>,
    onMovieClick: (MovieItem) -> Unit,
    onBack: () -> Unit,
    onDestinationSelected: (BottomDestination) -> Unit,
) {
    val watchListTitleFont = montserratSemiBoldFontFamily()
    Scaffold(
        containerColor = SCREEN_BG,
        bottomBar = {
            AppBottomNavigation(
                selected = BottomDestination.WATCH_LIST,
                onSelect = onDestinationSelected,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SCREEN_BG)
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
                    tint = Color(0xFFD5DAE3),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(onClick = onBack),
                )
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Watch list",
                        color = Color(0xFFEAF0FA),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = watchListTitleFont,
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
                    WatchListEmptyState()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        items(watchList, key = { movie -> movie.id }) { movie ->
                            WatchListMovieItem(
                                movie = movie,
                                onClick = { onMovieClick(movie) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchListMovieItem(movie: MovieItem, onClick: () -> Unit) {
    SearchResultItem(movie = movie, onClick = onClick)
}

@Composable
private fun WatchListEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(Res.drawable.watch_list_empty),
                contentDescription = "No watch list movies",
                modifier = Modifier.size(92.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "There Is No Movie Yet!",
                color = Color(0xFFDDE3EE),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Find your movie by Type title,\ncategories, years, etc",
                color = Color(0xFF8E99AC),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun AppBottomNavigation(
    selected: BottomDestination,
    onSelect: (BottomDestination) -> Unit,
) {
    val tabFont = robotoMediumFontFamily()
    Surface(color = Color(0xFF151B27), modifier = Modifier.fillMaxWidth()) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF14476F)),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomNavItem(
                    iconRes = Res.drawable.home_icon,
                    label = "Home",
                    fontFamily = tabFont,
                    modifier = Modifier.weight(1f),
                    selected = selected == BottomDestination.HOME,
                    onClick = { onSelect(BottomDestination.HOME) },
                )
                BottomNavItem(
                    iconRes = Res.drawable.search_icon,
                    label = "Search",
                    fontFamily = tabFont,
                    modifier = Modifier.weight(1f),
                    selected = selected == BottomDestination.SEARCH,
                    onClick = { onSelect(BottomDestination.SEARCH) },
                )
                BottomNavItem(
                    iconRes = Res.drawable.save_icon,
                    label = "Watch list",
                    fontFamily = tabFont,
                    modifier = Modifier.weight(1f),
                    selected = selected == BottomDestination.WATCH_LIST,
                    onClick = { onSelect(BottomDestination.WATCH_LIST) },
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    iconRes: DrawableResource,
    label: String,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (selected) Color(0xFF10A8FF) else Color(0xFF808B9B)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = tint,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            ),
        )
    }
}

private enum class DetailSection(val label: String) {
    ABOUT("About Movie"),
    REVIEWS("Reviews"),
    TRAILERS("Trailer"),
    IMAGES("Images"),
    SIMILAR("Similar"),
    CAST("Cast"),
}

@Composable
private fun MovieDetailScreen(
    movie: MovieDetailItem,
    isLoading: Boolean,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onMovieClick: (MovieItem) -> Unit,
    onOpenLink: (String) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onBack: () -> Unit,
) {
    val poppinsSemiBold = poppinsSemiBoldFontFamily()
    val poppinsMedium = poppinsMediumFontFamily()
    val poppinsRegular = poppinsRegularFontFamily()
    var section by remember(movie.id) { mutableStateOf(DetailSection.ABOUT) }
    val overlayPosterHeight = 126.dp
    val overlayPosterOverlap = overlayPosterHeight / 2
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SCREEN_BG)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 44.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFFDEE5F2),
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onBack),
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = "Detail",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFF1F5FF),
                )
            }
            Icon(
                painter = painterResource(Res.drawable.save_icon_detail),
                contentDescription = "Bookmark",
                tint = if (isBookmarked) Color(0xFF10A8FF) else Color(0xFFC8CEDA),
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onToggleBookmark),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(246.dp),
        ) {
            AsyncImageWithAnimatedPlaceholder(
                model = movie.backdropUrl ?: movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 14.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                AsyncImageWithAnimatedPlaceholder(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .size(width = 88.dp, height = overlayPosterHeight)
                        .offset(y = overlayPosterOverlap)
                        .clip(RoundedCornerShape(12.dp)),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, bottom = 6.dp),
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = poppinsSemiBold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                        ),
                        color = Color(0xFFF4F8FF),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x99232D3B),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.StarBorder,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFA726),
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = movie.voteAverage.toOneDecimalString(),
                            color = Color(0xFFFFA726),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(overlayPosterOverlap))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        ) {
            DetailMetaItem(text = movie.releaseDate.take(4).ifBlank { "-" }, icon = Res.drawable.detail_calendar_icon)
            DetailMetaItem(
                text = "${if (movie.runtime > 0) movie.runtime else 148} Minutes",
                icon = Res.drawable.detail_clock_icon,
            )
            DetailMetaItem(
                text = movie.genres.firstOrNull().orEmpty().ifBlank { "Action" },
                icon = Res.drawable.detail_ticket_icon,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            DetailSection.entries.forEach { tab ->
                val density = LocalDensity.current
                var indicatorWidth by remember(tab) { mutableStateOf(0.dp) }
                val selected = section == tab
                Column(
                    modifier = Modifier.clickable { section = tab },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = tab.label,
                        color = if (selected) Color(0xFFF3F7FF) else Color(0xFFAFB8C9),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = poppinsMedium,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                        ),
                        onTextLayout = { layoutResult ->
                            indicatorWidth = with(density) { layoutResult.size.width.toDp() }
                        },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(indicatorWidth)
                            .height(4.dp)
                            .background(if (selected) Color(0xFF4A5568) else Color.Transparent),
                    )
                }
            }
        }

        when (section) {
            DetailSection.ABOUT -> {
                if (isLoading) {
                    Text(
                        text = "Loading details...",
                        color = Color(0xFFAEB8C9),
                        modifier = Modifier.padding(horizontal = 22.dp),
                    )
                } else {
                    Text(
                        text = movie.overview.ifBlank { "No synopsis available." },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = poppinsRegular,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                        ),
                        color = Color(0xFFD7DEEB),
                        modifier = Modifier.padding(horizontal = 22.dp),
                    )
                }
            }

            DetailSection.REVIEWS -> {
                if (isLoading) {
                    Text(
                        text = "Loading details...",
                        color = Color(0xFFAEB8C9),
                        modifier = Modifier.padding(horizontal = 22.dp),
                    )
                } else {
                    val reviews = movie.reviews.take(5)
                    if (reviews.isEmpty()) {
                        Text(
                            text = "No reviews available.",
                            color = Color(0xFFAFB8C9),
                            modifier = Modifier.padding(horizontal = 22.dp),
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(horizontal = 22.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            reviews.forEach { review ->
                                Row {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        if (review.avatarUrl.isNullOrBlank()) {
                                            Image(
                                                painter = painterResource(Res.drawable.review_avatar_default),
                                                contentDescription = review.author,
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape),
                                            )
                                        } else {
                                            AsyncImageWithAnimatedPlaceholder(
                                                model = review.avatarUrl,
                                                contentDescription = review.author,
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape),
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = (review.rating ?: 6.3).toOneDecimalString(),
                                            color = Color(0xFF12A3FF),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                    Column(modifier = Modifier.padding(start = 10.dp)) {
                                        Text(
                                            text = review.author,
                                            color = Color(0xFFF2F6FF),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = poppinsMedium,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp,
                                            ),
                                        )
                                        Text(
                                            text = review.content,
                                            color = Color(0xFFD2D9E7),
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = poppinsRegular,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 12.sp,
                                            ),
                                            maxLines = 4,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }    
                        }
                    }
                }
            }

            DetailSection.TRAILERS -> {
                if (isLoading) {
                    Text(
                        text = "Loading details...",
                        color = Color(0xFFAEB8C9),
                        modifier = Modifier.padding(horizontal = 22.dp),
                    )
                } else {
                    val trailers = movie.trailers.take(6)
                    if (trailers.isEmpty()) {
                        Text(
                            text = "No trailers available.",
                            color = Color(0xFFAFB8C9),
                            modifier = Modifier.padding(horizontal = 22.dp),
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            trailers.forEach { trailer ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF222B3B),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onOpenLink(trailer.watchUrl)
                                        },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        AsyncImageWithAnimatedPlaceholder(
                                            model = trailer.thumbnailUrl ?: movie.backdropUrl ?: movie.posterUrl,
                                            contentDescription = trailer.name,
                                            modifier = Modifier
                                                .width(120.dp)
                                                .height(68.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                        )
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 10.dp),
                                        ) {
                                            Text(
                                                text = trailer.name,
                                                color = Color(0xFFE8EEFA),
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontFamily = poppinsMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 12.sp,
                                                ),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = trailer.type.ifBlank { "Trailer" },
                                                color = Color(0xFF9BA7BB),
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = poppinsRegular,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 12.sp,
                                                ),
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Filled.LocalMovies,
                                            contentDescription = "Open trailer",
                                            tint = Color(0xFF10A8FF),
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }    
                        }
                    }
                }
            }

            DetailSection.IMAGES -> {
                if (isLoading) {
                    Text(
                        text = "Loading details...",
                        color = Color(0xFFAEB8C9),
                        modifier = Modifier.padding(horizontal = 22.dp),
                    )
                } else {
                    val images = movie.images
                    if (images.isEmpty()) {
                        Text(
                            text = "No images available.",
                            color = Color(0xFFAFB8C9),
                            modifier = Modifier.padding(horizontal = 22.dp),
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            images.chunked(2).forEachIndexed { rowIndex, rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    rowItems.forEachIndexed { columnIndex, imageUrl ->
                                        val absoluteIndex = rowIndex * 2 + columnIndex
                                        AsyncImageWithAnimatedPlaceholder(
                                            model = imageUrl,
                                            contentDescription = "${movie.title} image",
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(110.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    onImageClick(images, absoluteIndex)
                                                },
                                        )
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(110.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            DetailSection.SIMILAR -> {
                val similar = movie.similarMovies.take(8)
                if (similar.isEmpty()) {
                    Text(
                        text = "No similar movies available.",
                        color = Color(0xFFAFB8C9),
                        modifier = Modifier.padding(horizontal = 22.dp),
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        similar.forEach { item ->
                            SearchResultItem(
                                movie = item,
                                onClick = { onMovieClick(item) },
                            )
                        }
                    }
                }
            }

            DetailSection.CAST -> {
                val cast = movie.cast.take(6)
                if (cast.isEmpty()) {
                    Text(
                        text = "No cast available.",
                        color = Color(0xFFAFB8C9),
                        modifier = Modifier.padding(horizontal = 22.dp),
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        cast.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                rowItems.forEach { castItem ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(120.dp),
                                    ) {
                                        AsyncImageWithAnimatedPlaceholder(
                                            model = castItem.profileUrl,
                                            contentDescription = castItem.name,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(CircleShape),
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = castItem.name,
                                            color = Color(0xFFE8EEFA),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = poppinsMedium,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp,
                                            ),
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun ImageViewerScreen(
    images: List<String>,
    initialIndex: Int,
    onBack: () -> Unit,
) {
    if (images.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No images available.",
                color = Color(0xFFAFB8C9),
            )
        }
        return
    }

    val startPage = remember(images.size, initialIndex) {
        val size = images.size.coerceAtLeast(1)
        val base = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % size)
        base + initialIndex.coerceIn(0, size - 1)
    }
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { Int.MAX_VALUE },
    )
    val currentPosition = ((pagerState.currentPage % images.size) + images.size) % images.size
    val currentImage = images[currentPosition]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AsyncImage(
            model = currentImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(24.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x66000000)),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 44.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFDEE5F2),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onBack),
                )
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "${currentPosition + 1}/${images.size}",
                        color = Color(0xFFF1F5FF),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Spacer(modifier = Modifier.size(24.dp))
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val imageIndex = ((page % images.size) + images.size) % images.size
                AsyncImage(
                    model = images[imageIndex],
                    contentDescription = "Movie image ${imageIndex + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun InAppBrowserScreen(
    url: String,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SCREEN_BG),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 44.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFFDEE5F2),
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onBack),
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = "Trailer",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFF1F5FF),
                )
            }
            Spacer(modifier = Modifier.size(24.dp))
        }
        InAppWebView(
            url = url,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun DetailMetaItem(text: String, icon: DrawableResource) {
    val montserratMedium = montserratMediumFontFamily()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color(0xFF8E99AC),
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = text,
            color = Color(0xFF8E99AC),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = montserratMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            ),
        )
    }
}
