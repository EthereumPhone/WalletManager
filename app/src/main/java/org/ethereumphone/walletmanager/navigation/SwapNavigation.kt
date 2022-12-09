package org.ethereumphone.walletmanager.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.ethereumphone.walletmanager.ui.screens.AssetRoute
import org.ethereumphone.walletmanager.ui.screens.SwapRoute

const val swapRoute = "swap_route"

fun NavController.navigateToSwap(navOptions: NavOptions? = null) {
    this.navigate(homeRoute, navOptions)
}

fun NavGraphBuilder.swapScreen() {
    composable(route = homeRoute) {
        SwapRoute()
    }
}