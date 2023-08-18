package com.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feature.home.HomeRoute

const val homeGraphRoutePattern = "home_graph"
const val homeRoute = "home_route"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(homeGraphRoutePattern, navOptions)
}

fun NavGraphBuilder.homeGraph(
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToReceive: () -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = homeGraphRoutePattern,
        startDestination = homeRoute
    ) {
        composable(route = homeRoute) {
            HomeRoute(
                navigateToSwap = navigateToSwap,
                navigateToSend = navigateToSend,
                navigateToReceive = navigateToReceive
            )
        }
        nestedGraphs()
    }
}