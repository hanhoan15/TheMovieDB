package com.example.themoviedb.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import themoviedb.composeapp.generated.resources.Res
import themoviedb.composeapp.generated.resources.montserrat_medium
import themoviedb.composeapp.generated.resources.montserrat_regular
import themoviedb.composeapp.generated.resources.montserrat_semibold
import themoviedb.composeapp.generated.resources.poppins_medium
import themoviedb.composeapp.generated.resources.poppins_regular
import themoviedb.composeapp.generated.resources.poppins_semibold
import themoviedb.composeapp.generated.resources.roboto_medium
import themoviedb.composeapp.generated.resources.roboto_regular

object AppTypography {
    @Composable
    fun montserrat(): FontFamily = FontFamily(Font(Res.font.montserrat_regular))

    @Composable
    fun montserratMedium(): FontFamily = FontFamily(Font(Res.font.montserrat_medium))

    @Composable
    fun montserratSemiBold(): FontFamily = FontFamily(Font(Res.font.montserrat_semibold))

    @Composable
    fun poppinsRegular(): FontFamily = FontFamily(Font(Res.font.poppins_regular))

    @Composable
    fun poppinsMedium(): FontFamily = FontFamily(Font(Res.font.poppins_medium))

    @Composable
    fun poppinsSemiBold(): FontFamily = FontFamily(Font(Res.font.poppins_semibold))

    @Composable
    fun robotoMedium(): FontFamily = FontFamily(Font(Res.font.roboto_medium))

    @Composable
    fun robotoRegular(): FontFamily = FontFamily(Font(Res.font.roboto_regular))
}
