package org.ethereumphone.walletmanager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.core.data.util.NetworkMonitor
import com.core.designsystem.theme.background
import com.core.designsystem.theme.secondary
import org.ethereumphone.walletmanager.navigation.WmNavHost
import org.ethereumphone.walletmanager.utils.Screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.data.repository.SendRepository
import com.core.terminalsdk.TerminalSDK
import com.core.ui.util.dgenBlack
import kotlinx.coroutines.launch
import org.ethosmobile.components.library.core.ethOSSnackbarHost
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
    terminalSDK: TerminalSDK?
) {

    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val snackbarHostState = rememberSnackbarDelegate(hostState,scope)

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
            println("wm offline")

//            snackbarHostState.showSnackbar(
//                message = notConnectedMessage,
//                duration = SnackbarDuration.Indefinite,
//            )
            //snackbarHostState.coroutineScope.launch{
                snackbarHostState.showSnackbar(state = SnackbarState.ERROR, message = notConnectedMessage, duration = SnackbarDuration.Indefinite)
            //}

        }else{
            println("wm online")
            snackbarHostState.snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    LaunchedEffect(currentTransferHash) {
        if (currentTransferHash.isNotEmpty()) {
            showSheet = true
        }
    }



    Scaffold(
       containerColor = dgenBlack,
    ) { paddingValues ->
        WmNavHost(
            appState = appState,
            modifier = Modifier.padding(paddingValues),
            terminalSDK = terminalSDK
        )
    }
}



private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: Screen) =
    this?.hierarchy?.any {
        it.route?.contains(destination.route, true) ?: false
    } ?: false