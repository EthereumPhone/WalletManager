package org.ethereumphone.walletmanager.ui

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.core.designsystem.theme.background
import com.feature.home.navigation.homeGraphRoutePattern
import com.feature.send.navigation.navigateToSend
import com.feature.send.navigation.sendRoute
import org.ethereumphone.walletmanager.navigation.WmNavHost


@Composable
fun WmApp(
    appState: WmAppState = rememberWmAppState(),
) {

    Scaffold(
        containerColor = background
    ) { paddingValues ->
        paddingValues

        WmNavHost(
            appState = appState,
            modifier = Modifier.padding(paddingValues),
        )

    }
}