package org.ethereumphone.walletmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import org.ethereumphone.walletmanager.utils.SystemWalletAddressUpdater
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var walletAddressUpdater: SystemWalletAddressUpdater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // checks periodically the address of the system wallet
        walletAddressUpdater.startPeriodicUpdate()



    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the periodic update
        walletAddressUpdater.stopPeriodicUpdate()
    }

}