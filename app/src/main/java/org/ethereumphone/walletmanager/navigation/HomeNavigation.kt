package org.ethereumphone.walletmanager.navigation

import androidx.compose.runtime.State
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.ui.screens.HomeRoute
import org.ethereumphone.walletmanager.utils.WalletInfoApi
import org.ethereumphone.walletmanager.utils.WalletInfoViewModel

const val homeRoute = "home_route"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(homeRoute, navOptions)
}

fun NavGraphBuilder.homeScreen(
    walletInfoApi: WalletInfoApi,
    walletInfoViewModel: WalletInfoViewModel,
    selectedNetwork: State<Network>
) {
    composable(route = homeRoute) {
        HomeRoute(
            walletInfoApi = walletInfoApi,
            walletInfoViewModel = walletInfoViewModel,
            selectedNetwork = selectedNetwork
        )
    }
}