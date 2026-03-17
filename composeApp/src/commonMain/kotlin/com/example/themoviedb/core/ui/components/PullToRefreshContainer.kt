package com.example.themoviedb.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PullToRefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // Basic wrapper - pull-to-refresh will be enhanced in Phase 7
    Box(modifier = modifier) {
        content()
    }
}
