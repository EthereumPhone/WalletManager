package com.feature.send.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.feature.send.SendRoute

const val sendRoute = "send_route"
const val sendGraphRoutePattern = "send_graph"

fun NavController.navigateToSend() {
    this.navigate(sendRoute)
}

fun NavGraphBuilder.sendScreen(
    onBackClick: () -> Unit
) {
    composable(route = sendRoute) {
        SendRoute(
            onBackClick= onBackClick
        )
        //SendRoute()
    }
}
