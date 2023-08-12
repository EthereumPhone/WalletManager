package com.core.ui

import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.core.designsystem.theme.primary

@Composable
fun WmListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supportingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = headlineContent,
        modifier = modifier,
        supportingContent = supportingContent,
        colors = ListItemDefaults.colors(
            headlineColor = primary,
            supportingColor = primary,
            containerColor = Color.Transparent
        )
    )
}