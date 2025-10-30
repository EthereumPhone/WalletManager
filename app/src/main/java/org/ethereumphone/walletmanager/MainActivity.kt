package org.ethereumphone.walletmanager

import android.app.Activity
import android.content.Intent
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
import android.util.Log
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
import org.ethereumphone.walletmanager.deeplink.Eip681DeepLinkHandler
import org.ethereumphone.walletmanager.deeplink.Eip681DeepLinkResult
import org.ethereumphone.walletmanager.ui.WmApp
import org.ethereumphone.walletmanager.utils.SystemWalletAddressUpdater
import com.core.ui.util.SystemColorManager
import org.ethereumphone.walletmanager.ui.rememberWmAppState
import com.core.data.service.WalletConnectService
import javax.inject.Inject
import android.Manifest
import android.os.Build

@AndroidEntryPoint
class MainActivity() : ComponentActivity() {



    @Inject
    lateinit var walletAddressUpdater: SystemWalletAddressUpdater

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var eip681DeepLinkHandler: Eip681DeepLinkHandler

    @Inject
    @JvmField
    var reflectiveLedPattern: ReflectiveLedPattern? = null

    @Inject
    @JvmField
    var terminalSDK: TerminalSDK? = null

    val viewModel: MainActivityViewModel by viewModels()

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var pendingDeepLink by mutableStateOf<Eip681DeepLinkResult?>(null)

    // Notification permission launcher for Android 13+
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.w("MainActivity", "Notification permission denied")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Request notification permission for Android 13+
        requestNotificationPermission()

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

        // Handle EIP-681 deep link
        handleDeepLink(intent)

        setContent {
            val systemUiController = rememberSystemUiController()
            systemUiController.setStatusBarColor(
                darkIcons = false,
                color = background
            )
            systemUiController.setNavigationBarColor(
                darkIcons = false,
                color = background
            )

            val appState = rememberWmAppState(networkMonitor = networkMonitor)


            WmApp(
                appState = appState,
                viewModel = viewModel,
                networkMonitor = networkMonitor,
                terminalSDK = terminalSDK,
                reflectiveLedPattern = reflectiveLedPattern,
                pendingDeepLink = pendingDeepLink,
                onDeepLinkHandled = { pendingDeepLink = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            when (uri?.scheme) {
                "ethereum" -> {
                    Log.d("MainActivity", "Processing EIP-681 deep link: $uri")
                    coroutineScope.launch {
                        val result = eip681DeepLinkHandler.handleIntent(intent, this@MainActivity)
                        withContext(Dispatchers.Main) {
                            pendingDeepLink = result
                        }
                    }
                }
                "wc" -> {
                    Log.d("MainActivity", "Processing WalletConnect URI: $uri")
                    // Pass the WalletConnect URI to the service
                    WalletConnectService.pair(this, uri.toString())
                    
                    // Close the activity immediately so user doesn't see the app open
                    // The connection will happen in the background via the service
                    finish()
                }
            }
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
        SystemColorManager.refresh(this)
        viewModel.onAppResumed()
    }

    override fun onDestroy() {
        super.onDestroy()

        walletAddressUpdater.stopPeriodicUpdate()
        // Synchronously destroy the touch handler to ensure immediate cleanup
        terminalSDK?.destroyTouchHandlerSync()

    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // User previously denied, show rationale and request again
                    Log.d("MainActivity", "Requesting notification permission (with rationale)")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // First time asking, request permission
                    Log.d("MainActivity", "Requesting notification permission (first time)")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d("MainActivity", "Notification permission not required (Android < 13)")
        }
    }

}

