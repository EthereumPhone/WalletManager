package org.ethereumphone.walletmanager.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.ui.screens.HomeRoute

const val homeRoute = "home_route"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(homeRoute, navOptions)
}

fun NavGraphBuilder.homeScreen() {
    composable(route = homeRoute) {
        HomeRoute(
            selectedNetwork = Network(
                chainCurrency = "ETH",
                chainId = 1,
                chainName = "Ethereum",
                chainRPC = "https://cloudflare-eth.com",
                chainExplorer = "https://etherscan.io"
            )
        )
    }
}