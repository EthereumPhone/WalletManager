package com.example.assets.navigation

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.assets.AssetDetailRoute
import com.example.assets.AssetRoute
import java.net.URLDecoder
import java.net.URLEncoder

private val URL_CHARACTER_ENCODING = Charsets.UTF_8.name()

@VisibleForTesting
internal const val symbolArg = "symbol"


const val assetDetailGraphRoutePattern = "assetdetail_graph"
const val assetDetailRoute = "assetdetail_route"

internal class SymbolArgs(val symbol: String) {
    constructor(savedStateHandle: SavedStateHandle) :
            this(URLDecoder.decode(checkNotNull(savedStateHandle[symbolArg]), URL_CHARACTER_ENCODING))
}

fun NavController.navigateToAssetDetail(symbol: String) {
    val encodedSymbol = URLEncoder.encode(symbol, URL_CHARACTER_ENCODING)
    this.navigate("$assetDetailRoute/$encodedSymbol") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.assetDetailScreen(
    onBackClick: () -> Unit,
) {

    composable(
        route = "$assetDetailRoute/{$symbolArg}",
        arguments = listOf(
            navArgument(symbolArg) { type = NavType.StringType },
        ),
    ) {
        AssetDetailRoute(
            navigateToAsset = onBackClick
        )
    }


}