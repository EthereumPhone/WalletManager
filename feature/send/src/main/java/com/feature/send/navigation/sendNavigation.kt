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
import com.core.terminalsdk.ReflectiveLedPattern
import com.core.ui.util.largeEnterDuration
import com.core.ui.util.largeExitDuration
import com.feature.send.SendRoute

const val sendRoute = "send_route?address={address}&groupId={groupId}&amount={amount}&chainId={chainId}"
const val sendDeepLinkPattern = "app://wallet_manager/send_deep_link/{address}"


fun NavController.navigateToSend(
    address: String = "",
    groupId: String = "",
    amount: String = "",
    chainId: String = "",
) {
    val route = "send_route?address=$address&groupId=$groupId&amount=$amount&chainId=$chainId"

    this.navigate(route) {
        popUpTo("home_route") {
            inclusive = false
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.sendScreen(
    onBackClick: () -> Unit,
    navController: NavController
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
            navArgument("groupId") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("amount") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("chainId") {
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
        val groupId = backStackEntry.arguments?.getString("groupId")

        SendRoute(
            onBackClick = {
                if (!address.isNullOrEmpty()) {
                    // Navigate to the home screen explicitly if opened via a deep link
                    navController.navigate("home_route") {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                } else {
                    onBackClick()
                }
            }
        )
    }
}
