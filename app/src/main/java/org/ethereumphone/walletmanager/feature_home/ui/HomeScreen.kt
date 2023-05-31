package org.ethereumphone.walletmanager.feature_home.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ethereumphone.walletmanager.core.designsystem.background
import org.ethereumphone.walletmanager.core.domain.model.TokenExchange
import org.ethereumphone.walletmanager.core.domain.model.Network
import org.ethereumphone.walletmanager.core.model.NetworkStyle
import org.ethereumphone.walletmanager.core.model.SendData
import org.ethereumphone.walletmanager.core.model.Transaction
import org.ethereumphone.walletmanager.feature_home.model.ButtonClicked
import org.ethereumphone.walletmanager.feature_home.model.WalletAmount
import org.ethereumphone.walletmanager.feature_home.ui.components.ButtonsColumn
import org.ethereumphone.walletmanager.feature_home.ui.components.NetworkPill
import org.ethereumphone.walletmanager.feature_home.ui.components.TransactionList
import org.ethereumphone.walletmanager.feature_home.ui.components.WalletInfo
import org.ethereumphone.walletmanager.feature_receive.ui.ReceiveDialog
import org.ethereumphone.walletmanager.feature_send.ui.SendDialog
import org.ethereumphone.walletmanager.ui.WalletManagerState
import org.ethereumphone.walletmanager.ui.components.Arbitrum
import org.ethereumphone.walletmanager.ui.components.Ethereum
import org.ethereumphone.walletmanager.ui.components.Goerli
import org.ethereumphone.walletmanager.ui.components.Optimism
import org.ethereumphone.walletmanager.core.data.remote.WalletInfoApi
import org.ethereumphone.walletmanager.core.data.remote.WalletInfoViewModel
import org.ethereumphone.walletsdk.WalletSDK
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.CompletableFuture

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {

    val walletAmount = WalletAmount(
        ethAmount = (ethAmount.value).toBigDecimal().setScale(6,RoundingMode.HALF_EVEN).stripTrailingZeros().toDouble(),
        fiatAmount = (ethAmount.value * tokenExchange.value.price.toDouble()).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toDouble()
    )

    val network = walletManagerState.network.value

    when(walletManagerState.network.value.chainId) {
        1 -> network.color = NetworkStyle.MAIN.color
        5 -> network.color = NetworkStyle.GOERLI.color
        10 -> network.color = NetworkStyle.OPTIMISM.color
        137 -> network.color = NetworkStyle.POLYGON.color
        42161 -> network.color = NetworkStyle.ARBITRUM.color
        else -> network.color = NetworkStyle.MAIN.color
    }


    val context = LocalContext.current
    val walletSDK = WalletSDK(context)



    CompletableFuture.runAsync {
        while (true) {
            val newChainId = walletSDK.getChainId()
            if (newChainId != walletManagerState.network.value.chainId) {
                when (newChainId) {
                    1 -> walletManagerState.changeNetwork(Ethereum)
                    5 -> walletManagerState.changeNetwork(Goerli)
                    10 -> walletManagerState.changeNetwork(Optimism)
                    42161 -> walletManagerState.changeNetwork(Arbitrum)
                }
            }
            Thread.sleep(1000)
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    HomeScreen(
        address = walletInfoApi.walletAddress,
        walletAmount = walletAmount,
        network = walletManagerState.network,
        transactionList = historicTransactions.value,
        swipeRefreshState = swipeRefreshState,
        onRefresh = {
            coroutineScope.launch {
                swipeRefreshState.isRefreshing = true
                delay(1000)
                walletInfoViewModel.refreshInfo()
                swipeRefreshState.isRefreshing = false
            }
        },
        onSendConfirmed = { sendData ->
            val amount = sendData.amount.toString()
            var address = sendData.address

            // Check if phone is connected to internet using NetworkManager
            val connectivityManager = ContextCompat.getSystemService(context, android.net.ConnectivityManager::class.java)
            val activeNetwork = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
            val hasInternet = capabilities != null && capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            
            if(hasInternet) {
                val walletSDK = WalletSDK(context, web3RPC = walletManagerState.network.value.chainRPC)
                val ensName = ENSName(address)
                if (ensName.isPotentialENSDomain()) {
                    val completableFuture = CompletableFuture<String>()
                    CompletableFuture.runAsync {
                        val ens = ENS(HttpEthereumRPC(walletManagerState.network.value.chainRPC))
                        completableFuture.complete(ens.getAddress(ensName)?.hex.toString())
                    }
                    address = completableFuture.get()
                }
                walletSDK.sendTransaction(
                    to = address,
                    value = BigDecimal(amount.replace(",",".").replace(" ","")).times(BigDecimal.TEN.pow(18)).toBigInteger().toString(),
                    data = "",
                    chainId = walletManagerState.network.value.chainId
                ).whenComplete { txHash, throwable ->
                    println("Send transaction result: $txHash")
                    // Open tx in etherscan
                    if (txHash != "decline") {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = android.net.Uri.parse(walletManagerState.network.value.chainExplorer + "/tx/" + txHash)
                        context.startActivity(intent)
                    }
                }
            } else {
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "No internet connection found", Toast.LENGTH_LONG).show()
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    address: String = "0x0000000000000000000000000000000000000000",
    walletAmount: WalletAmount = WalletAmount(),
    network: State<Network>,
    transactionList: List<Transaction> = listOf(),
    swipeRefreshState: SwipeRefreshState,
    onRefresh: () -> Unit,
    onSendConfirmed: (SendData) -> Unit,
) {
    var showSend by remember { mutableStateOf(false) }
    var showReceive by remember { mutableStateOf(false) }
    val context = LocalContext.current




    if (showSend) {
        SendDialog(
            walletAmount = walletAmount,
            setShowDialog = { showSend = false },
            modifier = Modifier.size(325.dp, 450.dp),
            onConfirm = { sendData ->
                onSendConfirmed(sendData)
                showSend = false
            }
        )
    }

    if (showReceive) {
        ReceiveDialog(
            address = address,
            modifier = Modifier
                .size(325.dp, 400.dp)
        ) {
            showReceive = false
        }
    }

    val scrollState = rememberScrollState()

    val boxHeight = LocalConfiguration.current.screenHeightDp.dp

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = onRefresh
    ) {
        Box(
            Modifier
                .verticalScroll(scrollState)
                .height(boxHeight)
        ) {
            Scaffold(
                containerColor = background,
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(50.dp, 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NetworkPill(
                        currentNetwork = network.value,
                        address = address
                    )
                    Spacer(Modifier.height(35.dp))
                    WalletInfo(walletAmount)
                    Spacer(Modifier.height(55.dp))
                    ButtonsColumn() { clicked ->
                        when (clicked) {
                            ButtonClicked.SEND -> showSend = true
                            ButtonClicked.RECEIVE -> showReceive = true
                            ButtonClicked.BUY -> openRampNetwork(context)
                            ButtonClicked.MANAGE_TOKENS -> openMyCrypto(context)
                        }
                    }
                    Spacer(Modifier.height(50.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        TransactionList(
                            address = address,
                            network = network.value,
                            transactionList = transactionList
                        )
                    }
                }
            }
            if (swipeRefreshState.isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .align(Alignment.TopCenter)
                ) {
                    CircularProgressIndicator(LocalContext.current)
                }
            }
        }
    }
}

fun openMyCrypto(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = android.net.Uri.parse("https://app.mycrypto.com")
    context.startActivity(intent)
}

fun openRampNetwork(context: Context) {
    // Open the website https://ramp.network
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = android.net.Uri.parse("https://ramp.network")
    context.startActivity(intent)
}


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun PreviewHome() {
    var items = listOf(
        Transaction(
            blockNumber = "",
            timeStamp = "",
            hash = "0x1231",
            nonce = "",
            blockHash = "",
            transactionIndex = "",
            from = "0x3a4e6ed8b0f02bfbfaa3c6506af2db939ea5798c",
            to = "nceornea.eth",
            value = "0.5",
            gas = "",
            gasPrice = "",
            isError = "",
            txReceiptStatus = "",
            input = "",
            contractAddress = "",
            cumulativeGasUsed = "",
            gasUsed = "",
            confirmations = "",
            methodId = "",
            functionName = ""
        ),
        Transaction(
            blockNumber = "",
            timeStamp = "",
            hash = "0x1231",
            nonce = "",
            blockHash = "",
            transactionIndex = "",
            from = "0x3a4e6ed8b0f02bfbfaa3c6506af2db939ea5798c",
            to = "nceornea.eth",
            value = "0.5",
            gas = "",
            gasPrice = "",
            isError = "",
            txReceiptStatus = "",
            input = "",
            contractAddress = "",
            cumulativeGasUsed = "",
            gasUsed = "",
            confirmations = "",
            methodId = "",
            functionName = ""
        ),
        Transaction(
            blockNumber = "",
            timeStamp = "",
            hash = "0x1231",
            nonce = "",
            blockHash = "",
            transactionIndex = "",
            from = "nceornea.eth",
            to = "0x3a4e6ed8b0f02bfbfaa3c6506af2db939ea5798c",
            value = "0.5",
            gas = "",
            gasPrice = "",
            isError = "",
            txReceiptStatus = "",
            input = "",
            contractAddress = "",
            cumulativeGasUsed = "",
            gasUsed = "",
            confirmations = "",
            methodId = "",
            functionName = ""
        )
    )

    var refreshing by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(refreshing, { println("Refreshing") })

    // Create a State object to hold the current network
    val network = mutableStateOf(Ethereum)
    //network.color = Color.Green
    HomeScreen(address = "nceornea.eth",
        network = network,
        transactionList = items,
        swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false),
        onRefresh = {},
        onSendConfirmed = {}
    )
}






