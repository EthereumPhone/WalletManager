package com.feature.swap.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.core.ui.util.largeEnterDuration
import com.core.ui.util.largeExitDuration
import com.feature.swap.SwapRoute

const val swapRoute = "swap_route"
const val swapRouteWithToken = "swap_route/{tokenGroupId}"

fun NavController.navigateToSwap(tokenGroupId: String? = null) {
    val route = if (tokenGroupId != null) {
        "swap_route/$tokenGroupId"
    } else {
        swapRoute
    }
    this.navigate(route) {
        popUpTo("home_route") {
            inclusive = false
        }
    }
}

fun NavGraphBuilder.swapScreen(
    onBackClick: () -> Unit
) {
    // Route without token
    composable(
        route = swapRoute,
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
            )
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(
                durationMillis = largeExitDuration,
                easing = FastOutSlowInEasing
            )
            )
        }
    ) {
        SwapRoute(
            modifier = Modifier,
            onBackClick = onBackClick,
            initialTokenGroupId = null
        )
    }
    
    // Route with token
    composable(
        route = swapRouteWithToken,
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
            )
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(
                durationMillis = largeExitDuration,
                easing = FastOutSlowInEasing
            )
            )
        }
    ) { backStackEntry ->
        val tokenGroupId = backStackEntry.arguments?.getString("tokenGroupId")
        SwapRoute(
            modifier = Modifier,
            onBackClick = onBackClick,
            initialTokenGroupId = tokenGroupId
        )
    }
}
