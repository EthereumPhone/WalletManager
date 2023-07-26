package com.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.core.designsystem.theme.background

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
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White),
        modifier = Modifier.size(42.dp)
    ) {
        Icon(
            imageVector = icon,
            modifier = Modifier.size(32.dp),
            contentDescription = contentDescription,
            tint = background
        )
    }
    if (contentDescription != null) {
        Text(
            text = contentDescription,
            color = Color.White,
            modifier = Modifier.padding(top = 5.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
@Preview
fun PreviewWmIconButton() {
    WmIconButton(
        onClick = {},
        icon = Icons.Default.NorthEast,
        contentDescription = "Test"
    )
}