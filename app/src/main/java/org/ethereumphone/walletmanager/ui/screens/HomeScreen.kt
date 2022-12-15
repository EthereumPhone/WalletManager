package org.ethereumphone.walletmanager.ui.screens

import android.content.Context.MODE_PRIVATE
import android.os.NetworkOnMainThreadException
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.models.Transaction
import org.ethereumphone.walletmanager.theme.WalletManagerTheme
import org.ethereumphone.walletmanager.theme.md_theme_dark_background
import org.ethereumphone.walletmanager.ui.components.ButtonRow
import org.ethereumphone.walletmanager.ui.components.TransactionItem
import org.ethereumphone.walletmanager.ui.components.TransactionList
import org.ethereumphone.walletmanager.ui.components.WalletInformation
import org.ethereumphone.walletmanager.utils.WalletInfoApi
import org.ethereumphone.walletmanager.utils.WalletInfoViewModel

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    walletInfoViewModel: WalletInfoViewModel,
    walletInfoApi: WalletInfoApi,
    selectedNetwork: State<Network>
) {
    HomeScreen(
        ethAmount = walletInfoViewModel.ethAmount.observeAsState(0.0).value,
        transactionList = walletInfoViewModel.historicTransactions.observeAsState(listOf()).value,
        address = walletInfoApi.walletAddress,
        fiatAmount = walletInfoViewModel.ethAmountInUSD.observeAsState(0.0).value,
        selectedNetwork =  selectedNetwork
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    ethAmount : Double = 0.0,
    fiatAmount : Double = 0.0,
    address : String = "0x0000000000000000000000000000000000000000",
    transactionList: List<Transaction> = listOf(),
    selectedNetwork: State<Network>,
) {
    Column {
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp))
        WalletInformation(
            ethAmount = ethAmount,
            fiatAmount = fiatAmount,
            address = address
        )
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp))
        ButtonRow()
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp))
        TransactionList(transactionList, selectedNetwork = selectedNetwork)
    }


}

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