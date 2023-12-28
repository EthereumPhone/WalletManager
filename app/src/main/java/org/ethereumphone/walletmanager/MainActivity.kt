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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.core.data.repository.SendRepository
import com.core.data.repository.TransferRepository
import com.core.data.repository.UserDataRepository
import com.core.data.util.NetworkMonitor
import com.core.designsystem.theme.background
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.workers.work.SeedTokensWorker
import com.workers.work.SeedUniswapTokensWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.withContext
import org.ethereumphone.walletmanager.ui.WmApp
import org.ethereumphone.walletmanager.utils.SystemWalletAddressUpdater
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var walletAddressUpdater: SystemWalletAddressUpdater

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var sendRepository: SendRepository

    val viewModel: MainActivityViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                sendRepository = sendRepository
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the periodic update
        walletAddressUpdater.stopPeriodicUpdate()
    }

}

