package com.example.themoviedb

import androidx.compose.ui.window.ComposeUIViewController
import com.example.themoviedb.core.di.appModules
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}

private fun initKoin() {
    if (runCatching { KoinPlatform.getKoin() }.isFailure) {
        startKoin {
            modules(appModules)
        }
    }
}
