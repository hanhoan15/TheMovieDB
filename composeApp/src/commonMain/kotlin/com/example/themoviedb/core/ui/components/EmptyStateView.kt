package com.example.themoviedb.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.themoviedb.core.ui.theme.AppColors
import com.example.themoviedb.core.ui.theme.AppTypography
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun EmptyStateView(
    imageRes: DrawableResource,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    titleColor: Color = AppColors.EmptyTitle,
    subtitleColor: Color = AppColors.EmptySubtitle,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = title,
                modifier = Modifier.size(96.dp),
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = AppTypography.montserratSemiBold(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 25.6.sp,
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = subtitle,
                color = subtitleColor,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = AppTypography.montserratMedium(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}
