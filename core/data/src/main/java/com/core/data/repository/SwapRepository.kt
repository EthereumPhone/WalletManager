package com.core.data.repository

import org.ethereumphone.walletsdk.WalletSDK
import org.ethosmobile.uniswap_routing_sdk.Token
import org.web3j.protocol.Web3j

interface SwapRepository {
    fun getBestRoute(
        fromToken: Token,
        toToken: Token,
        amount: Double
    ): List<String>

    suspend fun swap(
        fromToken: Token,
        toToken: Token,
        amount: Double
    ): String
}