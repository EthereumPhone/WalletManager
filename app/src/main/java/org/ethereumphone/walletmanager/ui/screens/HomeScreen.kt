package org.ethereumphone.walletmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.models.Transaction
import org.ethereumphone.walletmanager.theme.NetworkStyle
import org.ethereumphone.walletmanager.ui.WalletManagerState
import org.ethereumphone.walletmanager.ui.components.ButtonRow
import org.ethereumphone.walletmanager.ui.components.TransactionList
import org.ethereumphone.walletmanager.ui.components.WalletInformation
import org.ethereumphone.walletmanager.ui.components.networkDialog
import org.ethereumphone.walletmanager.utils.WalletInfoApi
import org.ethereumphone.walletmanager.utils.WalletInfoViewModel
import org.ethereumphone.walletsdk.WalletSDK

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    walletInfoViewModel: WalletInfoViewModel,
    walletInfoApi: WalletInfoApi,
    walletManagerState: WalletManagerState
) {

    val ethAmount = walletInfoViewModel.ethAmount.observeAsState(0.0)
    val historicTransactions = walletInfoViewModel.historicTransactions.observeAsState(listOf())
    val fiatAmount = walletInfoViewModel.ethAmountInUSD.observeAsState(0.0)

    HomeScreen(
        ethAmount = ethAmount.value,
        transactionList = historicTransactions.value,
        address = walletInfoApi.walletAddress,
        fiatAmount = fiatAmount.value,
        walletManagerState = walletManagerState
    )
}

private val networkStyles: List<NetworkStyle> = NetworkStyle.values().asList()

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    ethAmount : Double = 0.0,
    fiatAmount : Double = 0.0,
    address : String = "0x0000000000000000000000000000000000000000",
    transactionList: List<Transaction> = listOf(),
    walletManagerState: WalletManagerState
) {
    val context = LocalContext.current
    var showDialog by remember {mutableStateOf(false)}

    if(showDialog) {
        networkDialog(
            currentNetwork = walletManagerState.network.value,
            setShowDialog = { showDialog = it}
        ) {
            println(it)
            walletManagerState.changeNetwork(it)
            val walletSDK = WalletSDK(context)
            if (walletSDK.getChainId() != it.chainId) {
                walletSDK.changeChainid(it.chainId)
            }
        }
    }

    Column {
        Column(Modifier.clickable {
            showDialog = !showDialog
        }) {
            Text(
                text = "Wallet Manager",
                fontSize = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Normal
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(shape = CircleShape)
                        .background(
                            networkStyles.first { it.networkName == walletManagerState.network.value.chainName }.color
                        )
                )
                Text(
                    text = "  "+walletManagerState.network.value.chainName,
                    fontSize = 12.sp,
                )
            }
        }

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp))
        WalletInformation(
            ethAmount = ethAmount,
            fiatAmount = fiatAmount,
            address = address,
            chainId = walletManagerState.network.value.chainId
        )
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(40.dp))
        ButtonRow(
            walletManagerState = walletManagerState,
        )
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp))
        TransactionList(
            transactionList,
            selectedNetwork = walletManagerState.network)
    }
}

/**
@Preview
@Composable
fun PreviewHomeScreen() {
    val walletInfoApi = WalletInfoApi(
        context = LocalContext.current,
        sharedPreferences = LocalContext.current.getSharedPreferences("WalletInfo", MODE_PRIVATE)
    )
    val historicalTxs by WalletInfoViewModel(walletInfoApi, network = Network(
        chainCurrency = "ETH",
        chainId = 1,
        chainName = "Ethereum",
        chainRPC = "https://cloudflare-eth.com",
        chainExplorer = "https://etherscan.io"
    )).historicTransactions.observeAsState(listOf())

    val selectedNetwork = object : State<Network> {
        override val value: Network
            get() = Network(1, "https://cloudflare-eth.com", "ETH", chainName = "Ethereum", chainExplorer = "https://etherscan.io")
    }

    HomeScreen(
        transactionList = historicalTxs,
        selectedNetwork = selectedNetwork
    )

}
        */