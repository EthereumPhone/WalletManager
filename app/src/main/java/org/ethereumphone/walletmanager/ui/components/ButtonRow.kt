package org.ethereumphone.walletmanager.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.VerticalAlignBottom
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.walletmanager.navigation.sendRoute
import org.ethereumphone.walletmanager.theme.TopLevelDestination
import org.ethereumphone.walletmanager.theme.WalletManagerTheme
import org.ethereumphone.walletmanager.ui.WalletManagerState
import org.ethereumphone.walletmanager.utils.WalletSDK

@Composable
fun ButtonRow(
    //walletManagerState: WalletManagerState,
) {
    val modifier = Modifier
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        val context = LocalContext.current
        val iconButtons = listOf(
            Triple(
                Icons.Rounded.NorthEast,
                "Send"
            ) {
                //walletManagerState.navController.navigate(sendRoute)
                // Navigate to send screen
                //walletManagerState.navigateToTopLevelDestination(TopLevelDestination.SEND)
              },
            Triple(
                Icons.Default.CreditCard,
                "Buy"
            ) {
                //val userAddr = WalletSDK(context).getAddress()
                // Copy address to clipboard
                //val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                //clipboard.setPrimaryClip(ClipData.newPlainText("address", userAddr))
                // Make toast that address has been copied
                //Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_LONG).show()
                // Open link in browser
                //val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ramp.network/buy/?defaultAsset=ETH"))
                //context.startActivity(browserIntent)
            },
            Triple(
                Icons.Rounded.VerticalAlignBottom,
                "Receive"
            ) {
                // Navigate to receive screen
                //walletManagerState.navigateToTopLevelDestination(TopLevelDestination.RECEIVE)
            }
        )
        for(item in iconButtons) {
            val (icon, text, onClick) = item

            CircleButton(
                image = icon,
                buttonText = text,
                onClick = onClick,
                modifier = modifier.size(width = 80.dp, height = 80.dp)
            )
        }
    }
}

@Composable
fun CircleButton(
    image: ImageVector,
    buttonText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = MaterialTheme.colors.primary

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(50.dp)
                .clip(shape = CircleShape)
                .background(color = color)
        ) {
            Icon(
                image,
                contentDescription = "Arrow",
                tint = Color.White,
                modifier = Modifier.fillMaxSize(.60f)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = buttonText,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}

@Preview
@Composable
fun CircleButtonPreview() {
    WalletManagerTheme {
        CircleButton(
            Icons.Filled.NorthEast,
            "Send",
            modifier = Modifier
        ) {}
    }
}

@Preview
@Composable
fun PreviewButtonRow() {
    WalletManagerTheme {
        ButtonRow()
    }
}