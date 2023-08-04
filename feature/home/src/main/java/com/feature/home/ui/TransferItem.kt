package com.feature.home.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.core.designsystem.theme.background
import com.core.designsystem.theme.primary
import com.core.designsystem.theme.primaryVariant
import com.core.model.TransferItem
import kotlinx.datetime.Clock


@Composable
internal fun TransferItem(
    transfer: TransferItem
) {
    val headlineText = remember {
        buildString {
            append(if (transfer.userSent) "sent" else "received ")
            append(transfer.value)
            append(transfer.asset)
            append(if (transfer.userSent) " to " else " from ")
            append(transfer.address)
        }
    }
    val networkName = remember { chainToNetworkName(transfer.chainId) }

    ListItem(
        headlineContent = {
            Text(text = headlineText)
       },
        supportingContent = {
            Row {
                Text(
                    text = networkName,
                    modifier = Modifier.weight(1f)
                )
                Text(text = transfer.timeStamp)
            }
        },
        colors = ListItemDefaults.colors(
            headlineColor = primary,
            supportingColor = primary,
            containerColor = Color.Transparent
        )
   )
}

private fun chainToNetworkName(chainId: Int): String = when(chainId) {
        1 -> "Mainnet"
        5 -> "Görli"
        10 -> "Optimism"
        137 -> "Polygon"
        42161 -> "Arbitrum"
        else -> ""
    }

@Preview
@Composable
fun TransferItemPreview() {
    TransferItem(
        chainId = 5,
        asset = "ether",
        address = "0x123123",
        value = "2.24",
        timeStamp = Clock.System.now().toString(),
        userSent = true
    )
}