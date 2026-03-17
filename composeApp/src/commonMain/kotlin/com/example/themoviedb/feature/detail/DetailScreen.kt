package com.example.themoviedb.feature.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.themoviedb.core.data.mapper.toOneDecimalString
import com.example.themoviedb.core.data.model.MovieDetailItem
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.ui.components.AsyncImageWithPlaceholder
import com.example.themoviedb.core.ui.components.DetailMetaItem
import com.example.themoviedb.core.ui.components.MovieListItem
import com.example.themoviedb.core.ui.components.RatingBadge
import com.example.themoviedb.core.ui.theme.AppColors
import com.example.themoviedb.core.ui.theme.AppTypography
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import themoviedb.composeapp.generated.resources.Res
import themoviedb.composeapp.generated.resources.detail_calendar_icon
import themoviedb.composeapp.generated.resources.detail_clock_icon
import themoviedb.composeapp.generated.resources.detail_ticket_icon
import themoviedb.composeapp.generated.resources.review_avatar_default
import themoviedb.composeapp.generated.resources.save_icon_detail

private enum class DetailSection(val label: String) {
    ABOUT("About Movie"),
    REVIEWS("Reviews"),
    TRAILERS("Trailer"),
    IMAGES("Images"),
    SIMILAR("Similar"),
    CAST("Cast"),
}

@Composable
fun DetailScreen(
    movieId: Int,
    onMovieClick: (MovieItem) -> Unit,
    onOpenLink: (String) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: DetailViewModel = koinViewModel { parametersOf(movieId) },
) {
    val uiState by viewModel.uiState.collectAsState()
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    val movie = uiState.detailMovie

    if (movie == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.ScreenBackground),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Loading details...", color = AppColors.DetailLoadingText)
        }
        return
    }

    DetailContent(
        movie = movie,
        isLoading = uiState.isLoading,
        isBookmarked = isBookmarked,
        onToggleBookmark = viewModel::toggleBookmark,
        onMovieClick = onMovieClick,
        onOpenLink = onOpenLink,
        onImageClick = onImageClick,
        onBack = onBack,
    )
}

@Composable
private fun DetailContent(
    movie: MovieDetailItem,
    isLoading: Boolean,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onMovieClick: (MovieItem) -> Unit,
    onOpenLink: (String) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onBack: () -> Unit,
) {
    val poppinsSemiBold = AppTypography.poppinsSemiBold()
    val poppinsMedium = AppTypography.poppinsMedium()
    val poppinsRegular = AppTypography.poppinsRegular()
    var section by remember(movie.id) { mutableStateOf(DetailSection.ABOUT) }
    val overlayPosterHeight = 126.dp
    val overlayPosterOverlap = overlayPosterHeight / 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 44.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = AppColors.DetailBackButton,
                modifier = Modifier.size(24.dp).clickable(onClick = onBack),
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = "Detail",
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.DetailTitle,
                )
            }
            Icon(
                painter = painterResource(Res.drawable.save_icon_detail),
                contentDescription = "Bookmark",
                tint = if (isBookmarked) AppColors.AccentBlue else Color(0xFFC8CEDA),
                modifier = Modifier.size(24.dp).clickable(onClick = onToggleBookmark),
            )
        }

        // Backdrop + poster overlay
        Box(modifier = Modifier.fillMaxWidth().height(246.dp)) {
            AsyncImageWithPlaceholder(
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
                AsyncImageWithPlaceholder(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .size(width = 88.dp, height = overlayPosterHeight)
                        .offset(y = overlayPosterOverlap)
                        .clip(RoundedCornerShape(12.dp)),
                )
                Column(
                    modifier = Modifier.weight(1f).padding(start = 12.dp, bottom = 6.dp),
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = poppinsSemiBold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                        ),
                        color = AppColors.DetailMovieTitle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                RatingBadge(rating = movie.voteAverage)
            }
        }
        Spacer(modifier = Modifier.height(overlayPosterOverlap))

        // Meta info row
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

        // Section tabs
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
                        color = if (selected) AppColors.DetailSectionActive else AppColors.DetailSectionInactive,
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
                            .background(if (selected) AppColors.DetailTabIndicator else Color.Transparent),
                    )
                }
            }
        }

        // Section content
        when (section) {
            DetailSection.ABOUT -> AboutSection(movie = movie, isLoading = isLoading, fontFamily = poppinsRegular)
            DetailSection.REVIEWS -> ReviewsSection(movie = movie, isLoading = isLoading, poppinsMedium = poppinsMedium, poppinsRegular = poppinsRegular)
            DetailSection.TRAILERS -> TrailersSection(movie = movie, isLoading = isLoading, poppinsMedium = poppinsMedium, poppinsRegular = poppinsRegular, onOpenLink = onOpenLink)
            DetailSection.IMAGES -> ImagesSection(movie = movie, isLoading = isLoading, onImageClick = onImageClick)
            DetailSection.SIMILAR -> SimilarSection(movie = movie, onMovieClick = onMovieClick)
            DetailSection.CAST -> CastSection(movie = movie, poppinsMedium = poppinsMedium)
        }
    }
}

@Composable
private fun AboutSection(
    movie: MovieDetailItem,
    isLoading: Boolean,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
) {
    if (isLoading) {
        Text(text = "Loading details...", color = AppColors.DetailLoadingText, modifier = Modifier.padding(horizontal = 22.dp))
    } else {
        Text(
            text = movie.overview.ifBlank { "No synopsis available." },
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
            ),
            color = AppColors.TextTertiary,
            modifier = Modifier.padding(horizontal = 22.dp),
        )
    }
}

@Composable
private fun ReviewsSection(
    movie: MovieDetailItem,
    isLoading: Boolean,
    poppinsMedium: androidx.compose.ui.text.font.FontFamily,
    poppinsRegular: androidx.compose.ui.text.font.FontFamily,
) {
    if (isLoading) {
        Text(text = "Loading details...", color = AppColors.DetailLoadingText, modifier = Modifier.padding(horizontal = 22.dp))
        return
    }
    val reviews = movie.reviews.take(5)
    if (reviews.isEmpty()) {
        Text(text = "No reviews available.", color = AppColors.DetailSectionInactive, modifier = Modifier.padding(horizontal = 22.dp))
        return
    }
    Column(modifier = Modifier.padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        reviews.forEach { review ->
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (review.avatarUrl.isNullOrBlank()) {
                        Image(
                            painter = painterResource(Res.drawable.review_avatar_default),
                            contentDescription = review.author,
                            modifier = Modifier.size(44.dp).clip(CircleShape),
                        )
                    } else {
                        AsyncImageWithPlaceholder(
                            model = review.avatarUrl,
                            contentDescription = review.author,
                            modifier = Modifier.size(44.dp).clip(CircleShape),
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = (review.rating ?: 6.3).toOneDecimalString(),
                        color = AppColors.ReviewRating,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(
                        text = review.author,
                        color = AppColors.DetailReviewAuthor,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = poppinsMedium,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                        ),
                    )
                    Text(
                        text = review.content,
                        color = AppColors.DetailReviewContent,
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

@Composable
private fun TrailersSection(
    movie: MovieDetailItem,
    isLoading: Boolean,
    poppinsMedium: androidx.compose.ui.text.font.FontFamily,
    poppinsRegular: androidx.compose.ui.text.font.FontFamily,
    onOpenLink: (String) -> Unit,
) {
    if (isLoading) {
        Text(text = "Loading details...", color = AppColors.DetailLoadingText, modifier = Modifier.padding(horizontal = 22.dp))
        return
    }
    val trailers = movie.trailers.take(6)
    if (trailers.isEmpty()) {
        Text(text = "No trailers available.", color = AppColors.DetailSectionInactive, modifier = Modifier.padding(horizontal = 22.dp))
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        trailers.forEach { trailer ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AppColors.CardBackground,
                modifier = Modifier.fillMaxWidth().clickable { onOpenLink(trailer.watchUrl) },
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImageWithPlaceholder(
                        model = trailer.thumbnailUrl ?: movie.backdropUrl ?: movie.posterUrl,
                        contentDescription = trailer.name,
                        modifier = Modifier.width(120.dp).height(68.dp).clip(RoundedCornerShape(8.dp)),
                    )
                    Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(
                            text = trailer.name,
                            color = AppColors.DetailTrailerName,
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
                            color = AppColors.DetailTrailerType,
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
                        tint = AppColors.AccentBlue,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ImagesSection(
    movie: MovieDetailItem,
    isLoading: Boolean,
    onImageClick: (List<String>, Int) -> Unit,
) {
    if (isLoading) {
        Text(text = "Loading details...", color = AppColors.DetailLoadingText, modifier = Modifier.padding(horizontal = 22.dp))
        return
    }
    val images = movie.images
    if (images.isEmpty()) {
        Text(text = "No images available.", color = AppColors.DetailSectionInactive, modifier = Modifier.padding(horizontal = 22.dp))
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        images.chunked(2).forEachIndexed { rowIndex, rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEachIndexed { columnIndex, imageUrl ->
                    val absoluteIndex = rowIndex * 2 + columnIndex
                    AsyncImageWithPlaceholder(
                        model = imageUrl,
                        contentDescription = "${movie.title} image",
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onImageClick(images, absoluteIndex) },
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f).height(110.dp))
                }
            }
        }
    }
}

@Composable
private fun SimilarSection(
    movie: MovieDetailItem,
    onMovieClick: (MovieItem) -> Unit,
) {
    val similar = movie.similarMovies.take(8)
    if (similar.isEmpty()) {
        Text(text = "No similar movies available.", color = AppColors.DetailSectionInactive, modifier = Modifier.padding(horizontal = 22.dp))
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        similar.forEach { item ->
            MovieListItem(movie = item, onClick = { onMovieClick(item) })
        }
    }
}

@Composable
private fun CastSection(
    movie: MovieDetailItem,
    poppinsMedium: androidx.compose.ui.text.font.FontFamily,
) {
    val cast = movie.cast.take(6)
    if (cast.isEmpty()) {
        Text(text = "No cast available.", color = AppColors.DetailSectionInactive, modifier = Modifier.padding(horizontal = 22.dp))
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        cast.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                rowItems.forEach { castItem ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(120.dp)) {
                        AsyncImageWithPlaceholder(
                            model = castItem.profileUrl,
                            contentDescription = castItem.name,
                            modifier = Modifier.size(100.dp).clip(CircleShape),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = castItem.name,
                            color = AppColors.DetailCastName,
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
