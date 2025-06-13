package com.feature.paymaster.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.core.ui.util.largeEnterDuration
import com.core.ui.util.largeExitDuration
import com.feature.paymaster.PayMasterScreenRoute

const val paymasterRoute = "paymaster_route"

fun NavController.navigateToPayMaster() {
    this.navigate(paymasterRoute) {
        popUpTo("paymaster_route") {
            inclusive = false
        }
    }
}


fun NavGraphBuilder.payMasterScreen(
    onBackClick: () -> Unit
) {
    composable(
        route = paymasterRoute,
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
        PayMasterScreenRoute(
            onBackClick = onBackClick
        )
    }
}