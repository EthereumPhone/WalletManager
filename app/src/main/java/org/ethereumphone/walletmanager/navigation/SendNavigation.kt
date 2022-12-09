package org.ethereumphone.walletmanager.navigation
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.ethereumphone.walletmanager.ui.screens.AssetRoute
import org.ethereumphone.walletmanager.ui.screens.SwapRoute
import org.ethereumphone.walletmanager.ui.screens.SendRoute

const val sendRoute = "send_route"

fun NavController.navigateToSend(navOptions: NavOptions? = null) {
    this.navigate(sendRoute, navOptions)
}

fun NavGraphBuilder.sendScreen() {
    composable(route = sendRoute) {
        SendRoute()
    }
}