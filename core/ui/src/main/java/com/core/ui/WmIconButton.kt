package com.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WmIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String? = null
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.Black
        )
    }
    if (contentDescription != null) {
        Text(text = contentDescription)

    }
}

@Composable
@Preview
fun PreviewWmIconButton() {
    WmIconButton(
        onClick = {},
        icon = Icons.Default.NorthEast,
        contentDescription = null
    )
}