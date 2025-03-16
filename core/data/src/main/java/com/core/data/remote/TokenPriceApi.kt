package com.core.data.remote

import com.core.model.TokenPriceResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TokenPriceApi {
    // e.g. GET https://api.g.alchemy.com/prices/v1/{apiKey}/tokens/by-symbol?symbols=ETH
    @GET("prices/v1/{apiKey}/tokens/by-symbol")
    suspend fun getTokenPrice(
        @Path("apiKey") apiKey: String,
        @Query("symbols") symbols: List<String>
    ): TokenPriceResponse
}