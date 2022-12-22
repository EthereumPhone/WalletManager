package org.ethereumphone.walletmanager.ui

import androidx.compose.runtime.*
import androidx.core.os.trace
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.navigation.*
import org.ethereumphone.walletmanager.theme.TopLevelDestination

@Composable
fun rememberWalletManagerAppState(
    navController: NavHostController = rememberNavController(),
    network: MutableState<Network>
): WalletManagerState {
    return remember(navController) {
        WalletManagerState(navController, network)
    }
}

@Stable
class WalletManagerState(
    val navController: NavHostController,
    val network: MutableState<Network>
) {

    fun changeNetwork(newNetwork: Network) {
        network.value = newNetwork
    }

    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() = when(currentDestination?.route) {
            homeRoute -> TopLevelDestination.HOME
            sendRoute -> TopLevelDestination.SEND
            assetRoute -> TopLevelDestination.ASSET
            else -> null
        }

    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.values().asList()

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        trace("Navigation: ${topLevelDestination.name}") {
            val topLevelNavOptions = navOptions {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            when (topLevelDestination) {
                TopLevelDestination.HOME -> navController.navigateToHome(topLevelNavOptions)
                TopLevelDestination.SEND -> navController.navigateToSend(topLevelNavOptions)
                TopLevelDestination.ASSET -> navController.navigateToAsset(topLevelNavOptions)
                TopLevelDestination.RECEIVE -> navController.navigateToReceive(topLevelNavOptions)
            }
        }
    }
    fun onBackClick() {
        navController.popBackStack()
    }
}