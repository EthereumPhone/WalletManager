package org.ethereumphone.walletmanager.core.data.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.protocol.Web3j

class WalletService(
    @ApplicationContext private val context: Context,
    private val systemWallet: WalletSDK = WalletSDK(context)
) {

    fun getChainId() = systemWallet.getChainId()

    fun getAddress() = systemWallet.getAddress()

    fun getNetworkBalance() {

    }

    fun signMessage() {

    }

    fun singTransaction() {

    }
}