package com.example.themoviedb.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.themoviedb.core.data.mapper.primaryGenreLabel
import com.example.themoviedb.core.data.mapper.releaseYear
import com.example.themoviedb.core.data.mapper.toOneDecimalString
import com.example.themoviedb.core.data.model.MovieItem
import com.example.themoviedb.core.ui.theme.AppColors
import com.example.themoviedb.core.ui.theme.AppTypography
import themoviedb.composeapp.generated.resources.Res
import themoviedb.composeapp.generated.resources.detail_calendar_icon
import themoviedb.composeapp.generated.resources.detail_clock_icon
import themoviedb.composeapp.generated.resources.detail_ticket_icon

@Composable
fun MovieListItem(
    movie: MovieItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val poppinsRegular = AppTypography.poppinsRegular()
    val montserratSemiBold = AppTypography.montserratSemiBold()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        AsyncImageWithPlaceholder(
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
                color = AppColors.TextSecondary,
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
            MetaInfoRow(
                icon = Icons.Outlined.StarBorder,
                text = movie.rating.toOneDecimalString(),
                tint = AppColors.AccentOrange,
                textColor = AppColors.AccentOrange,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = montserratSemiBold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                ),
            )
            MetaInfoRowDrawable(
                icon = Res.drawable.detail_ticket_icon,
                text = movie.primaryGenreLabel(),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = poppinsRegular,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                ),
            )
            MetaInfoRowDrawable(
                icon = Res.drawable.detail_calendar_icon,
                text = movie.releaseYear(),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = poppinsRegular,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                ),
            )
            MetaInfoRowDrawable(
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
