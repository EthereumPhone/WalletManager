package org.ethereumphone.walletmanager.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.core.data.util.NetworkMonitor
import com.core.designsystem.theme.background
import com.core.designsystem.theme.secondary
import org.ethereumphone.walletmanager.navigation.WmNavHost
import org.ethereumphone.walletmanager.utils.Screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle



@Composable
fun WmApp(
    networkMonitor: NetworkMonitor,
    appState: WmAppState = rememberWmAppState(networkMonitor),
) {

    val snackbarHostState = remember { SnackbarHostState() }

    val isOffline by appState.isOffline.collectAsStateWithLifecycle()

    // If user is not connected to the internet show a snack bar to inform them.
    val notConnectedMessage = "⚠\uFE0F You aren’t connected to the internet"
    LaunchedEffect(isOffline) {
        if (isOffline) {
            println("offline")

            snackbarHostState.showSnackbar(
                message = notConnectedMessage,
                duration = SnackbarDuration.Indefinite,
            )
        }
    }

    val listScreens = listOf(Screen.Home,Screen.Assets,Screen.Transaction)
    Scaffold(
        containerColor = background,
        //bottombar
        bottomBar = {
            EthOSBottomBar(
                destinations = listScreens,//all screens
                onNavigateToDestination = appState::navigateToTopLevelDestination,
                currentDestination = appState.currentDestination,

            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        WmNavHost(
            appState = appState,
            modifier = Modifier.padding(paddingValues),
        )
    }
}


@Composable
private fun EthOSBottomBar(
    destinations: List<Screen>,
    onNavigateToDestination: (Screen) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {

    BottomNavigation (
        modifier = modifier,
        backgroundColor = Color.Black,//Color(0xFF262626)
        contentColor = Color.White
    ){

        println(currentDestination)

        destinations.forEach { destination ->
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            BottomNavigationItem(
                icon = {
                    Icon(
                        destination.icon,
                        contentDescription = null,
                        tint = if (selected) Color.White else secondary
                    )
                       },
                label = { Text(destination.label, color = if (selected) Color.White else secondary) },
                selected = selected,
                selectedContentColor = Color.White,
                unselectedContentColor = Color(0xFF9FA2A5),
                onClick = { onNavigateToDestination(destination) }
            )
        }
    }
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: Screen) =
    this?.hierarchy?.any {
        it.route?.contains(destination.route, true) ?: false
    } ?: false