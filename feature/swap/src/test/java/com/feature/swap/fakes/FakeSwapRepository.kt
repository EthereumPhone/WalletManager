package com.feature.swap.fakes

import com.core.data.repository.SwapRepository
import com.core.data.swap.ZeroXSwapQuoteResponse
import java.math.BigDecimal

class FakeSwapRepository : SwapRepository {

    var nextQuoteResult: Double = 0.0
    var nextSwapQuoteResult: ZeroXSwapQuoteResponse? = null
    var nextSwapResult: String = "tx_hash"

    override suspend fun getQuote(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: Double,
        receiverAddress: String,
        chainId: Int
    ): Double = nextQuoteResult

    override suspend fun getSwapQuote(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: BigDecimal,
        inputTokenDecimals: Int,
        outputTokenDecimals: Int,
        chainId: Int,
        inputTokenSymbol: String,
        outputTokenSymbol: String
    ): ZeroXSwapQuoteResponse? = nextSwapQuoteResult

    override suspend fun swap(
        inputTokenAddress: String,
        outputTokenAddress: String,
        amount: Double
    ): String = nextSwapResult
}


