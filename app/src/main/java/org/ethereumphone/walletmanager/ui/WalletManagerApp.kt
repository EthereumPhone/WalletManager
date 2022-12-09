package org.ethereumphone.walletmanager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.ethereumphone.walletmanager.navigation.WalletManagerNavHost
import org.ethereumphone.walletmanager.theme.WalletManagerTheme
import org.ethereumphone.walletmanager.ui.components.BottomNavBar

@Composable
fun WalletManagerApp(
    appState: WalletManagerState = rememberWalletManagerAppState()
) {
    WalletManagerTheme {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    destinations = appState.topLevelDestinations,
                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                    currentDestination = appState.currentDestination
                )
            }
        ) { innerPadding ->
            WalletManagerNavHost(
                navController = appState.navController,
                onBackClick = appState::onBackClick,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}