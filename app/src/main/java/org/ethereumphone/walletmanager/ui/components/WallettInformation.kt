package org.ethereumphone.walletmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.walletmanager.theme.BoxBackground
import org.ethereumphone.walletmanager.theme.WalletManagerTheme
import org.ethereumphone.walletmanager.viewModel.ExchangeViewModel


@Composable
fun WalletInformation(
    ethAmount: Double,
    fiatAmount: Double,
    address: String
) {

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "$ethAmount ETH",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "$$fiatAmount",
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        MiddleEllipseText(address)
    }
}


@Composable
fun MiddleEllipseText(
    address: String
) {
    val text = address.take(5) + "..." + address.takeLast(4)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(BoxBackground)
            .padding(vertical = 3.dp, horizontal = 10.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}

@Preview
@Composable
fun PreviewWalletInformation() {
    WalletManagerTheme {
        Column(
            Modifier
                .background(Color(0xFF1D1D1F))
        ) {
            WalletInformation(
                ethAmount = 0.0719,
                fiatAmount = 90.52,
                address = "0x0000000000000123"
            )
        }


    }
}