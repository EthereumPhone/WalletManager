package com.feature.home.ui

import androidx.compose.foundation.clickable
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
import org.ethosmobile.components.library.core.ethOSIconButton

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
        Box(
            modifier = Modifier.clickable {
                navigateToSend()
            }
        ) {
            ethOSIconButton(
                onClick = { navigateToSend() },
                icon = Icons.Rounded.NorthEast,
                contentDescription = "Send"
            )
        }


        Box(
            modifier = Modifier.clickable {
                navigateToReceive()
            }
        ) {
            ethOSIconButton(
                onClick = { navigateToReceive() },
                icon = Icons.Rounded.ArrowDownward,
                contentDescription = "Receive"
            )
        }

        Box(
            modifier = Modifier.clickable {
                navigateToSwap()
            }
        ) {
            ethOSIconButton(
                onClick = { navigateToSwap() },
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