package org.ethereumphone.walletmanager.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService
import org.ethereumphone.walletmanager.theme.BoxBackground
import org.ethereumphone.walletmanager.theme.ListBackground
import org.ethereumphone.walletmanager.theme.WalletManagerTheme
import org.ethereumphone.walletmanager.theme.dark_gray3


@Composable
fun WalletInformation(
    ethAmount: Double,
    fiatAmount: Double,
    address: String,
    chainId: Int,
) {

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MiddleEllipseText(address)

        if (ethAmount == Double.MAX_VALUE) {
            Text(
                text = getGreeting(),
                fontWeight = FontWeight.Medium,
                fontSize = 55.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "$ethAmount" + if(chainId == 137) " MATIC" else " ETH",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.h4,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "$$fiatAmount",
                fontWeight = FontWeight.Light,
                style = MaterialTheme.typography.button,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Function to return GM if its in the morning, GA if its in the afternoon, GE if its in the evening, and GN if its at night
 */
fun getGreeting(): String {
    return when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "gm"
        in 12..19 -> "ga"
        in 20..23 -> "gn"
        else -> "gm"
    }
}
@Composable
fun MiddleEllipseText(
    address: String
) {
    val text = address.take(5) + "..." + address.takeLast(4)
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(dark_gray3)
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .clickable {
                val clipboard: ClipboardManager? =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText("Address", address)
                clipboard?.setPrimaryClip(clip)

                // Toast copy to clipboard
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()

            }
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.button,
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
                address = "0x0000000000000123",
                chainId = 1
            )
        }


    }
}