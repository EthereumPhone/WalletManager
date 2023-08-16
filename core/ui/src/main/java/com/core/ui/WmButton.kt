package com.core.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable

@Composable
fun WmButton(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {

    Button(
        onClick = onClick,
        content = content
    )
}