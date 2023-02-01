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
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.models.Transaction
import org.ethereumphone.walletmanager.theme.NetworkStyle
import org.ethereumphone.walletmanager.theme.WalletManagerTheme
import org.ethereumphone.walletmanager.theme.black
import org.ethereumphone.walletmanager.ui.WalletManagerState
import org.ethereumphone.walletmanager.ui.components.ButtonRow
import org.ethereumphone.walletmanager.ui.components.TransactionList
import org.ethereumphone.walletmanager.ui.components.WalletInformation
import org.ethereumphone.walletmanager.ui.components.networkDialog
import org.ethereumphone.walletmanager.ui.rememberWalletManagerAppState
import org.ethereumphone.walletmanager.utils.WalletInfoApi
import org.ethereumphone.walletmanager.utils.WalletInfoViewModel

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    walletInfoViewModel: WalletInfoViewModel,
    walletInfoApi: WalletInfoApi,
    walletManagerState: WalletManagerState
) {
    HomeScreen(
        ethAmount = walletInfoViewModel.ethAmount.observeAsState(0.0).value,
        transactionList = walletInfoViewModel.historicTransactions.observeAsState(listOf()).value,
        address = walletInfoApi.walletAddress,
        fiatAmount = walletInfoViewModel.ethAmountInUSD.observeAsState(0.0).value,
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
    var showDialog by remember {mutableStateOf(false)}

    if(showDialog) {
        networkDialog(
            currentNetwork = walletManagerState.network.value,
            setShowDialog = { showDialog = it}
        ) {
            println(it)
            walletManagerState.changeNetwork(it)
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
                selectedNetwork = walletManagerState.network)
        }
    }

}


/*@Preview
@Composable
fun UIHomescreenPreview(){
    WalletManagerTheme{
        val t1 = Transaction(
            type = true,
            value = "0.5",
            hash =  "0x1231",
            toAddr = "nceornea.eth",
            fromAddr = "0x3a4e6ed8b0f02bfbfaa3c6506af2db939ea5798c"
        )
        val t2 = Transaction(
            type = false,
            value = "0.5",
            hash =  "0x1231",
            toAddr = "nceornea.eth",
            fromAddr = "0x3a4e6ed8b0f02bfbfaa3c6506af2db939ea5798c"
        )
        val transactionList = listOf(t1,t2)

        Column (
            modifier = Modifier.background(black)
                ){
            Column() {
                Text(
                    text = "Wallet Manager",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    textAlign = TextAlign.Center,
                    color = Color.White
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
                                Color.Green
                                //networkStyles.first { it.networkName == walletManagerState.network.value.chainName }.color
                            )
                    )
                    Text(
                        text = "  "+"Ethereum Main Network",//walletManagerState.network.value.chainName,
                        style = MaterialTheme.typography.button,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(50.dp))
            WalletInformation(
                ethAmount = 0.0719,
                fiatAmount = 90.52,
                address = "0xefBABdeE59968641DC6E892e30C470c2b40157Cd",
                chainId = 1//walletManagerState.network.value.chainId
            )
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(40.dp))

            var networklist by remember { mutableStateOf(Network(1, "https://cloudflare-eth.com", "ETH", chainName = "Ethereum", chainExplorer = "https://etherscan.io")
            ) }
            //val networklist = MutableState<Network>()
            //Network(1, "https://cloudflare-eth.com", "ETH", chainName = "Ethereum", chainExplorer = "https://etherscan.io")

            //State<Network> net =
            /*val appState: WalletManagerState = rememberWalletManagerAppState(
                network =
            )*/
            ButtonRow(
                //walletManagerState = null,
            )
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(50.dp))

            TransactionList(
                transactionList)//,
                //selectedNetwork = networklist)
        }
    }
}*/

/*
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

}*/
