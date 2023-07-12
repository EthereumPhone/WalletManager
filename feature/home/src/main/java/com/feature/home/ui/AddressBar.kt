package com.feature.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.util.Calendar

@Composable
internal fun AddressBar(
    userAddress: String,
    onclick: () -> Unit
) {
    val greeting = when(LocalTime.now().hour) {
        in 5..11 -> "Gm \uD83C\uDF1E" // sun with face emoji
        in 12..16 -> "Ga  \uD83C\uDF07" // city-sunset emoji
        else -> "Gn \uD83C\uDF1D" // full moon with face emoji
    }

    Column {
        Text(
            text = greeting,
            fontSize = 16.sp
        )
        Row(
            modifier = Modifier.
            clickable { onclick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = truncateText(userAddress),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
            Icon(
                imageVector = Icons.Rounded.ContentCopy,
                contentDescription = null
            )
        }
    }
}

private fun truncateText(text: String): String {
    if (text.length > 8) {
        return text.substring(0, 8) + "... "
    }
    return text
}