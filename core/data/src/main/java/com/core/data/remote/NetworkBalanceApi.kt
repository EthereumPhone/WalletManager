package com.core.data.remote

import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NetworkBalanceApi {
    suspend fun getNetworkCurrency(
        address: String,
        rpc: String
    ): BigDecimal {
        val web3j = Web3j.build(HttpService(rpc))

        return suspendCoroutine { continuation ->
            web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .sendAsync()
                .thenAccept {
                    val weiBalance = it.balance
                    val ethBalance = Convert.fromWei(weiBalance.toString(), Convert.Unit.ETHER)
                    continuation.resume(ethBalance)
                }
            web3j.shutdown()
        }
    }
}