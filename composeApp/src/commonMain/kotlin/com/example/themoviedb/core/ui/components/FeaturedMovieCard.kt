package com.example.themoviedb.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.ui.theme.AppColors
import com.example.themoviedb.core.ui.theme.AppTypography
import com.example.themoviedb.core.ui.theme.Dimensions

@Composable
fun FeaturedMovieCard(
    movie: MovieItem,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fontFamily = AppTypography.montserrat()
    Box(
        modifier = modifier
            .width(150.dp)
            .height(218.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .size(Dimensions.FeaturedCardSize)
                .align(Alignment.TopEnd)
                .clickable(onClick = onClick),
        ) {
            AsyncImageWithPlaceholder(
                model = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp)
                .offset(y = 10.dp),
        ) {
            val number = "${index + 1}"
            val coreStyle = MaterialTheme.typography.displayLarge.copy(
                fontSize = 96.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = number, color = AppColors.OutlineBlue, style = coreStyle, modifier = Modifier.offset(x = (-0.5).dp))
            Text(text = number, color = AppColors.OutlineBlue, style = coreStyle, modifier = Modifier.offset(x = 0.5.dp))
            Text(text = number, color = AppColors.OutlineBlue, style = coreStyle, modifier = Modifier.offset(y = (-0.5).dp))
            Text(text = number, color = AppColors.OutlineBlue, style = coreStyle, modifier = Modifier.offset(y = 0.5.dp))
            Text(
                text = number,
                color = AppColors.NumberFill,
                style = coreStyle,
            )
        }
    }
}
