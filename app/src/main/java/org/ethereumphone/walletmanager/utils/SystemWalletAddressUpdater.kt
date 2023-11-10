package org.ethereumphone.walletmanager.utils

import android.util.Log
import com.core.data.repository.UserDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import org.ethereumphone.walletsdk.WalletSDK
import javax.inject.Inject

class SystemWalletAddressUpdater @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val walletSDK: WalletSDK
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var isUpdating = false


    fun startPeriodicUpdate() {
        if (!isUpdating) {
            isUpdating = true
            coroutineScope.launch {
                while (isUpdating) {
                    val addressCheck = walletSDK.getAddress()
                    val network = walletSDK.getChainId()

                    if (addressCheck.isNotEmpty()) {
                        userDataRepository.setWalletAddress(addressCheck)
                    }
                    userDataRepository.setWalletNetwork(network.toString())
                    delay(500)
                }
            }
        }
    }

    fun stopPeriodicUpdate() {
        isUpdating = false
        coroutineScope.cancel()
    }
}