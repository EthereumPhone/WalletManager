package org.ethereumphone.walletmanager.ui

import androidx.compose.runtime.Composable
import org.ethereumphone.walletmanager.core.navigation.WmNavHost

@Composable
fun WmApp(
    appState: WmAppState = rememberWmAppState()
) {




    WmNavHost(appState.navController)


}