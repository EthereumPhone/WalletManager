package org.ethereumphone.walletmanager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.ui.WalletManagerState
import org.ethereumphone.walletmanager.ui.screens.HomeRoute
import org.ethereumphone.walletmanager.ui.screens.ReceiveScreen
import org.ethereumphone.walletmanager.ui.screens.SendRoute
import org.ethereumphone.walletmanager.utils.SelectedNetworkViewModel
import org.ethereumphone.walletmanager.utils.WalletInfoApi
import org.ethereumphone.walletmanager.utils.WalletInfoViewModel


@Composable
fun WalletManagerNavHost(
    navController: NavHostController,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = homeRoute,
    walletInfoApi: WalletInfoApi,
    walletInfoViewModel: WalletInfoViewModel,
    selectedNetwork: State<Network>,
    walletManagerState: WalletManagerState
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(homeRoute) {
            HomeRoute(
                walletInfoApi = walletInfoApi,
                walletInfoViewModel = walletInfoViewModel,
                walletManagerState = walletManagerState
            )
        }
        composable(sendRoute) {
            SendRoute(
                selectedNetwork = selectedNetwork
            )
        }
        composable(receiveRoute) {
            ReceiveScreen(
                address = walletInfoApi.walletAddress
            )
        }

    }
}

