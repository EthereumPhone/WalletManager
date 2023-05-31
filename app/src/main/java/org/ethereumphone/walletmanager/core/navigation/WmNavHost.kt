package org.ethereumphone.walletmanager.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import org.ethereumphone.walletmanager.feature_home.navigation.homeRoute
import org.ethereumphone.walletmanager.feature_home.navigation.homeScreen
import org.ethereumphone.walletmanager.ui.screens.HomeScreen


@Composable
fun WmNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = homeRoute
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
        ) {
            homeScreen()

    }



}