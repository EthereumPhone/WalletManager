package org.ethereumphone.walletmanager.feature_home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.ethereumphone.walletmanager.core.domain.model.Network
import org.ethereumphone.walletmanager.feature_home.ui.HomeRoute
import org.ethereumphone.walletmanager.ui.WalletManagerState

import org.ethereumphone.walletmanager.core.data.remote.WalletInfoApi
import org.ethereumphone.walletmanager.core.data.remote.WalletInfoViewModel

const val homeRoute = "home_route"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(homeRoute, navOptions)
}

fun NavGraphBuilder.homeScreen() {
    composable(route = homeRoute) {
        HomeRoute()
    }
}