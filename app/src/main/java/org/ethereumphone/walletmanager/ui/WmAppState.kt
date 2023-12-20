package org.ethereumphone.walletmanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.core.data.util.NetworkMonitor
import com.core.model.CurrentState
import kotlinx.coroutines.CoroutineScope
import com.example.assets.navigation.assetRoute
import com.example.assets.navigation.navigateToAsset
import com.example.transactions.navigation.navigateToTransaction
import com.example.transactions.navigation.transactionRoute

import com.feature.home.navigation.homeRoute
import com.feature.home.navigation.navigateToHome
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

import org.ethereumphone.walletmanager.utils.Screen

@Composable
fun rememberWmAppState(
    networkMonitor: NetworkMonitor,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController()
): WmAppState {
    return remember(
        networkMonitor,
        navController,
        coroutineScope
    ) {
        WmAppState(
            navController,
            networkMonitor,
            coroutineScope
        )
    }
}




@Stable
class WmAppState(
    val navController: NavHostController,
    networkMonitor: NetworkMonitor,
    val coroutineScope: CoroutineScope
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination


    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    fun navigateToTopLevelDestination(topLevelDestination: Screen) {

        val topLevelNavOptions = navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }

        // Check if we are already on the home screen and the user clicks on the home button
        if (topLevelDestination.route == homeRoute && navController.currentDestination?.route != homeRoute) {
            navController.popBackStack(homeRoute, inclusive = false)
        } else {
            when (topLevelDestination.route) {
                homeRoute -> navController.navigateToHome(topLevelNavOptions)
                assetRoute -> navController.navigateToAsset(topLevelNavOptions)
                transactionRoute -> navController.navigateToTransaction(topLevelNavOptions)
            }
        }
    }
}