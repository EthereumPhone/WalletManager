package org.ethereumphone.walletmanager.navigation

import android.content.Intent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.core.terminalsdk.ReflectiveLedPattern
import com.core.terminalsdk.TerminalSDK

import com.example.assets.navigation.navigateToAsset
import com.example.assets.navigation.navigateToAssetDetail
import com.example.transactions.navigation.navigateToTransaction
import com.example.transactions.navigation.navigateToTransactionDetail
import com.example.transactions.navigation.transactionGraph
import com.feature.home.navigation.homeGraph
import com.feature.home.navigation.homeGraphRoutePattern
import com.feature.home.navigation.homeRoute
import com.feature.home.navigation.navigateToHome
import com.feature.paymaster.navigation.navigateToPayMaster
import com.feature.paymaster.navigation.payMasterScreen
import com.feature.receive.navigation.navigateToReceive
import com.feature.receive.navigation.receiveScreen
import com.feature.send.navigation.navigateToSend
import com.feature.send.navigation.sendRoute
import com.feature.send.navigation.sendScreen
import com.feature.swap.navigation.navigateToSwap
import com.feature.swap.navigation.swapScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ethereumphone.walletmanager.ui.WmAppState

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WmNavHost(
    appState: WmAppState,
    modifier: Modifier = Modifier,
    startDestination: String = homeGraphRoutePattern,
    terminalSDK: TerminalSDK?,
    reflectiveLedPattern: ReflectiveLedPattern?
) {
    val coroutineScope = rememberCoroutineScope()
    val navController = appState.navController
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier,
            enterTransition = { fadeIn(
                animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing
                )
            )
            },
            exitTransition = { fadeOut(
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing
                ))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                ))
            }
        ) {

            homeGraph(
                sharedTransitionScope = this@SharedTransitionLayout,
                navigateToSwap = {
                    navController.navigateToSwap()
                },
                navigateToSend = { groupId ->
                    coroutineScope.launch(Dispatchers.IO) {
                        terminalSDK?.finishScreen()
                    }
                    navController.navigateToSend(groupId=groupId)
                },
                navigateToLog = { it ->
                    coroutineScope.launch(Dispatchers.IO) {
                        terminalSDK?.finishScreen()
                    }
                    navController.navigateToTransaction(tokenId = it)
                },
                navigateToReceive = {
                    coroutineScope.launch(Dispatchers.IO) {
                        terminalSDK?.finishScreen()
                    }
                    navController.navigateToReceive()
                                    },
                navigateToPayMaster = {
                    coroutineScope.launch(Dispatchers.IO) {
                        terminalSDK?.finishScreen()
                    }
                    navController.navigateToPayMaster()
                                      },
                nestedGraphs = {
                    swapScreen(navController::popBackStack)
                    sendScreen(navController::popBackStack, navController, this@SharedTransitionLayout, reflectiveLedPattern)
                    receiveScreen(navController::popBackStack)
                    payMasterScreen(navController::popBackStack)
                }
            )

            transactionGraph(
                navigateBack = navController::popBackStack,
                navigateToDetail = { txHash ->
                    navController.navigateToTransactionDetail(txHash)
                }
            )
        }
    }

}