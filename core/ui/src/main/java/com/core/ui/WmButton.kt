package com.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@Composable
fun WmButton(
    modifier: Modifier=Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {

    Button(
        onClick = onClick,
        content = content
    )
}