package org.ethereumphone.walletmanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Snackbar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.core.data.util.NetworkMonitor
import com.core.designsystem.theme.background
import com.core.designsystem.theme.secondary
import org.ethereumphone.walletmanager.navigation.WmNavHost
import org.ethereumphone.walletmanager.utils.Screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.data.repository.SendRepository
import kotlinx.coroutines.launch
import org.ethosmobile.components.library.core.ethOSSnackbarHost
import org.ethosmobile.components.library.theme.Fonts
import org.ethosmobile.components.library.utils.SnackbarState
import org.ethosmobile.components.library.utils.rememberSnackbarDelegate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WmApp(
    networkMonitor: NetworkMonitor,
    sendRepository: SendRepository,
    appState: WmAppState = rememberWmAppState(
        networkMonitor,
        sendRepository
    ),
) {

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isOffline by appState.isOffline.collectAsStateWithLifecycle()

    //TODO: better state handling
    val currentTransferHash by appState.currentTransaction.collectAsStateWithLifecycle(initialValue = "")
    val currentTransferChainId by appState.currentChainId.collectAsStateWithLifecycle(initialValue = 0)

    val transactionSheetState = rememberModalBottomSheetState(true)
    var showSheet by remember { mutableStateOf(false) }


    // If user is not connected to the internet show a snack bar to inform them.
    val notConnectedMessage = "You aren’t connected to the internet"//"⚠\uFE0F You aren’t connected to the internet"
    LaunchedEffect(isOffline) {
        if (isOffline) {
            println("offline")

            snackbarHostState.showSnackbar(
                message = notConnectedMessage,
                duration = SnackbarDuration.Indefinite,
            )
        }
    }

    LaunchedEffect(currentTransferHash) {
        if (currentTransferHash.isNotEmpty()) {
            showSheet = true
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

        snackbarHost = { SnackbarHost(snackbarHostState) {
            Snackbar(
                contentColor = Color.Red,
                shape =  RoundedCornerShape(12.dp),
                backgroundColor = Color(0xFFC63B3B)
            ) {
                Row (
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(
                        text = it.visuals.message,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = Fonts.INTER,
                        )
                    )
                    Image(
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.White),
                        imageVector = Icons.Outlined.Error,
                        contentDescription = "Snackbar icon"
                    )
                }
            }

        } },
    ) { paddingValues ->
        WmNavHost(
            appState = appState,
            modifier = Modifier.padding(paddingValues),
        )

        if(showSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch {
                        transactionSheetState.hide()
                    }.invokeOnCompletion {
                        if(!transactionSheetState.isVisible) {
                            showSheet = false
                            appState.restoreState()
                        }
                    }
                },
                sheetState = transactionSheetState,
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                PendingTransactionStateUi(transactionHash = currentTransferHash, transactionChainId = currentTransferChainId)
            }
        }
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
        backgroundColor = Color.Black,
        contentColor = Color.White
    ){

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