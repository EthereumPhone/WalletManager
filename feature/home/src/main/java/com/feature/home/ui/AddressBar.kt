package com.feature.home.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.UserData
import com.core.ui.SelectedNetworkButton
import com.feature.home.WalletDataUiState
import java.time.LocalTime
import java.util.Calendar

@Composable
internal fun AddressBar(
    walletDataUiState: WalletDataUiState,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val localContext = LocalContext.current


    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        //Address
        Row (
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ){
            IconButton(
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Information",
                    tint = Color.Transparent,
                    modifier = modifier
                        .clip(CircleShape)
                    //.background(Color.Red)
                )
            }
            Box(
                modifier = Modifier
            ){
                val address = when(walletDataUiState) {
                    is WalletDataUiState.Loading -> {
                        "..."
                    }
                    is WalletDataUiState.Success -> {
                        walletDataUiState.userData.walletAddress
                    }
                }

                Text(
                    modifier = Modifier
                        .clickable {
                            copyTextToClipboard(localContext, address)
                        },
                    text = truncateText(address),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9FA2A5)
                )
            }
            icon()
        }
        //Networkpill
        Surface (
            modifier = Modifier
                .clip(CircleShape),
            color = Color(0xFF24303D),
            contentColor = Color.White
        ) {
            val network = when(walletDataUiState) {
                is WalletDataUiState.Loading -> {
                    "..."
                }
                is WalletDataUiState.Success -> {
                    walletDataUiState.userData.walletNetwork
                }
            }

            Text(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                text = chainName(network),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }

}

private fun truncateText(text: String): String {
    if (text.length > 19) {
        return text.substring(0, 5) + "..." + text.takeLast(3) + " "
    }
    return text
}

private fun chainName(chainId: String) = when(chainId) {
    "1" -> "Mainnet"
    "5" -> "Görli"
    "10" -> "Optimism"
    "137" -> "Polygon"
    "8453" -> "Base"
    "42161" -> "Arbitrum"
    else -> "Loading..."
}

@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}

@Preview
@Composable
fun previewAddressBar() {
    AddressBar(
        WalletDataUiState.Success(UserData("","")),
        icon = {
            IconButton(
                onClick = {
                    //opens InfoDialog
                    //showInfoDialog.value = true
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Information",
                    tint = Color(0xFF9FA2A5),

                )
            }
        },
    )
}