package com.example.themoviedb.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.example.themoviedb.core.ui.theme.AppColors

@Composable
fun ShimmerPlaceholder(modifier: Modifier = Modifier) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1300, easing = LinearEasing)),
        label = "shimmer_progress",
    )
    val height = size.height.toFloat().coerceAtLeast(1f)
    val startY = (progress * 2f - 1f) * height
    val endY = startY + height
    val brush = Brush.verticalGradient(
        colors = listOf(
            AppColors.ShimmerGradientStart,
            AppColors.ShimmerGradientCenter,
            AppColors.ShimmerGradientStart,
        ),
        startY = startY,
        endY = endY,
    )

    Box(
        modifier = modifier
            .background(AppColors.ShimmerBase)
            .onSizeChanged { size = it },
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush))
    }
}
