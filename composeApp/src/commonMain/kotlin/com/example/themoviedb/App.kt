package com.example.themoviedb

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.themoviedb.core.navigation.AppNavGraph
import com.example.themoviedb.core.ui.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        AppNavGraph()
    }
}
