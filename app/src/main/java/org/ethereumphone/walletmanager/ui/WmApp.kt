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
import com.core.data.repository.SendRepository
import com.core.terminalsdk.ReflectiveLedPattern
import com.core.terminalsdk.TerminalSDK
import com.core.ui.util.dgenBlack
import kotlinx.coroutines.launch
import org.ethosmobile.components.library.core.ethOSSnackbarHost
import org.ethosmobile.components.library.utils.SnackbarState
import org.ethosmobile.components.library.utils.rememberSnackbarDelegate
import androidx.compose.foundation.layout.fillMaxSize
import org.ethereumphone.walletmanager.MainActivityViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WmApp(
    appState: WmAppState,
    viewModel: MainActivityViewModel,
    networkMonitor: NetworkMonitor,
    terminalSDK: TerminalSDK?,
    reflectiveLedPattern: ReflectiveLedPattern?
) {


    val currentRoute = appState.currentDestination?.route

    LaunchedEffect(currentRoute?.takeIf { it.isNotBlank() }) {
        viewModel.updateMatrix(currentRoute)
    }


    Scaffold(
       containerColor = dgenBlack,
    ) { _ ->
        WmNavHost(
            appState = appState,
            // Fill the entire screen without automatic system bar padding
            modifier = Modifier.fillMaxSize(),
            terminalSDK = terminalSDK,
        )
    }
}

@Composable
internal fun WmApp() {

}