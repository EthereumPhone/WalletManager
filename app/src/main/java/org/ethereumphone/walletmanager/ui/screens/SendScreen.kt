package org.ethereumphone.walletmanager.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.walletmanager.activities.MainActivity
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.theme.*
import org.ethereumphone.walletmanager.ui.components.InputField
import org.ethereumphone.walletmanager.utils.WalletSDK

@Composable
fun SendRoute(
    modifier: Modifier = Modifier,
    selectedNetwork: State<Network>
) {
    SendScreen(
        modifier = modifier,
        selectedNetworkState = selectedNetwork
    )
}


@Composable
fun SendScreen(
    modifier: Modifier = Modifier,
    selectedNetworkState: State<Network>,
) {
    val amount = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }
    val context = LocalContext.current
    val selectedNetwork = selectedNetworkState.value
    WalletManagerTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp),
            modifier = modifier
                .height(LocalConfiguration.current.screenHeightDp.dp)
                .padding(40.dp)

        ) {
            Text(
                text = "Send ether",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(),
                // Set color to be white
                color = md_theme_dark_onPrimary
            )
            InputField(
                value = "",
                label = "To",
                readOnly = false,
                singeLine = true,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                address.value = it
            }
            InputField(
                value = "",
                label = "Amount",
                readOnly = false,
                singeLine = true,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth(),
                onlyNumbers = true
            ) {
                amount.value = it
            }
            Button(
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = md_theme_light_primary),
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.value.isNotEmpty() && address.value.isNotEmpty(),
                onClick = {
                    println("Click")
                    val walletSDK = WalletSDK(context, web3RPC = selectedNetwork.chainRPC)
                    walletSDK.sendTransaction(
                        to = address.value,
                        value = amount.value,
                        data = "",
                        chainId = selectedNetwork.chainId
                    ).whenComplete { txHash, throwable ->
                        println("Send transaction result: $txHash")
                    }
                }
            ) {
                Text(
                    text = "Send",
                    color = if (amount.value.isNotEmpty() && address.value.isNotEmpty()) md_theme_light_onPrimary else md_theme_light_onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
@Preview
fun PreviewMintingScreen() {
    WalletManagerTheme {
        val mainnetNetwork = Network(
            chainName = "Mainnet",
            chainId = 1,
            chainRPC = "https://cloudflare-eth.com",
            chainExplorer = "https://etherscan.io",
            chainCurrency = "ETH",
        )
        // Create State<Network> object with mainnetNetwork
        val selectedNetwork = object : State<Network> {
            override val value: Network
                get() = mainnetNetwork
        }
        SendScreen(selectedNetworkState = selectedNetwork)
    }
}