package org.ethereumphone.walletmanager.ui

import android.content.Context.MODE_PRIVATE
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.models.Transaction
import org.ethereumphone.walletmanager.navigation.WalletManagerNavHost
import org.ethereumphone.walletmanager.theme.WalletManagerTheme
import org.ethereumphone.walletmanager.ui.components.BottomNavBar
import org.ethereumphone.walletmanager.ui.components.TransactionItem
import org.ethereumphone.walletmanager.ui.screens.HomeScreen
import org.ethereumphone.walletmanager.utils.SelectedNetworkViewModel
import org.ethereumphone.walletmanager.utils.WalletInfoApi
import org.ethereumphone.walletmanager.utils.WalletInfoViewModel

@Composable
fun WalletManagerApp(
    selectedNetworkViewModel: SelectedNetworkViewModel = SelectedNetworkViewModel(),
    selectedNetwork: MutableState<Network> = selectedNetworkViewModel.selectedNetwork.observeAsState(
        Network(
            chainId = 1,
            chainName = "Ethereum Mainnet",
            chainCurrency = "ETH",
            chainRPC = "https://cloudflare-eth.com",
            chainExplorer = "https://etherscan.io"
        )) as MutableState<Network>,
    appState: WalletManagerState = rememberWalletManagerAppState(
        network = selectedNetwork
    )
) {





    val walletInfoApi = WalletInfoApi(
        context = LocalContext.current,
        sharedPreferences = LocalContext.current.getSharedPreferences("WalletInfo", MODE_PRIVATE)
    )
    val walletInfoViewModel = WalletInfoViewModel(
        walletInfoApi = walletInfoApi,
        network = selectedNetwork.value
    )
    WalletManagerTheme {
        Scaffold(

        ) { innerPadding ->
            WalletManagerNavHost(
                navController = appState.navController,
                onBackClick = appState::onBackClick,
                modifier = Modifier.padding(innerPadding),
                walletInfoApi = walletInfoApi,
                walletInfoViewModel = walletInfoViewModel,
                selectedNetwork = selectedNetwork,
                walletManagerState = appState
            )
        }

    }
}

@Preview
@Composable
fun PreviewApp() {
    WalletManagerApp()
}
