package com.feature.receive.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.feature.receive.ReceiveScreen

const val receiveRoute = "receive_route"

fun NavController.navigateToReceive() {
    this.navigate(receiveRoute)
}


fun NavGraphBuilder.receiveScreen(
    onBackClick: () -> Unit
) {
    composable(route = receiveRoute) {
        ReceiveScreen()
    }
}