package org.ethereumphone.walletmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.workers.work.SeedTokensWorker
import dagger.hilt.android.AndroidEntryPoint
import org.ethereumphone.walletmanager.ui.WmApp
import org.ethereumphone.walletmanager.utils.SystemWalletAddressUpdater
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var walletAddressUpdater: SystemWalletAddressUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WorkManager.getInstance(applicationContext)
            .enqueue(SeedTokensWorker.startSeedNetworkBalanceWork())

        // checks periodically the address of the system wallet
        walletAddressUpdater.startPeriodicUpdate()


        setContent {
            // theme




            WmApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the periodic update
        walletAddressUpdater.stopPeriodicUpdate()
    }

}