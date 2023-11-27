package com.example.assets.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.assets.AssetRoute

const val assetGraphRoutePattern = "asset_graph"
const val assetRoute = "asset_route"

fun NavController.navigateToAsset(navOptions: NavOptions? = null) {
    this.navigate(assetGraphRoutePattern, navOptions)
}

fun NavGraphBuilder.assetGraph(
    navigateToAssetDetail: (String) -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit,
) {
    navigation(
        route = assetGraphRoutePattern,
        startDestination = assetRoute
    ) {
        composable(route = assetRoute) {
            AssetRoute(
                navigateToAssetDetail = navigateToAssetDetail
            )
        }
        nestedGraphs()
    }
}