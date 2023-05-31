package org.ethereumphone.walletmanager.core.data.remote

import org.ethereumphone.walletmanager.core.domain.model.TokenExchange
import retrofit2.http.GET
import retrofit2.http.Query


interface ExchangeApi {
    @GET("/api/v3/ticker/price")
    suspend fun getExchange(
        @Query("symbol") symbol: String
    ): TokenExchange

    @GET("/api/v3/ticker/price")
    suspend fun getAllExchanges(): List<TokenExchange>


    companion object {
        const val BASE_URL = "https://data.binance.com"
    }
}