package org.ethereumphone.walletmanager.feature_home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.ethereumphone.walletmanager.feature_home.ui.HomeRoute
import org.ethereumphone.walletmanager.ui.WalletManagerState

import org.ethereumphone.walletmanager.utils.WalletInfoApi
import org.ethereumphone.walletmanager.utils.WalletInfoViewModel

const val homeRoute = "home_route"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(homeRoute, navOptions)
}

fun NavGraphBuilder.homeScreen(
    walletInfoApi: WalletInfoApi,
    walletInfoViewModel: WalletInfoViewModel,
    walletManagerState: WalletManagerState
) {
    composable(route = homeRoute) {
        HomeRoute(
            walletInfoApi = walletInfoApi,
            walletInfoViewModel = walletInfoViewModel,
            walletManagerState = walletManagerState
        )
    }
}