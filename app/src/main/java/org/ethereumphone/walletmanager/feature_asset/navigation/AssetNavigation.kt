package org.ethereumphone.walletmanager.feature_asset.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable


const val assetRoute = "asset_route"

fun NavController.navigateToAsset(navOptions: NavOptions? = null) {
    this.navigate(assetRoute, navOptions)
}

fun NavGraphBuilder.assetScreen() {
    composable(route = assetRoute) {
        //AssetRoute()
    }
}