package com.example.themoviedb.feature.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.themoviedb.InAppWebView
import com.example.themoviedb.core.ui.components.ScreenTopBar
import com.example.themoviedb.core.ui.theme.AppColors

@Composable
fun WebViewScreen(
    url: String,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground),
    ) {
        ScreenTopBar(title = "Trailer", onBack = onBack)
        InAppWebView(
            url = url,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
