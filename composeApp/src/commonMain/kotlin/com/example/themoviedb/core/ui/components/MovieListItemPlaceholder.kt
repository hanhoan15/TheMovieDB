package com.example.themoviedb.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun MovieListItemPlaceholder(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        ShimmerPlaceholder(
            modifier = Modifier
                .width(92.dp)
                .height(126.dp)
                .clip(RoundedCornerShape(12.dp)),
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            ShimmerPlaceholder(
                modifier = Modifier
                    .width(210.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )
            Spacer(modifier = Modifier.height(12.dp))
            ShimmerPlaceholder(
                modifier = Modifier
                    .width(72.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerPlaceholder(
                modifier = Modifier
                    .width(96.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerPlaceholder(
                modifier = Modifier
                    .width(88.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerPlaceholder(
                modifier = Modifier
                    .width(104.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )
        }
    }
}
