package com.feature.send.navigation

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.feature.send.SendRoute

const val sendRoute = "send_route"
const val sendDeepLinkPattern = "app://wallet_manager/send_deep_link/{address}"


fun NavController.navigateToSend(address: String? = null) {
    this.navigate(sendRoute)
}

fun NavGraphBuilder.sendScreen(
    onBackClick: () -> Unit,

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
            }
        )
    ) { backStackEntry ->
        val address = backStackEntry.arguments?.getString("address")
        SendRoute(
            onBackClick= onBackClick,
            initialAddress = address?: ""
        )
    }
}
