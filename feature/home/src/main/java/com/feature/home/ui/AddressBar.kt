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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.UserData
import com.core.ui.SelectedNetworkButton
import com.feature.home.WalletDataUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumphone.walletsdk.WalletSDK
import java.time.LocalTime
import java.util.Calendar

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
internal fun AddressBar(
    userData: WalletDataUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val address = when(userData) {
        is WalletDataUiState.Loading -> {
            "..."
    }
        is WalletDataUiState.Success -> {
        truncateText(userData.userData.walletAddress)
    }
    }

    val network = when(userData) {
        is WalletDataUiState.Loading -> {
            "..."
        }
        is WalletDataUiState.Success -> {
            chainName(userData.userData.walletNetwork)
        }
    }

    var chainColor = when(userData) {
        is WalletDataUiState.Success -> {
            val data = userData.userData
            when(data.walletNetwork) {
                "1" -> Color(0xFF32CD32)
                "5" -> Color(0xFFF0EAD6)
                "137" -> Color(0xFF442fb2) // Polygon
                "10" -> Color(0xFFc82e31) // Optimum
                "42161" -> Color(0xFF2b88b8) // Arbitrum
                "8453" -> Color(0xFF053BCB) // Base
                "7777777" -> Color(0xFF777777)

                else -> {
                    Color(0xFF030303)
                }
            }
        }
        is WalletDataUiState.Loading -> {
            Color.Transparent
        }
    }

        Surface (
            modifier = Modifier.clip(CircleShape).clickable {
                copyTextToClipboard(context,address)
            },
            color = Color(0xFF262626),
            contentColor = Color.White
        ) {
            Row (
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier.padding(4.dp,4.dp)//(4.dp,4.dp)
            ){
                //Add later
                Box(
                    modifier = modifier.clip(CircleShape)
                        .size(24.dp)
                        .background(chainColor)

                )
                Text(
                    text = network,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
               )
                Text(
                    modifier = modifier.padding(end=12.dp),
                    text = truncateText(address),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

        }


}

private fun chainName(chainId: String) = when(chainId) {
    "1" -> "Mainnet"
    "5" -> "Görli"
    "10" -> "Optimism"
    "137" -> "Polygon"
    "8453" -> "Base"
    "42161" -> "Arbitrum"
    "7777777" -> "Zora"
    else -> "Loading..."
}

@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}


private fun truncateText(text: String): String {
    if (text.length > 19) {
        return text.substring(0, 5) + "..." //+ text.takeLast(3) + " "
    }
    return text
}

@Preview
@Composable
fun previewAddressBar() {

//    AddressBar(
////        UserData("0x123123123123123123"),
////        { },
//        icon = {
//            IconButton(
//                onClick = {
//                    //opens InfoDialog
//                    //showInfoDialog.value = true
//                }
//            ) {
//                Icon(
//                    imageVector = Icons.Outlined.Info,
//                    contentDescription = "Information",
//                    tint = Color(0xFF9FA2A5),
//
//                )
//            }
//        },
////        currentChain = 5,
////        getCurrentChain = {
////
////        }
//
//    )
}