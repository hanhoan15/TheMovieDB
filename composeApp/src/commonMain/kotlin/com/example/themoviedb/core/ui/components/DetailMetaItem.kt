package com.example.themoviedb.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.themoviedb.core.ui.theme.AppColors
import com.example.themoviedb.core.ui.theme.AppTypography
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun DetailMetaItem(
    text: String,
    icon: DrawableResource,
    modifier: Modifier = Modifier,
) {
    val fontFamily = AppTypography.montserratMedium()
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = AppColors.DetailMetaText,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = text,
            color = AppColors.DetailMetaText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            ),
        )
    }
}
