package org.ethereumphone.walletmanager.ui

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Dataset
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.core.designsystem.theme.background
import com.core.designsystem.theme.secondary
import com.feature.home.navigation.homeGraphRoutePattern
import com.feature.home.navigation.homeRoute
import com.example.assets.navigation.assetRoute
import com.example.transactions.navigation.transactionRoute
import com.feature.send.navigation.navigateToSend
import com.feature.send.navigation.sendRoute
import org.ethereumphone.walletmanager.navigation.WmNavHost
import org.ethereumphone.walletmanager.utils.Screen


@Composable
fun WmApp(
    appState: WmAppState = rememberWmAppState(),
) {

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
//
        }
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