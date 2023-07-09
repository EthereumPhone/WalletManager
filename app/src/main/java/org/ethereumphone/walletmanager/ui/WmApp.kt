package org.ethereumphone.walletmanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.Color
import org.ethereumphone.walletmanager.navigation.WmNavHost


@Composable
fun WmApp(
    appState: WmAppState = rememberWmAppState()
) {

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        paddingValues
        
        
        WmNavHost(appState = appState)
    }
    
}