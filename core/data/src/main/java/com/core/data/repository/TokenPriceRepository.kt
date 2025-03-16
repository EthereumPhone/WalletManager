package com.core.data.repository

import com.core.data.BuildConfig
import com.core.data.remote.TokenPriceApi
import com.core.model.TokenPriceResponse
import jakarta.inject.Inject

class TokenPriceRepository @Inject constructor(
    private val tokenPriceApi: TokenPriceApi
) {
    suspend fun fetchTokenPrice(symbol: List<String>): TokenPriceResponse {
        // Use BuildConfig to get the API key
        return tokenPriceApi.getTokenPrice(
            BuildConfig.TOKEN_PRICE_API,
            symbol
        )
    }
}