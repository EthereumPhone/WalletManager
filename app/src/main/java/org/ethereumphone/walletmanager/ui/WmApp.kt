package org.ethereumphone.walletmanager.ui

import android.content.Intent
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.core.designsystem.theme.background
import com.feature.home.navigation.homeGraphRoutePattern
import com.feature.home.navigation.homeRoute
import com.example.assets.navigation.assetRoute
import com.example.transactions.navigation.transactionRoute
import com.feature.send.navigation.navigateToSend
import com.feature.send.navigation.sendRoute
import org.ethereumphone.walletmanager.navigation.WmNavHost


sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen(homeRoute, "Overview", Icons.Rounded.Home)
    object Assets : Screen(assetRoute, "Assets", Icons.Rounded.Dataset)
    object Transaction : Screen(transactionRoute, "Transactions", Icons.Rounded.Article)
}

@Composable
fun WmApp(
    appState: WmAppState = rememberWmAppState(),
) {

    val listScreens = listOf(Screen.Home,Screen.Assets,Screen.Transaction)
    val navController = appState.navController
    Scaffold(
        containerColor = background,
        //bottombar
        bottomBar = {

            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                listScreens.forEach { screen ->
                    BottomNavigationItem(
                        icon = { Icon( screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color(0xFF9FA2A5),
                        onClick = {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            navController.popBackStack(
                                navController.graph.findStartDestination().id,
                                false
                            )

                            // Navigate to the start destination of the selected tab
                            navController.navigate(screen.route)
                        }
                    )
                }



            }
        }
    ) { paddingValues ->



        WmNavHost(
            appState = appState,
            modifier = Modifier.padding(paddingValues),
        )

    }
}