package com.feature.receive.navigation

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
import com.feature.receive.ReceiveRoute
import com.feature.receive.ReceiveScreen

const val receiveRoute = "receive_route"

fun NavController.navigateToReceive() {
    this.navigate(receiveRoute) {
        popUpTo("home_route") {
            inclusive = false
        }
    }
}


fun NavGraphBuilder.receiveScreen(
    onBackClick: () -> Unit
) {
    composable(
        route = receiveRoute,
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
    ) {
        ReceiveRoute(
            onBackClick = onBackClick
        )
    }
}
