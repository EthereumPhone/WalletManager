package com.example.transactions.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.transactions.LogRoute
import com.example.transactions.TransactionRoute

const val transactionGraphRoutePattern = "transaction_graph?tokenId={tokenId}"
const val transactionRoute = "transaction_route"

fun NavController.navigateToTransaction(tokenId: String = "") {


    val route = transactionGraphRoutePattern.replace("{tokenId}", tokenId)

    this.navigate(route) {
        popUpTo("home_route") {
            inclusive = false
        }
    }
}

fun NavGraphBuilder.transactionGraph(
    navigateBack: () -> Unit,
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
            )
        ){ backStackEntry ->
            val tokenId = backStackEntry.arguments?.getString("tokenId")
                LogRoute(
                    tokenId = tokenId,
                    navigateBack = navigateBack
                )

        }
    }
}