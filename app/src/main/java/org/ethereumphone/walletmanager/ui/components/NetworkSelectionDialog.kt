package org.ethereumphone.walletmanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.theme.ListBackground
import org.ethereumphone.walletmanager.theme.NetworkStyle
import org.ethereumphone.walletmanager.theme.WalletManagerTheme


val Ethereum = Network(
    chainId = 1,
    chainName = NetworkStyle.MAIN.networkName,
    chainCurrency = "ETH",
    chainRPC = "https://cloudflare-eth.com",
    chainExplorer = "https://etherscan.io"
)
val Goerli = Network(
    chainId = 5,
    chainName = NetworkStyle.GOERLI.networkName,
    chainCurrency = "ETH",
    chainRPC = "https://goerli.infura.io/v3/9aa3d95b3bc440fa88ea12eaa4456161",
    chainExplorer = "https://goerli.etherscan.io"
)

val Arbitrum = Network(
    chainId = 42161,
    chainName =NetworkStyle.ARBITRUM.networkName,
    chainCurrency = "ETH",
    chainRPC = "https://rpc.ankr.com/arbitrum",
    chainExplorer = "https://arbiscan.io/"
)

val Optimism = Network(
    chainId = 10,
    chainName = NetworkStyle.OPTIMISM.networkName,
    chainCurrency = "ETH",
    chainRPC = "https://mainnet.optimism.io",
    chainExplorer = "https://optimistic.etherscan.io/"
)

val Polygon = Network(
    chainId = 137,
    chainName = NetworkStyle.POLYGON.networkName,
    chainCurrency = "MATIC",
    chainRPC = "https://polygon-bor.publicnode.com",
    chainExplorer = "https://polygonscan.com/"
)

@Composable
fun networkDialog(
    currentNetwork: Network,
    setShowDialog: (Boolean) -> Unit,
    networkChanged: (Network) -> Unit
) {

    Dialog(
        onDismissRequest = { setShowDialog(false) },
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ListBackground.copy(alpha = 1f)
        ) {
            Box {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Select Network",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        fontSize = 24.sp
                        )
                    Spacer(Modifier.size(height = 10.dp, width = 0.dp))
                    networkList(currentNetwork.chainName) { networkName ->
                        setShowDialog(false)
                        when(networkName) {
                            NetworkStyle.MAIN.networkName -> networkChanged(Ethereum)
                            NetworkStyle.ARBITRUM.networkName -> networkChanged(Arbitrum)
                            NetworkStyle.GOERLI.networkName -> networkChanged(Goerli)
                            NetworkStyle.OPTIMISM.networkName -> networkChanged(Optimism)
                            NetworkStyle.POLYGON.networkName -> networkChanged(Polygon)
                        }
                    }
                }
            }
        }
    }
}


private val networks: List<NetworkStyle> = NetworkStyle.values().asList()

@Composable
fun networkList(
    selectedNetwork: String,
    onClick: (String) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(networks) {item ->
            networkListItem(
                network = item,
                selected = item.networkName.equals(selectedNetwork,true),
                onClick = {
                    onClick(item.networkName)
                })
        }
    }

}

@Composable
fun networkListItem(
    network: NetworkStyle,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier
        .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if(selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "active network",
                tint = Color.White
            )
        } else {
            Spacer(modifier = modifier.size(24.dp, 0.dp))
        }
        Icon(
            imageVector = Icons.Filled.Circle,
            contentDescription = network.networkName,
            tint = network.color
        )
        Text(
            text = network.networkName,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}


@Preview
@Composable
fun previewNetworkDialog() {
    WalletManagerTheme {
        var selected by remember { mutableStateOf(false) }

        val testNet = Network(
            123,
            "",
            "",
            "Ethereum Main Network",
            ""
        )

        Column(
            Modifier.fillMaxSize()
        ) {
            Button(
                onClick = {selected = !selected}
            ) {Text("test")}

            if(selected) {
                networkDialog(
                    setShowDialog = {selected = !selected},
                    currentNetwork = testNet
                ) {
                    println(it)
                }
            }
        }
    }


}