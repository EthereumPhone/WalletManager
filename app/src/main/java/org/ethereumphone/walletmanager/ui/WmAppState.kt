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
import com.core.model.CurrentState
import kotlinx.coroutines.CoroutineScope
import com.example.assets.navigation.assetRoute
import com.example.assets.navigation.navigateToAsset
import com.example.transactions.navigation.navigateToTransaction
import com.example.transactions.navigation.transactionRoute

import com.feature.home.navigation.homeRoute
import com.feature.home.navigation.navigateToHome

import org.ethereumphone.walletmanager.utils.Screen

@Composable
fun rememberWmAppState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController()
): WmAppState {
    return remember(navController, coroutineScope) {
        WmAppState(navController, coroutineScope)
    }
}




@Stable
class WmAppState(
    val navController: NavHostController,
    val coroutineScope: CoroutineScope
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    // val isOffline

    val currentState: CurrentState
        get() = currentState

    fun setCurrentState(newstate: CurrentState){
        clearCurrentState()
        currentState.address = newstate.address
        currentState.assets = newstate.assets
        currentState.name = newstate.name
        currentState.balance = newstate.balance
        currentState.symbol = newstate.symbol
    }

    fun clearCurrentState(){
        currentState.address = ""
        currentState.assets = emptyList()
        currentState.name = ""
        currentState.balance = 0.0
        currentState.symbol = ""
    }

    fun navigateToTopLevelDestination(topLevelDestination: Screen) {

        val topLevelNavOptions = navOptions {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }

        when (topLevelDestination.route) {
            homeRoute -> navController.navigateToHome(topLevelNavOptions)
            assetRoute -> navController.navigateToAsset(topLevelNavOptions)
            transactionRoute -> navController.navigateToTransaction(topLevelNavOptions)
        }

    }
}