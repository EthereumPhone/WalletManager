package org.ethereumphone.walletmanager.ui

import android.content.Context.MODE_PRIVATE
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import org.ethereumphone.walletmanager.navigation.WalletManagerNavHost
import org.ethereumphone.walletmanager.theme.WalletManagerTheme
import org.ethereumphone.walletmanager.ui.components.*
import org.ethereumphone.walletmanager.utils.SelectedNetworkViewModel
import org.ethereumphone.walletmanager.core.data.remote.WalletInfoApi
import org.ethereumphone.walletmanager.core.data.remote.WalletInfoViewModel

@Composable
fun WalletManagerApp(
    selectedNetworkViewModel: SelectedNetworkViewModel = SelectedNetworkViewModel(),
) {
    val walletInfoApi = WalletInfoApi(
        context = LocalContext.current,
        sharedPreferences = LocalContext.current.getSharedPreferences("WalletInfo", MODE_PRIVATE)
    )

    val selectedNetwork = selectedNetworkViewModel.selectedNetwork.observeAsState(
        when(walletInfoApi.getCurrentNetwork()) {
            1 -> Ethereum
            5 -> Goerli
            10 -> Optimism
            137 -> Polygon
            42161 -> Arbitrum
            else -> Ethereum
        }
    ) as MutableState
    val appState = rememberWalletManagerAppState(network = selectedNetwork)



    val walletInfoViewModel = WalletInfoViewModel(
        walletInfoApi = walletInfoApi,
        network = selectedNetwork.value
    )
    WalletManagerTheme {
        Scaffold() { innerPadding ->
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
