package com.core.data.exchange

import com.core.model.exchange.ExchangeQuote
import com.core.model.exchange.ExchangeResult
import java.math.BigDecimal

interface ExchangeRepository {
    suspend fun getQuote(
        tokenInAddr: String,
        tokenOutAddr: String,
        amountIn: BigDecimal,
        decimalsIn: Int,
        chainId: Int,
        slippageBps: Int = 50
    ): ExchangeQuote

    suspend fun swap(quote: ExchangeQuote): ExchangeResult
}


