package org.ethereumphone.walletmanager.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.assets.navigation.assetDetailScreen
import com.example.assets.navigation.assetGraph
import com.example.assets.navigation.navigateToAssetDetail
import com.example.transactions.navigation.navigateToTransactionDetail
import com.example.transactions.navigation.transactionDetailScreen
import com.example.transactions.navigation.transactionGraph
import com.feature.home.navigation.homeGraph
import com.feature.home.navigation.homeGraphRoutePattern
import com.feature.home.navigation.homeRoute
import com.feature.home.navigation.navigateToHome
import com.feature.receive.navigation.navigateToReceive
import com.feature.receive.navigation.receiveScreen
import com.feature.send.navigation.navigateToSend
import com.feature.send.navigation.sendRoute
import com.feature.send.navigation.sendScreen
import com.feature.swap.navigation.navigateToSwap
import com.feature.swap.navigation.swapScreen
import org.ethereumphone.walletmanager.ui.WmAppState

@Composable
fun WmNavHost(
    appState: WmAppState,
    modifier: Modifier = Modifier,
    startDestination: String = homeGraphRoutePattern,
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
        assetGraph(
            navigateToAssetDetail = navController::navigateToAssetDetail,
            nestedGraphs = {
                assetDetailScreen(
                    onBackClick = navController::popBackStack
                )
            }
        )
        transactionGraph(
            navigateToTxDetail = navController::navigateToTransactionDetail,
            nestedGraphs = {
                transactionDetailScreen(
                    onBackClick = navController::popBackStack
                )
            }

        )
    }
}

//         var address = destinationIntent?.getStringExtra("recipient_address") ?: "123"