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
import kotlinx.coroutines.withContext
import org.ethereumphone.walletsdk.WalletSDK
import javax.inject.Inject

class SystemWalletAddressUpdater @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val walletSDK: WalletSDK?
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var isUpdating = false


    fun startPeriodicUpdate() {
        if (!isUpdating) {
            isUpdating = true

            coroutineScope.launch {
                while (isUpdating) {
                    val addressCheck = walletSDK?.getAddress() ?: "0x3a4e6eD8B0F02BFBfaA3C6506Af2DB939eA5798c"
                    val networkCheck = walletSDK?.getChainId() ?: 1

                    val userData = userDataRepository.userData.first()

                    if(userData.walletAddress != addressCheck) {
                        userDataRepository.setWalletAddress(addressCheck)
                        Log.d("updater", "updated address")
                    }

                    if(userData.walletAddress == "") {
                        userDataRepository.setWalletNetwork(networkCheck.toString())
                    } else {
                        if(userData.walletNetwork.toInt() != networkCheck) {
                            userDataRepository.setWalletNetwork(networkCheck.toString())
                            Log.d("updater", "updated network")
                        }
                    }



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