package org.ethereumphone.walletmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import org.ethereumphone.walletmanager.core.data.util.NetworkMonitor
import org.ethereumphone.walletmanager.core.designsystem.WmTheme
import org.ethereumphone.walletmanager.theme.WalletManagerTheme
import org.ethereumphone.walletmanager.ui.WalletManagerApp
import javax.inject.Inject

class WalletManagerActivity: ComponentActivity()  {

    //@Inject
    //lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            WmTheme {
                WalletManagerApp()
            }
        }
    }
}

