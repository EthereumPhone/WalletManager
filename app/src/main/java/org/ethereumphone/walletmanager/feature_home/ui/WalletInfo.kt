package org.ethereumphone.walletmanager.feature_home.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.walletmanager.core.designsystem.WmTheme
import org.ethereumphone.walletmanager.core.designsystem.primary
import org.ethereumphone.walletmanager.feature_home.model.WalletAmount

@Composable
fun WalletInfo(
    walletAmount: WalletAmount
) {
    val ethAmount = walletAmount.ethAmount.toBigDecimal().stripTrailingZeros().toPlainString()
    val fiatAmount = walletAmount.fiatAmount.toBigDecimal().stripTrailingZeros().toPlainString()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Wallet",
            fontSize = 40.sp,
            color = primary,
            fontWeight = FontWeight.Bold
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
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewWalletInfo() {
    WmTheme {
        WalletInfo(
            WalletAmount(
                ethAmount = 4.3E-6

            )
        )
    }
}