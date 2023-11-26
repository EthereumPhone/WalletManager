package com.example.assets.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.assets.AssetRoute

const val assetDetailGraphRoutePattern = "assetdetail_graph"
const val assetDetailRoute = "assetdetail_route"

fun NavController.navigateToAssetDetail(navOptions: NavOptions? = null) {
    this.navigate(assetDetailGraphRoutePattern, navOptions)
}

fun NavGraphBuilder.assetDetailGraph(
    navigateToAsset: () -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit,
) {
    navigation(
        route = assetDetailGraphRoutePattern,
        startDestination = assetDetailRoute
    ) {
        composable(route = assetDetailRoute) {
            AssetRoute(
                navigateToAsset = navigateToAsset
//                navigateToSwap = navigateToSwap,
//                navigateToSend = navigateToSend,
//                navigateToReceive = navigateToReceive
            )
        }
        nestedGraphs()
    }
}