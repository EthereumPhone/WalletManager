package org.ethereumphone.walletmanager.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.core.data.repository.SendRepository
import com.core.data.repository.TransferRepository
import com.core.data.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import com.example.assets.navigation.assetRoute
import com.example.assets.navigation.navigateToAsset
import com.example.transactions.navigation.navigateToTransaction
import com.example.transactions.navigation.transactionRoute

import com.feature.home.navigation.homeRoute
import com.feature.home.navigation.navigateToHome
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
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
            coroutineScope,
        )
    }
}


@Stable
class WmAppState(
    val navController: NavHostController,
    networkMonitor: NetworkMonitor,
    coroutineScope: CoroutineScope,
) {

    private val previousDestination = mutableStateOf<NavDestination?>(null)

    val currentDestination: NavDestination?
    @Composable get() {
        // Collect the currentBackStackEntryFlow as a state
        val currentEntry = navController.currentBackStackEntryFlow
            .distinctUntilChanged()
            .collectAsState(initial = null)

        // Fallback to previousDestination if currentEntry is null
        return currentEntry.value?.destination.also { destination ->
            if (destination != null) {
                previousDestination.value = destination
            }
        } ?: previousDestination.value
    }
}