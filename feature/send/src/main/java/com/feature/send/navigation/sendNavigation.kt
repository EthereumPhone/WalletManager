package com.feature.send.navigation

import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.dgenlibrary.ui.theme.largeEnterDuration
import com.example.dgenlibrary.ui.theme.largeExitDuration
import com.feature.send.SendRoute2

const val sendRoute = "send_route?address={address}&tokenId={tokenId}"
const val sendDeepLinkPattern = "app://wallet_manager/send_deep_link/{address}"


fun NavController.navigateToSend(
    address: String = "",
    tokenId: String = "",
) {

    val route = "send_route?address=$address&tokenId=$tokenId"

    this.navigate(route) {
        popUpTo("home_route") {
            inclusive = false
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.sendScreen(
    onBackClick: () -> Unit,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
) {
    composable(
        route = sendRoute,
        deepLinks = listOf(
            navDeepLink {
                uriPattern = sendDeepLinkPattern
                action = Intent.ACTION_VIEW
            }
        ),
        arguments = listOf(
            navArgument("address") {
                type = NavType.StringType
                defaultValue = ""
            },
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
    ) { backStackEntry ->
        val address = backStackEntry.arguments?.getString("address")
        val tokenId = backStackEntry.arguments?.getString("tokenId")

        SendRoute2(
            onBackClick = {
                if (!address.isNullOrEmpty()) {
                    // Navigate to the home screen explicitly if opened via a deep link
                    navController.navigate("home_route")
                } else {
                    onBackClick()
                }
            },
            initialAddress = address,
            tokenId = tokenId,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = this@composable,
        )
    }
}
