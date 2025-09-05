package com.feature.home.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feature.home.HomeRoute2
import kotlinx.serialization.Serializable

const val homeGraphRoutePattern = "home_graph"
const val homeRoute = "home_route"


fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(homeGraphRoutePattern, navOptions)
}

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.homeGraph(
    sharedTransitionScope: SharedTransitionScope,
    navigateToSwap: () -> Unit,
    navigateToSend: (groupId: String) -> Unit,
    navigateToLog: (String) -> Unit,
    navigateToReceive: () -> Unit,
    navigateToPayMaster: () -> Unit,
    nestedGraphs: NavGraphBuilder.() -> Unit,
) {
    navigation(
        route = homeGraphRoutePattern,
        startDestination = homeRoute
    ) {
        composable(route = homeRoute) {
            HomeRoute2(
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = this@composable,
                navigateToSwap = navigateToSwap,
                navigateToSend = navigateToSend,
                navigateToLog = navigateToLog,
                navigateToReceive = navigateToReceive,
                navigateToPayMaster = navigateToPayMaster
            )
        }
        nestedGraphs()
    }
}