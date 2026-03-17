package com.example.themoviedb

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun InAppWebView(
    url: String,
    modifier: Modifier = Modifier,
)
