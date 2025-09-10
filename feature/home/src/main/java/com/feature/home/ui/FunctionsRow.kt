package com.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.core.ui.WmIconButton
import com.core.ui.util.debouncedClickable
import org.ethosmobile.components.library.core.ethOSIconButton

@Composable
internal fun FunctionsRow(
    navigateToSend: () -> Unit,
    navigateToReceive: () -> Unit,
    navigateToBuy: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier.debouncedClickable {
                navigateToSend()
            }
        ) {
            ethOSIconButton(
                onClick = { }, // Empty since Box handles the click
                icon = Icons.Rounded.NorthEast,
                contentDescription = "Send"
            )
        }


        Box(
            modifier = Modifier.debouncedClickable {
                navigateToReceive()
            }
        ) {
            ethOSIconButton(
                onClick = { }, // Empty since Box handles the click
                icon = Icons.Rounded.ArrowDownward,
                contentDescription = "Receive"
            )
        }

        Box(
            modifier = Modifier.debouncedClickable {
                navigateToBuy()
            }
        ) {
            ethOSIconButton(
                onClick = { }, // Empty since Box handles the click
                icon = Icons.Rounded.SwapVert,
                contentDescription = "Swap"
            )
        }




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