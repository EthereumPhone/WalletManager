package org.ethereumphone.walletmanager.feature_home.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.walletmanager.core.designsystem.primary
import org.ethereumphone.walletmanager.feature_home.model.WalletAmount

@Composable
fun WalletInfo(
    walletAmount: WalletAmount
) {
    val ethAmount = walletAmount.ethAmount.toString()
    val fiatAmount = walletAmount.fiatAmount.toString()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Wallet",
            fontSize = 40.sp,
            fontWeight = FontWeight.W100,
            color = primary
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "$ethAmount ETH · $$fiatAmount",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = primary
            )
    }

}


@Preview
@Composable
fun PreviewWalletInfo() {
    WalletInfo(
        WalletAmount()
    )
}