package com.feature.swap.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.feature.swap.SwapRoute

const val swapRoute = "swap_route"

fun NavController.navigateToSwap() {
    this.navigate(swapRoute) {
        popUpTo("home_route") {
            inclusive = true
        }
    }
}

fun NavGraphBuilder.swapScreen(
    onBackClick: () -> Unit
) {
    composable(route = swapRoute) {
        SwapRoute(
            modifier = Modifier,
            onBackClick = onBackClick
        )
    }
}
