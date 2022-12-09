package org.ethereumphone.walletmanager.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.ethereumphone.walletmanager.ui.screens.AssetRoute

const val assetRoute = "asset_route"

fun NavController.navigateToAsset(navOptions: NavOptions? = null) {
    this.navigate(homeRoute, navOptions)
}

fun NavGraphBuilder.assetScreen() {
    composable(route = homeRoute) {
        AssetRoute()
    }
}