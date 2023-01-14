package org.ethereumphone.walletmanager.navigation
import androidx.compose.runtime.State
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.ui.screens.AssetRoute
import org.ethereumphone.walletmanager.ui.screens.ReceiveScreen
import org.ethereumphone.walletmanager.ui.screens.SwapRoute
import org.ethereumphone.walletmanager.ui.screens.SendRoute

const val receiveRoute = "receive_route"

fun NavController.navigateToReceive(navOptions: NavOptions? = null) {
    this.navigate(receiveRoute, navOptions)
}

fun NavGraphBuilder.receiveScreen(
    address: String,
    selectedNetwork: State<Network>,
    onBackClick: () -> Unit,
) {
    composable(route = sendRoute) {
        ReceiveScreen(
            address = address,
            selectedNetwork = selectedNetwork,
            onBackClick = onBackClick
        )
    }
}