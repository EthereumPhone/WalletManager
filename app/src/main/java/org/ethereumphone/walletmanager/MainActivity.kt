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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.core.data.repository.SendRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.data.util.NetworkMonitor
import com.core.designsystem.theme.background
import com.core.terminalsdk.ReflectiveLedPattern
import com.core.terminalsdk.TerminalSDK
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.workers.work.SeedTokensWorker
import com.workers.work.SeedUniswapTokensWorker
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

    companion object {
        private const val PREFS_NAME = "welcome_prefs"
        private const val KEY_LAST_REFRESH_TIME = "lastRefreshTime"
        private const val REFRESH_INTERVAL_MS = 90_000L // 1.5 minutes
    }


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

    private fun seedDataIfAllowed() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val lastRefresh = prefs.getLong(KEY_LAST_REFRESH_TIME, 0L)
        if (now - lastRefresh >= REFRESH_INTERVAL_MS) {
            val seedUniswapTokensWork = SeedUniswapTokensWorker.startSeedUniswapTokensWork()
            val seedNetworkBalanceWork = SeedTokensWorker.startSeedNetworkBalanceWork()
            

            WorkManager.getInstance(applicationContext)
                .beginWith(seedUniswapTokensWork)
                .then(seedNetworkBalanceWork)
                .enqueue()

            prefs.edit().putLong(KEY_LAST_REFRESH_TIME, now).apply()
        }
    }


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

        seedDataIfAllowed() // rate-limited
        



        reflectiveLedPattern?.setup()

        /*
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "seed UniswapTokens",
                ExistingWorkPolicy.REPLACE,
                SeedUniswapTokensWorker.startSeedUniswapTokensWork()
            )

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "data updater",
                ExistingWorkPolicy.REPLACE,
                SeedTokensWorker.startSeedNetworkBalanceWork()
            )
         */

        /*
        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)


        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            when(uiState) {
                MainActivityUiState.Loading -> false
                is MainActivityUiState.Success -> false
            }
        }
         */

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
        }
        
        // Clear LED pattern when app goes to background
        reflectiveLedPattern?.clear()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResumed()

        seedDataIfAllowed() // rate-limited
        




        SystemColorManager.refresh(this)

    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the periodic update
        walletAddressUpdater.stopPeriodicUpdate()
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                terminalSDK?.let {
                    it.resume(it.ID_STATUSBAR)
                    it.destroyTouchHandler()
                }
            }
        }

        reflectiveLedPattern?.clear()

    }

}

