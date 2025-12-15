package com.core.data.remote

import android.util.Log
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class NetworkBalanceApi {
    companion object {
        private const val TAG = "NetworkBalanceApi"
    }
    
    suspend fun getNetworkCurrency(
        address: String,
        rpc: String
    ): BigDecimal {
        val web3j = Web3j.build(HttpService(rpc))

        return try {
            suspendCoroutine { continuation ->
                web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .thenAccept { response ->
                        try {
                            val weiBalance = response.balance
                            val ethBalance = Convert.fromWei(weiBalance.toString(), Convert.Unit.ETHER)
                            continuation.resume(ethBalance)
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    }
                    .exceptionally { throwable ->
                        Log.e(TAG, "Failed to fetch balance from $rpc: ${throwable.message}")
                        continuation.resumeWithException(throwable)
                        null
                    }
            }
        } finally {
            web3j.shutdown()
        }
    }
}