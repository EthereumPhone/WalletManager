package org.ethereumphone.walletmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.walletmanager.models.Exchange
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.models.Transaction
import org.ethereumphone.walletmanager.theme.NetworkStyle
import org.ethereumphone.walletmanager.theme.WalletManagerTheme
import org.ethereumphone.walletmanager.theme.black
import org.ethereumphone.walletmanager.ui.WalletManagerState
import org.ethereumphone.walletmanager.ui.components.*
import org.ethereumphone.walletmanager.ui.rememberWalletManagerAppState
import org.ethereumphone.walletmanager.utils.WalletInfoApi
import org.ethereumphone.walletmanager.utils.WalletInfoViewModel
import org.ethereumphone.walletsdk.WalletSDK
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.CompletableFuture

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    walletInfoViewModel: WalletInfoViewModel,
    walletInfoApi: WalletInfoApi,
    walletManagerState: WalletManagerState
) {

    val ethAmount = walletInfoViewModel.networkAmount.observeAsState(0.0)
    val historicTransactions = walletInfoViewModel.historicTransactions.observeAsState(listOf())
    val exchange = walletInfoViewModel.exchange.observeAsState(Exchange("ETHUSDT","0.0"))
    val fiatAmount = ethAmount.value * exchange.value.price.toDouble()

    HomeScreen(
        ethAmount = BigDecimal(ethAmount.value).setScale(6, RoundingMode.HALF_EVEN).stripTrailingZeros().toString(),
        transactionList = historicTransactions.value,
        address = walletInfoApi.walletAddress,
        fiatAmount = BigDecimal(fiatAmount).setScale(2,RoundingMode.HALF_EVEN).toPlainString(),
        walletManagerState = walletManagerState
    )
}

private val networkStyles: List<NetworkStyle> = NetworkStyle.values().asList()

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    ethAmount : String,
    fiatAmount : String = "0.0",
    address : String = "0x0000000000000000000000000000000000000000",
    transactionList: List<Transaction> = listOf(),
    walletManagerState: WalletManagerState
) {
    val context = LocalContext.current
    var showDialog by remember {mutableStateOf(false)}
    val walletSDK = WalletSDK(context)

    CompletableFuture.runAsync {
        while(true) {
            val newChainId = walletSDK.getChainId()
            if (walletManagerState.network.value.chainId != newChainId) {
                when(newChainId) {
                    1 -> walletManagerState.changeNetwork(Ethereum)
                    5 -> walletManagerState.changeNetwork(Goerli)
                    42161 -> walletManagerState.changeNetwork(Arbitrum)
                    10 -> walletManagerState.changeNetwork(Optimism)
                    137 -> walletManagerState.changeNetwork(Polygon)
                }
            }
            Thread.sleep(1000)
        }
    }

    if(showDialog) {
        networkDialog(
            currentNetwork = walletManagerState.network.value,
            setShowDialog = { showDialog = it}
        ) {
            println(it)

            if (walletSDK.getChainId() != it.chainId) {
                walletSDK.changeChainid(it.chainId).whenComplete { s, throwable ->
                    if (s != WalletSDK.DECLINE) {
                        walletManagerState.changeNetwork(it)
                    }
                }
            }

        }
    }
    Box(
        modifier = Modifier.background(black)
    ){
        Column (){
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
                selectedNetwork = walletManagerState.network,
                address = walletSDK.getAddress())
        }
    }

}

