package com.example.themoviedb.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import themoviedb.composeapp.generated.resources.Res
import themoviedb.composeapp.generated.resources.home_icon
import themoviedb.composeapp.generated.resources.save_icon
import themoviedb.composeapp.generated.resources.search_icon

enum class BottomDestination {
    HOME,
    SEARCH,
    WATCH_LIST,
}

@Composable
fun AppBottomNavBar(
    selected: BottomDestination,
    onSelect: (BottomDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabFont = AppTypography.robotoMedium()
    Surface(color = AppColors.BottomNavBackground, modifier = modifier.fillMaxWidth()) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppColors.BottomNavDivider),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomNavItem(
                    iconRes = Res.drawable.home_icon,
                    label = "Home",
                    fontFamily = tabFont,
                    modifier = Modifier.weight(1f),
                    selected = selected == BottomDestination.HOME,
                    onClick = { onSelect(BottomDestination.HOME) },
                )
                BottomNavItem(
                    iconRes = Res.drawable.search_icon,
                    label = "Search",
                    fontFamily = tabFont,
                    modifier = Modifier.weight(1f),
                    selected = selected == BottomDestination.SEARCH,
                    onClick = { onSelect(BottomDestination.SEARCH) },
                )
                BottomNavItem(
                    iconRes = Res.drawable.save_icon,
                    label = "Watch list",
                    fontFamily = tabFont,
                    modifier = Modifier.weight(1f),
                    selected = selected == BottomDestination.WATCH_LIST,
                    onClick = { onSelect(BottomDestination.WATCH_LIST) },
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    iconRes: DrawableResource,
    label: String,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (selected) AppColors.BottomNavActive else AppColors.BottomNavInactive
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = tint,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            ),
        )
    }
}
