package org.ethereumphone.walletmanager

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import android.content.Context
import android.content.SharedPreferences
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.core.data.repository.SendRepository
import com.core.data.util.NetworkMonitor
import com.core.designsystem.theme.background
import com.core.terminalsdk.ReflectiveLedPattern
import com.core.terminalsdk.TerminalLEDController
import com.core.terminalsdk.TerminalSDK
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ethereumphone.walletmanager.ui.WmApp
import org.ethereumphone.walletmanager.utils.SystemWalletAddressUpdater
import com.core.ui.util.SystemColorManager
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity() : ComponentActivity() {



    @Inject
    lateinit var walletAddressUpdater: SystemWalletAddressUpdater

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var sendRepository: SendRepository

    @Inject
    @JvmField
    var reflectiveLedPattern: ReflectiveLedPattern? = null

    @Inject
    @JvmField
    var terminalSDK: TerminalSDK? = null

    val viewModel: MainActivityViewModel by viewModels()

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Hide the status bar
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            // If you also want to hide the navigation bar:
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        }

        TerminalLEDController.initialize(this)


        // checks periodically the address & network of the system wallet
        walletAddressUpdater.startPeriodicUpdate()

        setContent {
            // theme
            val systemUiController = rememberSystemUiController()
            systemUiController.setStatusBarColor(
                darkIcons = false,
                color = background
            )
            systemUiController.setNavigationBarColor(
                darkIcons = false,
                color = background
            )

            WmApp(
                networkMonitor = networkMonitor,
                sendRepository = sendRepository,
                terminalSDK = terminalSDK,
                reflectiveLedPattern = reflectiveLedPattern
            )
        }
    }

    override fun onPause() {
        super.onPause()

        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                terminalSDK?.let {
                    it.resume(it.ID_STATUSBAR)
                    it.destroyTouchHandler()
                }
            }
            // Clear LED pattern when app goes to background
            //reflectiveLedPattern?.clear()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResumed()
        
        SystemColorManager.refresh(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        walletAddressUpdater.stopPeriodicUpdate()
        // Synchronously destroy the touch handler to ensure immediate cleanup
        terminalSDK?.destroyTouchHandlerSync()

        //reflectiveLedPattern?.clear()

    }

}

