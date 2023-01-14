package org.ethereumphone.walletmanager.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.theme.TopLevelDestination
import org.ethereumphone.walletmanager.theme.Icon
import org.ethereumphone.walletmanager.theme.WalletManagerTheme
import org.ethereumphone.walletmanager.ui.rememberWalletManagerAppState

@Composable
fun BottomNavBar(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier
    ) {
    BottomNavigation {
        destinations.forEach { destination ->
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            BottomNavigationItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    val icon = destination.icon
                    if(icon is Icon.ImageVectorIcon) {
                        AnimatedIcon(
                            imageVector = icon.imageVector,
                            scale =  if(selected) 1.5f else 1f
                        )
                    }
                }
            )
        }
    }
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false



private val COLOR_UNSELECTED = Color(0xFF7B7C89)
private val COLOR_SELECTED = Color.White
private val ICON_SIZE = 24.dp

@Composable
fun AnimatedIcon(
    imageVector: ImageVector,
    scale: Float = 1f,
    size: Dp = ICON_SIZE,
) {
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = TweenSpec(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        )
    )
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .scale(animatedScale)
    )
}

@Preview
@Composable
fun PreviewBottomNavBar() {
    val appState = rememberWalletManagerAppState(
        network = mutableStateOf(
            Network(
                chainId = 1,
                chainName = "Ethereum Mainnet",
                chainCurrency = "ETH",
                chainRPC = "https://cloudflare-eth.com",
                chainExplorer = "https://etherscan.io"
            )
        )
    )
    val destination = appState.currentTopLevelDestination

    WalletManagerTheme {

        BottomNavBar(
            destinations = appState.topLevelDestinations,
            onNavigateToDestination = appState::navigateToTopLevelDestination,
            currentDestination = appState.currentDestination
        )
    }
}