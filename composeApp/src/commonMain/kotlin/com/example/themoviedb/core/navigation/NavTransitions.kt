package com.example.themoviedb.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween

object NavTransitions {
    fun slideInForward(): EnterTransition =
        slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth / 4 },
            animationSpec = tween(280),
        ) + fadeIn(animationSpec = tween(280))

    fun slideOutForward(): ExitTransition =
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth / 6 },
            animationSpec = tween(220),
        ) + fadeOut(animationSpec = tween(220))

    fun slideInBack(): EnterTransition =
        slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth / 4 },
            animationSpec = tween(280),
        ) + fadeIn(animationSpec = tween(280))

    fun slideOutBack(): ExitTransition =
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth / 4 },
            animationSpec = tween(220),
        ) + fadeOut(animationSpec = tween(220))

    fun crossfadeIn(): EnterTransition =
        fadeIn(animationSpec = tween(280))

    fun crossfadeOut(): ExitTransition =
        fadeOut(animationSpec = tween(200))
}
