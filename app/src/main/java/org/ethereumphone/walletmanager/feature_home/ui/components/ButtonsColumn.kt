package org.ethereumphone.walletmanager.feature_home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ethereumphone.walletmanager.core.ui.WmButton
import org.ethereumphone.walletmanager.core.ui.WmButtonDefaults
import org.ethereumphone.walletmanager.feature_home.model.ButtonClicked

@Composable
fun ButtonsColumn(
    modifier: Modifier = Modifier,
    onClick: (ButtonClicked) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(25.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            WmButton(
                value = "  Send",
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.ArrowUpward,

                colors = WmButtonDefaults.wmLightRoundedButton()
            ) {
                onClick(ButtonClicked.SEND)
            }
            WmButton(
                value = "    Receive",
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.ArrowDownward,
                colors = WmButtonDefaults.wmLightRoundedButton()
            ) {
                onClick(ButtonClicked.RECEIVE)
            }
        }
        WmButton(
            value = "Buy",
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Rounded.Add,
            colors = WmButtonDefaults.wmDarkRoundedButton()
        ) {
            onClick(ButtonClicked.BUY)
        }
        WmButton(
            value = "Manage Tokens",
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Rounded.AttachMoney,
            colors = WmButtonDefaults.wmDarkRoundedButton()
        ) {
            onClick(ButtonClicked.MANAGE_TOKENS)
        }
    }
}


@Preview
@Composable
fun PreviewButtonsColum() {
    ButtonsColumn() {}
}