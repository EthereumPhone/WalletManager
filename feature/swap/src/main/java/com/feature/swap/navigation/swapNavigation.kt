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

fun NavController.navigateToSwap() {
    this.navigate(swapRoute) {
        popUpTo("home_route") {
            inclusive = false
        }
    }
}

fun NavGraphBuilder.swapScreen(
    onBackClick: () -> Unit
) {
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
            onBackClick = onBackClick
        )
    }
}
