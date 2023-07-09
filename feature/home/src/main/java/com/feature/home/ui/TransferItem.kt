package com.feature.home.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.core.model.EntryCategory
import com.core.model.Transfer
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun TransferItem(
    transfer: Transfer
) {

    val time = transfer.blockTimestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    val prepend = (if (transfer.userIsSender) "sent " else "received ")
    val append = (if (transfer.userIsSender) " to ${truncateAddress(transfer.to)}" else " from ${truncateAddress(transfer.from)}")

    val transactionText = prepend + when (transfer.category) {
        EntryCategory.EXTERNAL -> "${transfer.value} ${transfer.asset}"
        else -> ""
    } + append

    ListItem(
        headlineContent = {
            Text(
                text = transactionText,
            )
       },
        supportingContent = {
            Row {
                Text(
                    text = chainToNetworkName(transfer.chainId),
                    modifier = Modifier.weight(1f)
                )
                Text(text = time)
            }
        }
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

private fun truncateAddress(address: String): String = address.substring(0,5) + "..." + address.takeLast(3)

@Preview
@Composable
fun TransferItemPreview() {
    TransferItem(
        Transfer(
            "ether",
            5,
            EntryCategory.EXTERNAL,
            emptyList(),
            "",
            "nicola",
            Transfer.RawContract("","",""),
            "0x123123123",
            "123",
            12.3,
            Clock.System.now(),
            true
        )
    )
}