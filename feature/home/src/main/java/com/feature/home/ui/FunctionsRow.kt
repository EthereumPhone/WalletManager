package com.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.core.ui.WmIconButton

@Composable
internal fun FunctionsRow(
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToReceive: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WmIconButton(
            onClick = { navigateToSend() },
            icon = Icons.Rounded.NorthEast,
            contentDescription = "Send"
        )

        WmIconButton(
            onClick = { navigateToReceive() },
            icon = Icons.Rounded.ArrowDownward,
            contentDescription = "Receive"
        )

        WmIconButton(
            onClick = { navigateToSwap() },
            icon = Icons.Rounded.SwapVert,
            contentDescription = "Swap"
        )
    }
}

@Preview
@Composable
private fun previewFunctionsRow() {
    FunctionsRow(
        {},
        {},
        {}
    )
}