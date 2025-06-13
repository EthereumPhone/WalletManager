package com.example.transactions.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.core.ui.util.largeEnterDuration
import com.core.ui.util.largeExitDuration
import com.example.transactions.DetailLogRoute
import com.example.transactions.LogRoute

const val transactionGraphRoutePattern = "transaction_graph?tokenId={tokenId}"
const val transactionRoute = "transaction_route"
const val transactionDetailRoute = "transaction_detail_route"

fun NavController.navigateToTransaction(tokenId: String = "") {


    val route = transactionGraphRoutePattern.replace("{tokenId}", tokenId)

    this.navigate(route) {
        popUpTo("home_route") {
            inclusive = false
        }
    }
}

fun NavController.navigateToTransactionDetail(txHash: String) {
    this.navigate("$transactionDetailRoute/$txHash")
}

fun NavGraphBuilder.transactionGraph(
    navigateBack: () -> Unit,
    navigateToDetail: (String) -> Unit
) {
    navigation(
        route = transactionGraphRoutePattern,
        startDestination = transactionRoute,
    ) {
        composable(
            route = transactionRoute,
            arguments = listOf(
                navArgument("tokenId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            ),
            enterTransition = { fadeIn(
                animationSpec = tween(
                    durationMillis = largeEnterDuration,
                    easing = FastOutSlowInEasing
                )
            )
            },
            exitTransition = { fadeOut(
                animationSpec = tween(
                    durationMillis = largeExitDuration,
                    easing = FastOutSlowInEasing
                )
            )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(
                    durationMillis = largeEnterDuration,
                    easing = FastOutSlowInEasing
                ))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(
                    durationMillis = largeExitDuration,
                    easing = FastOutSlowInEasing
                ))
            }
        ){ backStackEntry ->
            val tokenId = backStackEntry.arguments?.getString("tokenId")
                LogRoute(
                    tokenId = tokenId,
                    navigateBack = navigateBack,
                    onTransactionClick = navigateToDetail
                )

        }
        composable(
            route = "$transactionDetailRoute/{txHash}",
            arguments = listOf(
                navArgument("txHash") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val txHash = backStackEntry.arguments?.getString("txHash")
            if (txHash != null) {
                DetailLogRoute(
                    navigateBack = navigateBack,
                    txHash = txHash
                )
            }
        }
    }
}