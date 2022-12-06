package org.ethereumphone.walletmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.VerticalAlignBottom
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ButtonRow() {
    val modifier = Modifier.fillMaxWidth()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Send
        CircleButton(
            image = Icons.Rounded.NorthEast,
            buttonText = "send",
            onClick = {},
            modifier = modifier.weight(1f)
        )

        // Buy
        CircleButton(
            image = Icons.Default.CreditCard,
            buttonText = "Buy",
            onClick = {},
            modifier = modifier.weight(1f)
        )

        // Receive
        CircleButton(
            image = Icons.Rounded.VerticalAlignBottom,
            buttonText = "Receive",
            onClick = {},
            modifier = modifier.weight(1f)
        )
    }
}
@Preview
@Composable
fun PreviewButtonRow() {
    Column() {
        ButtonRow()
    }

}