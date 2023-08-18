package org.ethereumphone.walletmanager.navigation
import androidx.compose.runtime.State
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.ethereumphone.walletmanager.core.model.Network
import org.ethereumphone.walletmanager.ui.screens.SendRoute

const val sendRoute = "send_route"

fun NavController.navigateToSend(navOptions: NavOptions? = null) {
    this.navigate(sendRoute, navOptions)
}

fun NavGraphBuilder.sendScreen(
    onBackClick: () -> Unit,
    selectedNetwork: State<Network>
) {
    composable(route = sendRoute) {
        SendRoute(
            selectedNetwork = selectedNetwork,
            onBackClick = onBackClick
        )
    }
}