package org.ethereumphone.walletmanager.ui.screens

import android.content.Context.MODE_PRIVATE
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
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

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    selectedNetwork: Network
) {
    val walletInfoApi = WalletInfoApi(
        context = LocalContext.current,
        sharedPreferences = LocalContext.current.getSharedPreferences("WalletInfo", MODE_PRIVATE)
    )

    val ethAmount = walletInfoApi.getEthAmount(selectedNetwork = selectedNetwork)

    HomeScreen(
        modifier = modifier,
        transactionList = walletInfoApi.getHistoricTransactions(
            selectedNetwork = selectedNetwork
        ),
        ethAmount = ethAmount,
        fiatAmount = walletInfoApi.getEthAmountInUSD(ethAmount = ethAmount)
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    ethAmount : Double = 0.0,
    fiatAmount : Double = 0.0,
    address : String = "0x0000000000000000000000000000000000000000",
    transactionList: List<Transaction> = listOf()
) {
    Column {
        Spacer(modifier = Modifier.fillMaxWidth().height(50.dp))
        WalletInformation(
            ethAmount = ethAmount,
            fiatAmount = fiatAmount,
            address = address
        )
        Spacer(modifier = Modifier.fillMaxWidth().height(50.dp))
        ButtonRow()
        Spacer(modifier = Modifier.fillMaxWidth().height(50.dp))
        TransactionList(transactionList)
    }


}

@Preview
@Composable
fun PreviewHomeScreen() {
    val t1 = Transaction(
        type = true,
        value = "0.5",
        hash =  "0x1231",
        toAddr = "nceornea.eth",
        fromAddr = "mhaas.eth"
    )
    val t2 = Transaction(
        type = false,
        value = "0.5",
        hash =  "0x1231",
        toAddr = "nceornea.eth",
        fromAddr = "mhaas.eth"
    )
    HomeScreen(
        transactionList = listOf(t1,t2)
    )
}