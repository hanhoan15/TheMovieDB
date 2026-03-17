package com.example.themoviedb.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.themoviedb.core.data.model.AppTab
import com.example.themoviedb.core.ui.theme.AppColors
import com.example.themoviedb.core.ui.theme.AppTypography

@Composable
fun CategoryTabs(
    tabs: List<AppTab>,
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fontFamily = AppTypography.poppinsMedium()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        tabs.forEach { tab ->
            val density = LocalDensity.current
            var indicatorWidth by remember(tab) { mutableStateOf(0.dp) }
            val selected = selectedTab == tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(tab) },
            ) {
                Text(
                    text = tab.label,
                    color = if (selected) Color.White else AppColors.TextMuted,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    ),
                    onTextLayout = { layoutResult ->
                        indicatorWidth = with(density) { layoutResult.size.width.toDp() }
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(indicatorWidth)
                        .height(4.dp)
                        .background(if (selected) AppColors.TabIndicator else Color.Transparent),
                )
            }
        }
    }
}
