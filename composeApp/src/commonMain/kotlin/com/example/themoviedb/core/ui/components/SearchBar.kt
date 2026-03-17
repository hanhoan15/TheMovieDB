package com.example.themoviedb.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.themoviedb.core.ui.theme.AppColors
import com.example.themoviedb.core.ui.theme.AppTypography
import org.jetbrains.compose.resources.painterResource
import themoviedb.composeapp.generated.resources.Res
import themoviedb.composeapp.generated.resources.search_textbox_icon

@Composable
fun SearchBarReadOnly(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fontFamily = AppTypography.poppinsRegular()
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AppColors.SearchBackground,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Search",
                color = AppColors.TextHint,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                ),
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(Res.drawable.search_textbox_icon),
                contentDescription = "Search",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun SearchInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val inputFont = AppTypography.poppinsRegular()
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = inputFont,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
        ),
        placeholder = {
            Text(
                text = "Search",
                color = AppColors.TextSearchHint,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = inputFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                ),
            )
        },
        trailingIcon = {
            Icon(
                painter = painterResource(Res.drawable.search_textbox_icon),
                contentDescription = "Search",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp),
            )
        },
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppColors.SearchBackground,
            unfocusedContainerColor = AppColors.SearchBackground,
            disabledContainerColor = AppColors.SearchBackground,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = AppColors.TextSecondary,
            unfocusedTextColor = AppColors.TextSecondary,
            cursorColor = AppColors.AccentBlueBright,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}
