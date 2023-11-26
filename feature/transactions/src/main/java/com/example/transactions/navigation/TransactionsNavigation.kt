package com.example.transactions.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.transactions.TransactionRoute

const val transactionGraphRoutePattern = "transaction_graph"
const val transactionRoute = "transaction_route"

fun NavController.navigateToTransaction(navOptions: NavOptions? = null) {
    this.navigate(transactionGraphRoutePattern, navOptions)
}

fun NavGraphBuilder.transactionGraph(
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToReceive: () -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit,
) {
    navigation(
        route = transactionGraphRoutePattern,
        startDestination = transactionRoute
    ) {
        composable(transactionRoute){
            TransactionRoute()
        }
        nestedGraphs()
    }
}