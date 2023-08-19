package com.feature.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.util.Calendar

@Composable
internal fun AddressBar(

    userAddress: String,
    onclick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = when(LocalTime.now().hour) {
        in 5..12 -> "Gm \uD83C\uDF1E" // sun with face emoji
        in 13..16 -> "Ga  \uD83C\uDF07" // city-sunset emoji
        else -> "Gn \uD83C\uDF1D" // full moon with face emoji
    }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        Row(
            modifier = Modifier
                .clickable { onclick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = truncateText(userAddress),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9FA2A5)
            )
            Icon(
                imageVector = Icons.Rounded.ContentCopy,
                contentDescription = null,
                tint = Color(0xFF9FA2A5)
            )
        }
    }
}

private fun truncateText(text: String): String {
    if (text.length > 19) {
        return text.substring(0, 5) + "..." + text.takeLast(3) + " "
    }
    return text
}

@Preview
@Composable
fun previewAddressBar() {
    AddressBar(
        "0x123123123123123123",
        { }
    )
}