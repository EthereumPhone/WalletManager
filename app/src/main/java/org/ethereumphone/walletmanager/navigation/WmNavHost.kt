package org.ethereumphone.walletmanager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.feature.home.navigation.homeGraph
import com.feature.receive.navigation.navigateToReceive
import com.feature.receive.navigation.receiveScreen
import com.feature.send.navigation.navigateToSend
import com.feature.send.navigation.sendScreen
import com.feature.swap.navigation.navigateToSwap
import com.feature.swap.navigation.swapScreen
import org.ethereumphone.walletmanager.ui.WmAppState

@Composable
fun WmNavHost(
    appState: WmAppState,
    modifier: Modifier = Modifier,
    startDestination: String = ""
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        homeGraph(
            navigateToSwap = {
                navController.navigateToSwap()
            },
            navigateToSend = {
                navController.navigateToSend()
            },
            navigateToReceive = {
                navController.navigateToReceive()
            },
            nestedGraphs = {
                swapScreen(navController::popBackStack)
                sendScreen(navController::popBackStack)
                receiveScreen(navController::popBackStack)
            }
        )
    }
}